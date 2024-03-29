/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.hazelcast.spring.context.SpringAware;
import com.amp.crm.constants.CallRoutingOption;
import com.amp.crm.embeddable.OutboundDialerQueueRecord;
import com.amp.crm.pojo.DialerQueueAccountDetails;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.DialerType;
import com.amp.tms.exception.CallNotFoundException;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.hazelcast.entity.AgentWeightedPriority;
import com.amp.tms.hazelcast.entity.DialerCall;
import com.amp.tms.service.AgentQueueAssociationService;
import com.amp.tms.websocket.message.outbound.PhoneToType;
import java.util.List;
import java.util.Map;
import org.joda.time.LocalTime;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * 
 */
@SpringAware
public class PredictiveDialer extends AbstractDialer {

    private static final Logger LOG = LoggerFactory.getLogger(PredictiveDialer.class);

    @Autowired
    private AgentQueueAssociationService associationService;

    private PredictiveDialer() {
    }

    public PredictiveDialer(long dialerPk, OutboundDialerQueueRecord record, LocalTime endTime) {
        super(dialerPk, record, endTime);
    }

    private TriggerKey callBatchTriggerKey() {
        return TriggerKey.triggerKey("call-batch", pausableTriggerGroupPrefix());
    }

    @Override
    protected void scheduleTriggers() throws DialerException {
        super.scheduleTriggers();
        Trigger callBatchTrigger = TriggerBuilder.newTrigger()
                .withIdentity(callBatchTriggerKey())
                .forJob(CallBatchJob.NAME, CallBatchJob.GROUP)
                .usingJobData(CallBatchJob.buildDataMap(getDialerPk()))
                .withSchedule(SimpleScheduleBuilder
                        .repeatSecondlyForever(5)
                        .withMisfireHandlingInstructionNextWithRemainingCount())
                .startNow()
                .endAt(getEndTime().toDate())
                .build();
        try {
            scheduler.scheduleJob(callBatchTrigger);
        } catch (SchedulerException ex) {
            throw new DialerException(this, ex);
        }
    }

    @Override
    protected boolean handleAgentReadyInternal(int ext) {
        return callService.connectToNextWaitingCall(getQueuePk(), ext);
    }

    @Override
    protected String makeCall(Integer ext, LoanNumber loanNumber, DialerQueueAccountDetails details) {
        PhoneToType type = Utils.getPhoneToType(loanNumber, details);
        //ignore ext
        return callService.initiatePredictiveCall(record.getDialerQueueSettings(), loanNumber.getLoanPk(), type);
    }

    @Override
    public void callResponded(DialerCall call, long responseTimeMillis, CallRespondedCallback callback) throws DialerException {
        super.callResponded(call, responseTimeMillis, null);
        Map<Integer, AgentWeightedPriority> agents = associationService.getParticipatingAgents(getQueuePk(), CallDirection.OUTBOUND, true);

        long loanPk = call.getLoanPk();
        PhoneToType type = call.getCallInfo();
        if (!agents.isEmpty()) {
            //TODO support other call orders
            List<AgentTMS> agentList = agentService.getAgents(agents, record.getWeightedPriority(),
                    CallRoutingOption.LONGEST_IDLE);
            for (AgentTMS agent : agentList) {
                int ext = agent.getExtension();
                try {
                    if (callback.connectOutboundCallToAgent(ext,
                            call.getCallUUID(), record.getDialerQueueSettings(), loanPk, type)) {
                        return;
                    }
                } catch (CallNotFoundException ex) {
                    return;
                }
            }
        }
        callback.putCallOnWait(getDialerPk(), call.getCallUUID(), loanPk, type);
    }

    @Override
    public DialerType getDialerType() {
        return DialerType.PREDICTIVE;
    }

}
