/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spring.context.SpringAware;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.tms.db.entity.freeswitch.StaticDialplan;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.db.repository.DialplanRepository;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.hazelcast.Configs;
import com.amp.tms.hazelcast.entity.AgentCall;
import com.amp.tms.hazelcast.keys.StaticDialplanKey;
import com.amp.tms.hazelcast.keys.TMSDialplanKey;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service(DialplanService.BEAN_NAME)
public class DialplanService {

    public static final String BEAN_NAME = "dialplanService";

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private FreeswitchConfiguration freeswitchConfiguration;

    private IMap<TMSDialplanKey, TMSDialplan> tmsDialplanMap;
    //private IMap<String, TMSDialplanKey> tmsDialplanMapKeys;
    private IMap<StaticDialplanKey, StaticDialplan> staticDialplanMap;

    @Autowired
    private AgentCallService agentCallService;

    @Autowired
    private DialplanRepository dialplanRepository;

    private IExecutorService executorService;

    @PostConstruct
    private void init() {
        tmsDialplanMap = hazelcastService.getMap(Configs.TMSDIALPLAN_MAP);
        staticDialplanMap = hazelcastService.getMap(Configs.STATIC_DIALPLAN_MAP);
        executorService = hazelcastService.getExecutorService(Configs.DIALPLAN_EXECUTOR_SERVICE);
        //tmsDialplanMapKeys = hazelcastService.getMap(Configs.TMSDIALPLANKEYS_MAP);
    }

    private static final Logger log = LoggerFactory.getLogger(DialplanService.class);

    public StaticDialplan getStaticDialplan(String caller, String callee, FreeswitchContext context) {
        StaticDialplanKey key = new StaticDialplanKey();
        key.setCallee(callee);
        key.setCaller(caller);
        key.setContext(context);
        return staticDialplanMap.get(key);
    }

    public TMSDialplan findTMSDialplan(String tms_uuid, FreeswitchContext context, String order) {
        TMSDialplanKey key = new TMSDialplanKey(tms_uuid, context, order);
        log.info("Find: " + key.toString());

        TMSDialplan tmsDialplan = tmsDialplanMap.get(key);
        return tmsDialplan;
    }

    public TMSDialplan getPremaidDialplan(DialplanVariable variable) {
        String order = variable.getTmsOrderNext();
        boolean isNext = true;
        if (order == null) {
            isNext = false;
            order = variable.getTmsOrder();
        }
        if (variable.getRdnisInteger() != null) {
            AgentCall agentCall = null;
            if (variable.getCall_uuid() != null) {
                Map.Entry<Integer, AgentCall> entry = agentCallService.getTransferingCall(variable.getCall_uuid());
                if (entry != null) {
                    agentCall = entry.getValue();
                }
            }
            if (agentCall == null) {
                agentCall = agentCallService.getTransferingCall(variable.getRdnisInteger());
            }
            log.info("AgentCall: " + agentCall);
            if (agentCall != null) {
                if (Objects.equals(agentCall.getCallUUID(), variable.getCall_uuid())) {
                    order = variable.getTmsOrder();
                }
            }
        }
        TMSDialplanKey key = new TMSDialplanKey(variable.getTmsUUID(), variable.getContext(), order);
        log.info("Finding Dailplan: " + variable.getTmsUUID() + " - " + variable.getContext() + " - " + order + " isNext:" + isNext);
        TMSDialplan tmsDialplan = tmsDialplanMap.get(key);

        if (tmsDialplan == null && isNext) {
            order = variable.getTmsOrder();
            key = new TMSDialplanKey(variable.getTmsUUID(), variable.getContext(), order);
            log.info("Finding Dailplan: " + variable.getTmsUUID() + " - " + variable.getContext() + " - " + order);
            tmsDialplan = tmsDialplanMap.get(key);
        }

//        log.info("--------------------");
//        log.info("JSON TMSDialplan: " + tmsDialplan);
//        log.info("JSON TMSDialplan: " + tmsDialplan.getKey().getTms_uuid() + " - " + tmsDialplan.getKey().getContext() + " = " + tmsDialplan.isCompleted());
//        log.info(tmsDialplan.toJson());
//        log.info("JSON TMSDialplan :");
//        log.info("--------------------");
        if (tmsDialplan != null) {
            log.info("--------------------");
            log.info("Found Dialplan: " + tmsDialplan.getKey().toString() + " is complete: " + tmsDialplan.isCompleted());
            //log.info(tmsDialplan.toJson());
            log.info("--------------------");
        } else {
            log.info("--------------------");
            log.info("Not Found Dialplan: " + tmsDialplan.getKey().toString() + " is complete: " + tmsDialplan.isCompleted());
            //log.info(tmsDialplan.toJson());
            log.info("--------------------");
        }
        if (tmsDialplan.isCompleted() && tmsDialplan.getOnce()) {
            if (tmsDialplan.isDebugOn()) {
                log.info("--------------------");
                log.info("Checking TMSDialplan returning null: " + tmsDialplan.getKey().toString());
                log.info("Checking TMSDialplan returning null: Dialplan was used already  to allow multple use please set once to false on the dialplan.");
                log.info("Checking TMSDialplan returning null: Dialplan was used already  to allow multple use please set once to false on the dialplan.");
                //log.info(tmsDialplan.toJson());
                log.info("--------------------");
            }
            return null;
        }
        tmsDialplan.setCounter(tmsDialplan.getCounter() + 1);
        tmsDialplan.setCompleted(true);
        tmsDialplan.setRetreived(LocalDateTime.now());
        tmsDialplan.setElapseLife(System.currentTimeMillis() - tmsDialplan.getCreateLife());
        tmsDialplan.setUniqueID(variable.getUniqueID());
        tmsDialplan.setChannelCallUUID(variable.getChannelCallUUID());
        tmsDialplan.setVariables(variable.toJson());

        updateTMSDialplan(tmsDialplan);
        return tmsDialplan;
    }

    public TMSDialplan getTMSDialplanForCDR(String tms_uuid, FreeswitchContext context, String orderPower) {
        TMSDialplanKey key = new TMSDialplanKey(tms_uuid, context, orderPower);
        log.info("For CDR: " + key.toString());
        TMSDialplan tmsDialplan = tmsDialplanMap.get(key);
        tmsDialplan.setCdrDateTime(LocalDateTime.now());
        updateTMSDialplan(tmsDialplan);
        return tmsDialplan;
    }

    public TMSDialplan createTMSDialplan(TMSDialplanKey key) {
        TMSDialplan tmsDialplan = new TMSDialplan();
        tmsDialplan.setKey(key);
        tmsDialplan.setCreateDateTime(LocalDateTime.now());
        tmsDialplan.setCreateLife(System.currentTimeMillis());
        tmsDialplan.setUploadRecodingOnCallEnd(freeswitchConfiguration.getUploadRecordingEndOfCall());
        tmsDialplan.setUploadRecodingOnTouch(freeswitchConfiguration.getUploadRecordingOnTouch());
        tmsDialplan.setUploadRecodingURL(freeswitchConfiguration.getUploadRecordingURL());
        tmsDialplan.setGatewayVersion(freeswitchConfiguration.getGatewayVersion());
        tmsDialplan.setDefaultCallerIdNumber(freeswitchConfiguration.getDefaultCallerIdNumber());
        tmsDialplan.setOutboundBeepVolume(freeswitchConfiguration.getOutboundBeepVolume());
        tmsDialplan.setOutboundBeepLapseSpace(freeswitchConfiguration.getOutboundBeepLapseSpace());
        tmsDialplan.setOutboundBeepUseNew(freeswitchConfiguration.getOutboundBeepUseNew());
        tmsDialplan.setOutboundBeepOnDuration(freeswitchConfiguration.getOutboundBeepOnDuration());
        tmsDialplan.setOutboundBeepOffDuration(freeswitchConfiguration.getOutboundBeepOffDuration());
        tmsDialplan.setOutboundBeepHz(freeswitchConfiguration.getOutboundBeepOfHZ());
        if (freeswitchConfiguration.getTMSDialplanMapAsync()) {
            try {
                executorService.submitToKeyOwner(new PutTask(tmsDialplan), key).get();
            } catch (InterruptedException | ExecutionException ex) {
                tmsDialplanMap.putAsync(key, tmsDialplan);
            }
        } else {
            tmsDialplanMap.putAsync(key, tmsDialplan);
        }
        return tmsDialplan;
    }

    @SpringAware
    private static class PutTask implements Callable<Void>, DataSerializable {

        @Autowired
        @Qualifier(DialplanService.BEAN_NAME)
        private DialplanService service;

        private TMSDialplan dialplan;

        private PutTask() {
        }

        public PutTask(TMSDialplan dialplan) {
            this.dialplan = dialplan;
        }

        @Override
        public Void call() throws Exception {
            service.tmsDialplanMap.putAsync(dialplan.getKey(), dialplan);
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput odo) throws IOException {
            odo.writeObject(dialplan);
        }

        @Override
        public void readData(ObjectDataInput odi) throws IOException {
            dialplan = odi.readObject();
        }

    }

    public TMSDialplan createTMSDialplan(String tms_uuid, FreeswitchContext context) {
        return createTMSDialplan(new TMSDialplanKey(tms_uuid, context));
    }

    public TMSDialplan createTMSDialplan(String tms_uuid, FreeswitchContext context, Enum<?> order) {
        return createTMSDialplan(new TMSDialplanKey(tms_uuid, context, order.name()));
    }

    public TMSDialplan createTMSDialplan(String tms_uuid, FreeswitchContext context, String order) {
        return createTMSDialplan(new TMSDialplanKey(tms_uuid, context, order));
    }

    public TMSDialplan createTMSDialplan(String tms_uuid, FreeswitchContext context, int order) {
        return createTMSDialplan(new TMSDialplanKey(tms_uuid, context, Integer.toString(order)));
    }

    public void deleteTMSDialplan(TMSDialplan tmsDialplan) {
        tmsDialplanMap.delete(tmsDialplan.getKey());
    }

    public void updateTMSDialplan(TMSDialplan tmsDialplan) {
        if (freeswitchConfiguration.getTMSDialplanMapAsync1()) {
            try {
                executorService.submitToKeyOwner(new PutTask(tmsDialplan), tmsDialplan.getKey()).get();
            } catch (InterruptedException | ExecutionException ex) {
                tmsDialplanMap.putAsync(tmsDialplan.getKey(), tmsDialplan);
            }
        } else {
            tmsDialplanMap.putAsync(tmsDialplan.getKey(), tmsDialplan);
        }
        if (tmsDialplan.isDebugOn()) {
            log.info("--------------------");
            log.info("Updated TMSDialplan: " + tmsDialplan.getKey().toString() + " - " + tmsDialplan.getTms_type());
            log.info("--------------------");
        }
    }

    public boolean isTMSDialplan(DialplanVariable variable) {
        if (variable.getTmsDP() == null) {
            return false;
        }
        return variable.getTmsDP();
    }

    public boolean isAgentToAgentCall(DialplanVariable variable) {
        return variable.getCallDirection() == CallDirection.INTERNAL;
    }

    public Boolean isOutbound(DialplanVariable variable) {
        return variable.getCallDirection() == CallDirection.OUTBOUND;
    }

    public void LogDialplanInfoIntoDb(String callUUID, Object... content) {
        if (callUUID != null && callUUID.equals("DIALER_DIALER")) {
            if (freeswitchConfiguration.getDbLogingForDialer()) {
                dialplanRepository.logDialplanInfoIntoDb(callUUID, content);
            }
        } else {
            if (freeswitchConfiguration.getDbLoging()) {
                dialplanRepository.logDialplanInfoIntoDb(callUUID, content);
            }
        }
    }

}
