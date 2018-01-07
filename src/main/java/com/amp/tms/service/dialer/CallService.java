/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.hazelcast.core.IMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spring.context.SpringAware;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.crm.constants.PreviewDialerType;
import com.amp.crm.db.entity.base.dialer.DialerQueueSettings;
import com.amp.crm.db.entity.base.dialer.InboundDialerQueueSettings;
import com.amp.crm.db.entity.base.dialer.OutboundDialerQueueSettings;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.refrence.DDD;
import com.amp.tms.enumerated.refrence.HOLDOrder;
import com.amp.tms.exception.CallNotFoundException;
import com.amp.tms.freeswitch.pojo.DialerInfoPojo;
import com.amp.tms.freeswitch.pojo.InboundDialerInfoPojo;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.freeswitch.premaid.incoming.IncomingPlaceOffHold;
import com.amp.tms.freeswitch.premaid.outbound.CallOutWithAMD;
import com.amp.tms.freeswitch.premaid.outbound.ConnectCallToAgent;
import com.amp.tms.freeswitch.premaid.outbound.PowerDialer;
import com.amp.tms.hazelcast.AbstractEntryProcessor;
import com.amp.tms.hazelcast.Configs;
import com.amp.tms.hazelcast.QueueAdapter;
import com.amp.tms.hazelcast.entity.AgentWeightedPriority;
import com.amp.tms.hazelcast.entity.PrimaryCall;
import com.amp.tms.hazelcast.entity.WaitingCall;
import com.amp.tms.pojo.BorrowerInfo;
import com.amp.tms.service.AgentCallService;
import com.amp.tms.service.AgentQueueAssociationService;
import com.amp.tms.service.DialerQueueRecordService;
import com.amp.tms.service.DialplanService;
import com.amp.tms.service.FreeswitchConfiguration;
import com.amp.tms.service.FreeswitchService;
import com.amp.tms.service.TransferService;
import com.amp.tms.websocket.Websocket;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CallService implements Dialer.CallRespondedCallback {

    private static final Logger LOG = LoggerFactory.getLogger(CallService.class);

    @Autowired
    @Lazy
    private Websocket websocket;
    
    @Autowired
    private TransferService transferService;

    @Autowired
    private DialerQueueRecordService recordService;

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private AgentCallService agentCallService;

    @Autowired
    private FreeswitchService freeswitchService;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private DialplanService dialplanService;

    @Autowired
    private AgentQueueAssociationService agentQueueAssociationService;

    private IMap<Long, QueueAdapter<WaitingCall>> waitingQueuesMap;
    private IMap<Integer, QueueAdapter<PrimaryCall>> primaryCallMap;
    private IMap<String, Long> agentTransferWithQueueMap;

    @PostConstruct
    private void init() {
        waitingQueuesMap = hazelcastService.getMap(Configs.QUEUE_WAITING_CALL_MAP);
        primaryCallMap = hazelcastService.getMap(Configs.AGENT_TO_PRIMARY_CALL_MAP);
        agentTransferWithQueueMap = hazelcastService.getMap(Configs.AGENT_TRANSFER_QUEUE_MAP);
    }

    public void addAgentTransferQueueMap(Long queue, String callUIID) {
        agentTransferWithQueueMap.put(callUIID, queue);
    }

    public Long getAgentTransferQueueMap(String callUIID) {
        return agentTransferWithQueueMap.get(callUIID);
    }

    public QueueAdapter<WaitingCall> getWaitingCalls(long queuePk) {
        return waitingQueuesMap.get(queuePk);
    }

    public Boolean hasWaitingCall(long queuePk) {
        return (Boolean) waitingQueuesMap.executeOnKey(queuePk, new IsNotEmptyEntryProcessor());
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Boolean> hasWaitingCalls(Set<Long> queuePks) {
        if (queuePks == null || queuePks.isEmpty()) {
            return Collections.emptyMap();
        }
        return (Map) waitingQueuesMap.executeOnKeys(queuePks, new IsNotEmptyEntryProcessor());
    }

    public boolean connectToWaitingCall(long queuePk, int ext, String callUUID) {
        LOG.info("Connect to waiting call. QueuePk {}, ext {}, callUUID: {}", queuePk, ext, callUUID);
        WaitingCall call = removeWaitingCall(queuePk, callUUID);
        if (call == null) {
            return false;
        }
        LOG.info("Found Call! Trying to connect to agent {}", ext);
        try {
            if (connectWaitingCallToAgent(call, queuePk, ext)) {
                return true;
            }
        } catch (CallNotFoundException ex) {
            return false;
        }
        //something went wrong, put back waiting call
        pushWaitingCall(queuePk, call);
        return false;
    }

    public boolean connectToNextWaitingCall(long queuePk, int ext) {
        LOG.info("Connect to next waiting call {} {}", queuePk, ext);
        for (;;) {
            WaitingCall call = pollWaitingCall(queuePk);
            if (call == null) {
                LOG.info("no waiting calls for queue {}, ext: {}", queuePk, ext);
                return false;
            }
            try {
                if (connectWaitingCallToAgent(call, queuePk, ext)) {
                    return true;
                }
            } catch (CallNotFoundException ex) {
                //if that particular call was missing then we just try the next one.
                continue;
            }
            //something went wrong, put back waiting call
            LOG.info("Failed to aquire waiting call {} in queue {}, for agent {}", call.getCallUUID(), queuePk, ext);
            pushWaitingCall(queuePk, call);
            return false;
        }
    }

    private boolean connectWaitingCallToAgent(WaitingCall call, long queuePk, int ext) throws CallNotFoundException {
        boolean successful;

        LOG.info("Checking call is in queue transfer: {} for callUUID {} and orginal agent ext {} current ext {}", call.getOrginalAgentForTransfer() != null, call.getCallUUID(), call.getOrginalAgentForTransfer(), ext);
        if (call.getOrginalAgentForTransfer() != null) {

            if (ext == call.getOrginalAgentForTransfer()) {
                LOG.info("Call cannot transfer to the same agent {} for callUUID {}", ext, call.getCallUUID());
                successful = false;
            } else if (call.getOrginalAgentForTransfer() == 1010) {
                successful = true;
                LOG.info("Sending Transfer Call to {} from {} for Call UUID {} call started true fifo", ext, call.getOrginalAgentForTransfer(), call.getCallUUID());
                successful = connectInboundCallToAgent(ext, call.getCallUUID(), (InboundDialerQueueSettings) recordService.getQueueSettings(queuePk), call.getLoanPk(), call.getPhoneToType());
                LOG.info("Result: {}", successful);
            } else {
                if (agentCallService.callStarted(ext, call.getCallUUID(), null, true, null, CallDirection.INTERNAL, queuePk, null, false, 30)) {
                    LOG.info("Call started for for callUUID {} and orginal agent ext {} current ext {}", call.getOrginalAgentForTransfer() != null, call.getCallUUID(), call.getOrginalAgentForTransfer(), ext);

                    successful = true;
                    LOG.info("Sending Transfer Call to {} from {} for Call UUID {} call started true websocket.", ext, call.getOrginalAgentForTransfer(), call.getCallUUID());
                    transferService.sendToAgentTheExtToTransferTo(call.getOrginalAgentForTransfer(), ext, call.getCallUUID());

                } else {
                    successful = false;
                    LOG.info("Sending Transfer Call to {} from {} for Call UUID {} call started false", ext, call.getOrginalAgentForTransfer(), call.getCallUUID());
                }

            }
            return successful;
        }

        Map<Integer, Object> removedEntries = primaryCallMap.executeOnEntries(new RemovePrimaryCallEntryProcessor(call.getCallUUID()));
        DialerQueueSettings callSettings = recordService.getQueueSettings(queuePk);

        if (callSettings instanceof OutboundDialerQueueSettings) {
            successful = connectOutboundCallToAgent(ext, call.getCallUUID(), (OutboundDialerQueueSettings) callSettings, call.getLoanPk(), call.getPhoneToType());
        } else if (callSettings instanceof InboundDialerQueueSettings) {
            successful = connectInboundCallToAgent(ext, call.getCallUUID(), (InboundDialerQueueSettings) callSettings, call.getLoanPk(), call.getPhoneToType());
        } else {
            throw new IllegalStateException(call + " has unsupported SvDialerQueueSettings class: " + callSettings.getClass());
        }
        if (!successful) {
            for (Map.Entry<Integer, Object> entrySet : removedEntries.entrySet()) {
                Integer key = entrySet.getKey();
                PrimaryCall primCall = (PrimaryCall) entrySet.getValue();
                primaryCallMap.submitToKey(key, new PushCallEntryProcessor<>(primCall), null);
            }
        }
        return successful;
    }

    public void putCallOnWait(long queuePk, String callUUID, Long loanId, PhoneToType phoneToTypes, Integer orginalAgentForTransfer) {
        if (callUUID == null) {
            throw new IllegalArgumentException(String.format("callUUID cannot be null. loanId: %s, phoneNumber: %s",
                    loanId, phoneToTypes.getPhoneNumber()));
        }
        if (orginalAgentForTransfer != null) {
            LOG.info("Putting call on wait from orginal agent transfer {} for callUUID {}", orginalAgentForTransfer, callUUID);
        }
        WaitingCall call = new WaitingCall(callUUID, loanId, phoneToTypes, orginalAgentForTransfer);
        waitingQueuesMap.executeOnKey(queuePk, new OfferCallEntryProcessor<>(call));
    }

    @Override
    public void putCallOnWait(long queuePk, String callUUID, Long loanId, PhoneToType phoneToTypes) {
        putCallOnWait(queuePk, callUUID, loanId, phoneToTypes, null);
    }

    private void pushWaitingCall(long queuePk, WaitingCall call) {
        waitingQueuesMap.executeOnKey(queuePk, new PushCallEntryProcessor<>(call));
    }

    private WaitingCall pollWaitingCall(long queuePk) {
        return (WaitingCall) waitingQueuesMap.executeOnKey(queuePk, new PollCallEntryProcessor<>());
    }

    public WaitingCall removeWaitingCall(long queuePk, String callUUID) {
        return (WaitingCall) waitingQueuesMap.executeOnKey(queuePk, new RemoveWaitingCallEntryProcessor(callUUID));
    }

    public String initiatePredictiveCall(OutboundDialerQueueSettings settings, Long loanId, PhoneToType phoneToTypes) {
        DialerInfoPojo dialerInfoPojo = new DialerInfoPojo();
        dialerInfoPojo.setBorrowerFirstName(phoneToTypes.getFirstName());
        dialerInfoPojo.setBorrowerLastName(phoneToTypes.getLastName());
        dialerInfoPojo.setLoanId(loanId);
        dialerInfoPojo.setSettings(settings);
        dialerInfoPojo.addPhoneToTypeSingle(phoneToTypes);
        dialerInfoPojo.setDialerMode(settings.getDialerMode());
        DialplanBuilder builder = new CallOutWithAMD(dialerInfoPojo);
        builder.execute();
        return builder.getTMS_UUID();
    }

    public String initiateCall(OutboundDialerQueueSettings settings, Long loanId, Integer agentExt, PhoneToType phoneToTypes) {

        DialerInfoPojo dialerInfoPojo = new DialerInfoPojo();
        dialerInfoPojo.setAgentExt(agentExt);
        dialerInfoPojo.setBorrowerFirstName(phoneToTypes.getFirstName());
        dialerInfoPojo.setBorrowerLastName(phoneToTypes.getLastName());
        dialerInfoPojo.setLoanId(loanId);
        dialerInfoPojo.setSettings(settings);
        dialerInfoPojo.addPhoneToTypeSingle(phoneToTypes);
        dialerInfoPojo.setDialerMode(settings.getDialerMode());
        dialerInfoPojo.setPreviewDialerType(settings.getPreviewDialerType());

        dialerInfoPojo.setCallUUID(UUID.randomUUID().toString());

        BorrowerInfo info = new BorrowerInfo();
        info.setLoanId(loanId);
        info.setBorrowerFirstName(phoneToTypes.getFirstName());
        info.setBorrowerLastName(phoneToTypes.getLastName());
        info.setBorrowerPhoneNumber(Long.toString(phoneToTypes.getPhoneNumber()));

        if (!agentCallService.callStarted(agentExt, dialerInfoPojo.getCallUUID(),
                settings.getPreviewDialerType(), false, info, CallDirection.OUTBOUND,
                settings.getDialerQueuePk(), null, true, configuration.getCallWaitTimeoutBeforeConnect(null))) {
            return null;
        }

        if (settings.getPreviewDialerType() == PreviewDialerType.REGULAR) {
            DialplanBuilder builder = new PowerDialer(dialerInfoPojo);
            builder.execute();
        } else {
            websocket.sendToAgent(dialerInfoPojo);
        }
        return dialerInfoPojo.getCallUUID();
    }

    public String initiateCallPreviewSelect(OutboundDialerQueueSettings settings, Long loanId, Integer agentExt, ArrayList<PhoneToType> phoneToTypes) {

        DialerInfoPojo dialerInfoPojo = new DialerInfoPojo();
        dialerInfoPojo.setAgentExt(agentExt);
        dialerInfoPojo.setLoanId(loanId);
        dialerInfoPojo.setPhoneToType(phoneToTypes);
        dialerInfoPojo.setSettings(settings);
        dialerInfoPojo.setDialerMode(settings.getDialerMode());
        dialerInfoPojo.setPreviewDialerType(settings.getPreviewDialerType());
        dialerInfoPojo.setCallUUID(UUID.randomUUID().toString());

        BorrowerInfo info = new BorrowerInfo();
        info.setLoanId(loanId);

        if (!agentCallService.callStarted(agentExt, dialerInfoPojo.getCallUUID(),
                settings.getPreviewDialerType(), false, info, CallDirection.OUTBOUND,
                settings.getDialerQueuePk(), null, true, configuration.getCallWaitTimeoutBeforeConnect(null))) {
            return null;
        }

        websocket.sendToAgent(dialerInfoPojo);

        return dialerInfoPojo.getCallUUID();
    }

    public String initiateBroadcast(OutboundDialerQueueSettings settings, Long loanId, PhoneToType phoneToTypes) {
        return "";
    }

    @Override
    public boolean connectOutboundCallToAgent(int ext, String CallUUID, OutboundDialerQueueSettings settings, Long loanId, PhoneToType phoneToTypes)
            throws CallNotFoundException {
        LOG.info("Connect outbound call to agent {} {}", ext, CallUUID);
        BorrowerInfo info = new BorrowerInfo();
        info.setBorrowerFirstName(phoneToTypes.getFirstName());
        info.setBorrowerLastName(phoneToTypes.getLastName());
        info.setBorrowerPhoneNumber(Long.toString(phoneToTypes.getPhoneNumber()));
        info.setLoanId(loanId);

        //TODO fix with proper parameters
        TMSDialplan old = dialplanService.findTMSDialplan(CallUUID, FreeswitchContext.fifo_dp, DDD.PLACE_CALL_IN_FIFO.name());
        if (old == null) {
            LOG.error("Punting out of the call for outbound call.  The call in question does was not foud in the TMSDialplan. {}", CallUUID);
            throw new CallNotFoundException("Unable to find TMSDialplan for call: " + CallUUID);
        }
        if (!freeswitchService.callUUIDExistsOnFreeswitch(old.getUniqueID())) {
            LOG.error("Punting out of the call for outbound call.  The call in question does  not exists in the fifo. {}", CallUUID);
            throw new CallNotFoundException("Unable to find call in fifo: " + CallUUID);
        }

        LOG.info("Connect outbound call to agent {} {} before call started", ext, CallUUID);
        if (!agentCallService.callStarted(ext, CallUUID, null, false, info, CallDirection.OUTBOUND, settings.getDialerQueuePk(), null, true, configuration.getCallWaitTimeoutBeforeConnect(null))) {
            LOG.info("Connect outbound call to agent {} {} call started", ext, CallUUID);
            return false;
        }
        LOG.info("Connect outbound call to agent {} {} after call started", ext, CallUUID);
        DialerInfoPojo dialerInfoPojo = new DialerInfoPojo();
        dialerInfoPojo.setBorrowerFirstName(phoneToTypes.getFirstName());
        dialerInfoPojo.setBorrowerLastName(phoneToTypes.getLastName());
        dialerInfoPojo.setLoanId(loanId);
        dialerInfoPojo.setSettings(settings);
        dialerInfoPojo.addPhoneToTypeSingle(phoneToTypes);
        dialerInfoPojo.setDialerMode(settings.getDialerMode());
        dialerInfoPojo.setAgentExt(ext);
        dialerInfoPojo.setCallUUID(CallUUID);

        LOG.info("Connected outbound call to agent {} {} before dialplan", ext, CallUUID);

        DialplanBuilder builder = new ConnectCallToAgent(dialerInfoPojo, old);
        builder.setTMS_UUID(CallUUID);
        builder.execute();

        LOG.info("Connected outbound call to agent {} {}", ext, CallUUID);
        return true;
    }

//    public boolean connectInboundCallToAgentWithoutSettings(int ext, String CallUUID, Long loanId, PhoneToType phoneToTypes, Long queuePK) {
//        SvInboundDialerQueueSettings settings = new SvInboundDialerQueueSettings();
//        settings.setAutoAnswerEnabled(true);
//        settings.setPopupDisplayMode(PopupDisplayMode.SAME_WINDOW);
//        settings.setDialerQueuePk(queuePK);
//        settings.setMaxDelayBeforeAgentAnswer(30l);
//
//        InboundDialerInfoPojo dialerInfoPojo = new InboundDialerInfoPojo();
//        dialerInfoPojo.setBorrowerFirstName(phoneToTypes.getFirstName());
//        dialerInfoPojo.setBorrowerLastName(phoneToTypes.getLastName());
//        dialerInfoPojo.setLoanId(loanId);
//        dialerInfoPojo.setSettings(settings);
//        dialerInfoPojo.addPhoneToTypeSingle(phoneToTypes);
//        dialerInfoPojo.setAgentExt(ext);
//        dialerInfoPojo.setCallUUID(CallUUID);
//        DialplanBuilder builder = new IncomingPlaceOffHoldTransffer(dialerInfoPojo);
//        builder.setTMS_UUID(CallUUID);
//        builder.execute();
//        return true;
//    }
    public boolean connectInboundCallToAgent(int ext, String CallUUID, InboundDialerQueueSettings settings, Long loanId, PhoneToType phoneToTypes)
            throws CallNotFoundException {

        LOG.info("Looking for Old TMSDialplan {} {}, {}", CallUUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_ON_HOLD.name());
        TMSDialplan old = dialplanService.findTMSDialplan(CallUUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_ON_HOLD.name());

        if (old == null) {
            LOG.info("Looking for Old TMSDialplan {} {}, {}", CallUUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_ON_HOLD_CUSTOMER_SERVICE.name());
            old = dialplanService.findTMSDialplan(CallUUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_ON_HOLD_CUSTOMER_SERVICE.name());
        }
        if (old == null) {
            LOG.info("Looking for Old TMSDialplan {} {}, {}", CallUUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_ON_HOLD_TRANSFER.name());
            old = dialplanService.findTMSDialplan(CallUUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_ON_HOLD_TRANSFER.name());
        }

        if (old == null) {
            LOG.error("Punting out of the call for outbound call.  The call in question does was not foud in the TMSDialplan. {}", CallUUID);
            throw new CallNotFoundException("Unable to find TMSDialplan for call: " + CallUUID);
        }
        if (!freeswitchService.callUUIDExistsOnFreeswitch(old.getUniqueID())) {
            LOG.error("Punting out of the call for outbound call.  The call in question does  not exists in the fifo. {}", CallUUID);
            throw new CallNotFoundException("Unable to find call in fifo: " + CallUUID);
        }

        AgentWeightedPriority agentWeightedPriority = agentQueueAssociationService.getAssociation(ext, settings.getDialerQueuePk());
        Long agentGroupId = null;
        if (agentWeightedPriority != null) {
            agentGroupId = agentWeightedPriority.getGroupPk();
        }
        BorrowerInfo info = new BorrowerInfo();
        info.setBorrowerFirstName(phoneToTypes.getFirstName());
        info.setBorrowerLastName(phoneToTypes.getLastName());
        info.setBorrowerPhoneNumber(Long.toString(phoneToTypes.getPhoneNumber()));
        info.setLoanId(loanId);

        Integer delay = 30;
        if (settings.getMaxDelayBeforeAgentAnswer() != null) {
            delay = settings.getMaxDelayBeforeAgentAnswer().intValue();
        }
        //TODO fix with proper parameters
        if (!agentCallService.callStarted(ext, CallUUID, null, false, info, CallDirection.INBOUND, settings.getDialerQueuePk(), null, false, delay)) {
            return false;
        }

        InboundDialerInfoPojo dialerInfoPojo = new InboundDialerInfoPojo();
        dialerInfoPojo.setBorrowerFirstName(phoneToTypes.getFirstName());
        dialerInfoPojo.setBorrowerLastName(phoneToTypes.getLastName());
        dialerInfoPojo.setLoanId(loanId);
        dialerInfoPojo.setSettings(settings);
        dialerInfoPojo.addPhoneToTypeSingle(phoneToTypes);
        dialerInfoPojo.setAgentExt(ext);
        dialerInfoPojo.setCallUUID(CallUUID);
        dialerInfoPojo.setAgentGroupId(agentGroupId);
        DialplanBuilder builder = new IncomingPlaceOffHold(dialerInfoPojo, old);
        builder.setTMS_UUID(CallUUID);
        builder.execute();
        return true;
    }

    public void addPrimaryCall(int ext, long queuePk, String callUUID) {
        PrimaryCall call = new PrimaryCall(queuePk, callUUID);
        LOG.info("adding primary call {} for agent {}", callUUID, ext);
        primaryCallMap.executeOnKey(ext, new OfferCallEntryProcessor<>(call));
    }

    public boolean connectToWaitingPrimaryCall(int ext) {
        PollCallEntryProcessor<PrimaryCall> processor = new PollCallEntryProcessor<>();
        for (;;) {
            PrimaryCall call = (PrimaryCall) primaryCallMap.executeOnKey(ext, processor);
            if (call == null) {
                LOG.info("No more primary calls for agent {}", ext);
                return false;
            }
            LOG.info("Check agent {} for primary call {}", ext, call.getCallUUID());
            if (connectToWaitingCall(call.getQueuePk(), ext, call.getCallUUID())) {
                LOG.info("Connected agent {} to primary call {}", ext, call.getCallUUID());
                return true;
            }
        }
    }

    public static class IsNotEmptyEntryProcessor extends AbstractEntryProcessor<Long, QueueAdapter<?>> {

        public IsNotEmptyEntryProcessor() {
            super(false);
        }

        @Override
        public Boolean process(Map.Entry<Long, QueueAdapter<?>> entry, boolean isPrimary) {
            QueueAdapter<?> queue = entry.getValue();
            if (queue == null) {
                return false;
            }
            return !queue.isEmpty();
        }

    }

    @SpringAware
    public static class PushCallEntryProcessor<T> extends AddCallEntryProcessor<T> {

        private PushCallEntryProcessor() {
        }

        public PushCallEntryProcessor(T call) {
            super(call);
        }

        @Override
        protected void add(QueueAdapter<T> queue, T call) {
            queue.push(call);
        }

    }

    @SpringAware
    public static class OfferCallEntryProcessor<T> extends AddCallEntryProcessor<T> {

        private OfferCallEntryProcessor() {
        }

        public OfferCallEntryProcessor(T call) {
            super(call);
        }

        @Override
        protected void add(QueueAdapter<T> queue, T call) {
            queue.offer(call);
        }

    }

    @SpringAware
    public static abstract class AddCallEntryProcessor<T> extends AbstractEntryProcessor<Object, QueueAdapter<T>> {

        private T call;

        @Autowired
        @Qualifier(AgentQueueAssociationService.BEAN_NAME)
        private AgentQueueAssociationService agentQueueAssociationService;

        private AddCallEntryProcessor() {
        }

        public AddCallEntryProcessor(T call) {
            this.call = call;
        }

        protected abstract void add(QueueAdapter<T> queue, T call);

        @Override
        public Void process(Map.Entry<Object, QueueAdapter<T>> entry, boolean isPrimary) {
            QueueAdapter<T> queue = entry.getValue();
            if (queue == null) {
                queue = new QueueAdapter<>();
            }
            if (!queue.contains(call)) {
                boolean notifyAssoc = isPrimary && entry.getKey() instanceof Long;
                boolean isEmpty = queue.isEmpty();
                add(queue, call);
                entry.setValue(queue);

                if (isEmpty && notifyAssoc) {
                    agentQueueAssociationService.setAssociationHasWaitingCall((Long) entry.getKey(), true);
                }
            }
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeObject(call);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            call = in.readObject();
        }

    }

    @SpringAware
    public static class RemoveWaitingCallEntryProcessor extends RemoveCallEntryProcessor<WaitingCall> {

        public RemoveWaitingCallEntryProcessor() {
        }

        public RemoveWaitingCallEntryProcessor(String callUUID) {
            super(callUUID);
        }

        @Override
        protected boolean matches(WaitingCall call) {
            return call.getCallUUID().equals(callUUID);
        }

    }

    @SpringAware
    public static class PollCallEntryProcessor<T> extends AbstractEntryProcessor<Object, QueueAdapter<T>> {

        @Autowired
        @Qualifier(AgentQueueAssociationService.BEAN_NAME)
        private AgentQueueAssociationService agentQueueAssociationService;

        public PollCallEntryProcessor() {
        }

        @Override
        public T process(Map.Entry<Object, QueueAdapter<T>> entry, boolean isPrimary) {
            QueueAdapter<T> queue = entry.getValue();
            if (queue == null) {
                return null;
            }
            boolean notifyAssoc = isPrimary && entry.getKey() instanceof Long;
            boolean isEmpty = queue.isEmpty();
            T call = queue.poll();
            entry.setValue(queue.isEmpty() ? null : queue);
            if (!isEmpty && queue.isEmpty() && notifyAssoc) {
                agentQueueAssociationService.setAssociationHasWaitingCall((Long) entry.getKey(), false);
            }
            return call;
        }

    }

    @SpringAware
    public static class RemovePrimaryCallEntryProcessor extends RemoveCallEntryProcessor<PrimaryCall> {

        private RemovePrimaryCallEntryProcessor() {
        }

        public RemovePrimaryCallEntryProcessor(String callUUID) {
            super(callUUID);
        }

        @Override
        protected boolean matches(PrimaryCall call) {
            return call.getCallUUID().equals(callUUID);
        }

    }

    @SpringAware
    public abstract static class RemoveCallEntryProcessor<T> extends AbstractEntryProcessor<Object, QueueAdapter<T>> {

        protected String callUUID;

        @Autowired
        @Qualifier(AgentQueueAssociationService.BEAN_NAME)
        private AgentQueueAssociationService agentQueueAssociationService;

        public RemoveCallEntryProcessor() {
        }

        public RemoveCallEntryProcessor(String callUUID) {
            this.callUUID = callUUID;
        }

        protected abstract boolean matches(T call);

        @Override
        public T process(Map.Entry<Object, QueueAdapter<T>> entry, boolean isPrimary) {
            QueueAdapter<T> queue = entry.getValue();
            T foundCall = null;
            boolean isEmpty = queue == null || queue.isEmpty();
            if (queue != null) {
                for (Iterator<T> iterator = queue.iterator(); iterator.hasNext();) {
                    T call = iterator.next();
                    if (matches(call)) {
                        foundCall = call;
                        iterator.remove();
                        break;
                    }
                }
            }
            if (foundCall != null) {
                entry.setValue(queue.isEmpty() ? null : queue);
                boolean notifyAssoc = isPrimary && entry.getKey() instanceof Long;
                if (!isEmpty && queue.isEmpty() && notifyAssoc) {
                    agentQueueAssociationService.setAssociationHasWaitingCall((Long) entry.getKey(), false);
                }
            }
            return foundCall;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            super.writeData(out);
            out.writeUTF(callUUID);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            super.readData(in);
            callUUID = in.readUTF();
        }

    }
}
