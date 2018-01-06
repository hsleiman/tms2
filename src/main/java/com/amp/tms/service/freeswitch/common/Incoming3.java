/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.freeswitch.common;

import com.objectbrains.ams.iws.User;
import com.amp.crm.service.dialer.DialerQueueService;
import com.amp.crm.service.tms.TMSService;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.IncomingCallRouting;
import com.amp.tms.enumerated.WorkHours;
import com.amp.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.freeswitch.premaid.incoming.IncomingDialerOrder;
import com.amp.tms.freeswitch.premaid.incoming.IncomingIVR;
import com.amp.tms.freeswitch.premaid.incoming.IncomingIVR2;
import com.amp.tms.freeswitch.premaid.incoming.IncomingIVR2AfterHour;
import com.amp.tms.freeswitch.premaid.incoming.IncomingIVRIdentity;
import com.amp.tms.freeswitch.premaid.incoming.IncomingIVRIdentity2;
import com.amp.tms.freeswitch.premaid.incoming.IncomingPlaceOnHold;
import com.amp.tms.freeswitch.premaid.incoming.IncomingVoicemail;
import com.amp.tms.freeswitch.premaid.local.P1AgentToP2Agent;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.service.TMSAgentService;
import com.amp.tms.service.AgentStatsService;
import com.amp.tms.service.AmsService;
import com.amp.tms.service.CallDetailRecordService;
import com.amp.tms.service.DialplanService;
import com.amp.tms.service.FreeswitchConfiguration;
import com.amp.tms.service.InboundCallService;
import java.util.UUID;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service
public class Incoming3 {

    @Autowired
    private InboundCallService inboundCallService;

    @Autowired
    private TMSService tmsIWS;

    @Autowired
    private AmsService amsService;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private TMSAgentService agentService;

    @Autowired
    private AgentStatsService agentStatsService;

    @Autowired
    private DialerQueueService dialerQueueService;

    @Autowired
    private DialplanService dialplanRepository;

    private static final Logger log = LoggerFactory.getLogger(Incoming3.class);

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    public DialplanBuilder getDialplanBuilderSBC(DialplanVariable variable) {

        AgentIncomingDistributionOrder ado = null;
//
//       if (variable.getCalleeIdNumber().endsWith("8889")) {
//            log.info("Load test. ");
//            return (new IncomingBlackhole(variable));
//        }

        String TMS_UUID = UUID.randomUUID().toString();
        String destinationNumber = variable.getCalleeIdNumber();

        callDetailRecordService.updateInboundDIDNumber(TMS_UUID, destinationNumber);

        if (destinationNumber != null && destinationNumber.length() == "221711".length() && destinationNumber.startsWith("22")) {
            try {
                Integer agentExt = Integer.parseInt(destinationNumber.substring(2));
                log.info("Finding Agent : {}", agentExt);
                AgentTMS agent = agentService.getAgent(agentExt);
                boolean sendCall = false;
                switch (agentStatsService.getAgentState(agentExt)) {
                    case IDLE:
                    case WRAP:
                        sendCall = true;
                }
                if (agent != null && sendCall) {
                    DialplanBuilder p1AgentToP2Agent = new P1AgentToP2Agent(variable, agent);
                    p1AgentToP2Agent.setTMS_UUID(TMS_UUID);
                    return p1AgentToP2Agent;
                }
            } catch (Exception ex) {
                log.error("Exception when sending call from p1 to p2: {}", ex);
            }
        }

        long queuePk = dialerQueueService.getDialerQueuePkForPhoneNumber(destinationNumber);
        log.info("Checking Distination {} is in queue {}", variable.getCalleeIdLong(), queuePk);
        dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Checking Distination {} is in queue {}", variable.getCalleeIdLong(), queuePk);

        if (queuePk != -1) {
            callDetailRecordService.updateQueueId(TMS_UUID, queuePk);
            ado = inboundCallService.inboundCallOrder(queuePk, variable.getCallerIdLong(), TMS_UUID);
            log.info("Sending to IncomingDialerOrder. Distination was {}", variable.getCalleeIdLong());
            dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Sending to IncomingDialerOrder. Distination was {}", variable.getCalleeIdLong());
            WorkHours workHours = isWorkingHour(ado, destinationNumber);
            callDetailRecordService.updateIsOpen(TMS_UUID, workHours.isOpen());
            log.info(workHours.dump());
            if (workHours.isOpen() == false) {
                DialplanBuilder incomingVoicemail;
                incomingVoicemail = new IncomingVoicemail(variable, ado, workHours, Boolean.FALSE);
                incomingVoicemail.setTMS_UUID(TMS_UUID);
                return incomingVoicemail;
            }
            if (ado.getAgents().isEmpty() == false) {
                DialplanBuilder incomingDialerOrder = new IncomingDialerOrder(variable, ado);
                incomingDialerOrder.setTMS_UUID(TMS_UUID);
                return incomingDialerOrder;
            }
        }

        log.info("Checking caller id length {} size was {}", variable.getCallerIdNumber(), variable.getCallerIdNumber().length());
        dialplanRepository.LogDialplanInfoIntoDb("Checking caller id length {} size was {}", variable.getCallerIdNumber(), variable.getCallerIdNumber().length());

        if (variable.getCallerIdNumber().length() != "7147182832".length()) {
            log.info("Sending to IVR Identity. Caller ID is not full 10 digit number. ");
            dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Sending to IVR Identity. Caller ID is not full 10 digit number. ");
            WorkHours workHours = isWorkingHour(ado, destinationNumber);
            callDetailRecordService.updateIsOpen(TMS_UUID, workHours.isOpen());
            log.info(workHours.dump());
            dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Work Hours {}", workHours.dump());
            if (workHours.isOpen() == false) {
                DialplanBuilder incomingVoicemail;
                incomingVoicemail = new IncomingVoicemail(variable, ado, workHours, Boolean.FALSE);
                incomingVoicemail.setTMS_UUID(TMS_UUID);
                return incomingVoicemail;
            }
            if (configuration.getIVRVersion() == 2) {
                log.info("Sending to IVR Identity Version {}.", configuration.getIVRVersion());
                dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Sending to IVR Identity Version {}.", configuration.getIVRVersion());
                DialplanBuilder incomingIVRIdentity = new IncomingIVRIdentity2(variable);
                incomingIVRIdentity.setTMS_UUID(TMS_UUID);
                return incomingIVRIdentity;
            }
            DialplanBuilder incomingIVRIdentity = new IncomingIVRIdentity(variable);
            incomingIVRIdentity.setTMS_UUID(TMS_UUID);
            return incomingIVRIdentity;
        }

        if (variable.getCallerIdLong() != null) {
            ado = inboundCallService.inboundCallOrder(null, variable.getCallerIdLong(), TMS_UUID);
        } else {
            ado = inboundCallService.inboundCallOrder(null, 0, TMS_UUID);
        }

        WorkHours workHours = isWorkingHour(ado, destinationNumber);
        log.info(workHours.dump());
        dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Work Hours {}", workHours.dump());
        callDetailRecordService.updateIsOpen(TMS_UUID, workHours.isOpen());

        if (destinationNumber.startsWith("18") || destinationNumber.startsWith("8")) {
            log.info("Inbound call on (18|8)" + destinationNumber);
            dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Inbound call on (18|8)" + destinationNumber);
        } else {
            ado = directRouteProcess(variable, ado);
        }

        IncomingCallRouting callRouting = getIncomingCallRouting(variable, ado);

        //Closed options
        if (workHours.isOpen() == false) {
            return closedOption(callRouting, variable, ado, workHours, TMS_UUID);
        }

        switch (callRouting) {
            case SEND_TO_IVR_IDENTITY:
                if (configuration.getIVRVersion() == 2) {
                    log.info("Sending to IVR Identity Version {}.", configuration.getIVRVersion());
                    dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Sending to IVR Identity Version {}.", configuration.getIVRVersion());
                    DialplanBuilder incomingIVRIdentity = new IncomingIVRIdentity2(variable);
                    incomingIVRIdentity.setTMS_UUID(TMS_UUID);
                    return incomingIVRIdentity;
                }
                log.info("Sending to IVR Identity Version {}.", configuration.getIVRVersion());
                dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Sending to IVR Identity Version {}.", configuration.getIVRVersion());
                DialplanBuilder incomingIVRIdentity = new IncomingIVRIdentity(variable);
                incomingIVRIdentity.setTMS_UUID(TMS_UUID);
                return incomingIVRIdentity;

            case SEND_TO_HOLD:
                log.info("Sending to IncomingPlaceOnHold. ");
                dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Sending to IncomingPlaceOnHold. ");
                DialplanBuilder incomingPlaceOnHold = new IncomingPlaceOnHold(variable, ado);
                incomingPlaceOnHold.setTMS_UUID(TMS_UUID);
                return incomingPlaceOnHold;

            case SEND_TO_IVR:
                if (configuration.getIVRVersion() == 2) {
                    log.info("Sending to IVR {}.", configuration.getIVRVersion());
                    dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Sending to IVR {}.", configuration.getIVRVersion());
                    DialplanBuilder incomingIVR = new IncomingIVR2(variable, ado);
                    incomingIVR.setTMS_UUID(TMS_UUID);
                    return incomingIVR;
                }
                log.info("Sending to IVR {}.", configuration.getIVRVersion());
                dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Sending to IVR {}.", configuration.getIVRVersion());
                DialplanBuilder incomingIVR = new IncomingIVR(variable, ado);
                incomingIVR.setTMS_UUID(TMS_UUID);
                return incomingIVR;
            default:
                log.info("Sending to IncomingDialerOrder. ");
                dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Sending to IncomingDialerOrder. ");
                DialplanBuilder incomingDialerOrder = new IncomingDialerOrder(variable, ado);
                incomingDialerOrder.setTMS_UUID(TMS_UUID);
                return incomingDialerOrder;
        }
    }

    private DialplanBuilder closedOption(IncomingCallRouting callRouting, DialplanVariable variable, AgentIncomingDistributionOrder ado, WorkHours workHours, String TMS_UUID) {
        if (callRouting == IncomingCallRouting.SEND_TO_IVR && ado.getBorrowerInfo() != null) {
            try {

                boolean isGood = true;
//                if (isGood && ivrAchInformationPojo == null) {
//                    log.warn("Check ACH Condition {} - ivrAchInformationPojo is null", variable.getCall_uuid());
//                    dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Check ACH Condition {} - ivrAchInformationPojo is null", variable.getCall_uuid());
//                    isGood = false;
//                }
//                if (isGood && ivrAchInformationPojo.getAutoPaymentOption() != 1) {
//                    log.warn("Check ACH Condition {} - Auto payment option ACH is {}", variable.getCall_uuid(), ivrAchInformationPojo.getAutoPaymentOption());
//                    dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Check ACH Condition {} - Auto payment option ACH is {}", variable.getCall_uuid(), ivrAchInformationPojo.getAutoPaymentOption());
//                    isGood = false;
//                }
//                if (isGood && ivrAchInformationPojo.getPendingAchPk() > 0) {
//                    log.warn("Check ACH Condition {} - Pending ACH is {}", variable.getCall_uuid(), ivrAchInformationPojo.getPendingAchPk());
//                    dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Check ACH Condition {} - Pending ACH is {}", variable.getCall_uuid(), ivrAchInformationPojo.getPendingAchPk());
//                    isGood = false;
//                }
//                if (isGood && ivrAchInformationPojo.getAchAmount() == null) {
//                    log.warn("Check ACH Condition {} - ivrAchInformationPojo AchAmount is null", variable.getCall_uuid());
//                    dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Check ACH Condition {} - ivrAchInformationPojo AchAmount is null", variable.getCall_uuid());
//                    isGood = false;
//                }
                if (isGood && configuration.getIVRVersion() == 2) {
                    log.info("Sending to IVR {}.", configuration.getIVRVersion());
                    dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Sending to IVR {}.", configuration.getIVRVersion());
                    DialplanBuilder incomingIVR = new IncomingIVR2AfterHour(variable, ado, workHours);
                    incomingIVR.setTMS_UUID(TMS_UUID);
                    return incomingIVR;
                }
            } catch (Exception ex) {

            }

        }
        DialplanBuilder incomingVoicemail;
        incomingVoicemail = new IncomingVoicemail(variable, ado, workHours, Boolean.FALSE);
        incomingVoicemail.setTMS_UUID(TMS_UUID);
        return incomingVoicemail;
    }

    private IncomingCallRouting getIncomingCallRouting(DialplanVariable variable, AgentIncomingDistributionOrder ado) {

        log.info("ado: {}", ado.toJson());
        dialplanRepository.LogDialplanInfoIntoDb(variable.getCall_uuid(), "ado: {}", ado.toJson());

        Long loanId = ado.getBorrowerInfo().getLoanId();

        log.info("LoanId: {}, isHasMultipleMatches: {}", loanId, ado.getCallDetails().isHasMultipleMatches());
        dialplanRepository.LogDialplanInfoIntoDb(variable.getCall_uuid(), "LoanId: {}, isHasMultipleMatches: {}", loanId, ado.getCallDetails().isHasMultipleMatches());

        if (ado.isDirectLine()) {
            log.info("Sending to IncomingDialerOrder. ");
            dialplanRepository.LogDialplanInfoIntoDb(variable.getCall_uuid(), "Sending to IncomingDialerOrder. ");
            return IncomingCallRouting.SEND_TO_AGENT;
        }

        if (loanId == null || ado.getCallDetails().isHasMultipleMatches()) {
            log.info("Sending to IVR Identity. ");
            dialplanRepository.LogDialplanInfoIntoDb(variable.getCall_uuid(), "Sending to IVR Identity. ");
            return IncomingCallRouting.SEND_TO_IVR_IDENTITY;
        }

        //BasicLoanInformationPojo blip = null;
        //This logic is also in the IVR it should 
        if (false) {//} else if (ado.getCallDetails().getLoanServicingStatus() == 1) {
            log.info("Sending to IVR. ");
            dialplanRepository.LogDialplanInfoIntoDb(variable.getCall_uuid(), "Sending to IVR. ");
            return IncomingCallRouting.SEND_TO_IVR;
        }

        if (ado.getAgents().isEmpty() && loanId != null) {
            log.info("Sending to IncomingPlaceOnHold. ");
            dialplanRepository.LogDialplanInfoIntoDb(variable.getCall_uuid(), "Sending to IncomingPlaceOnHold. ");
            return IncomingCallRouting.SEND_TO_HOLD;
        }

        log.info("Sending to IncomingDialerOrder. ");
        dialplanRepository.LogDialplanInfoIntoDb(variable.getCall_uuid(), "Sending to IncomingDialerOrder. ");
        return IncomingCallRouting.SEND_TO_AGENT;
    }

    public boolean isNotDelinquent() {

        return true;

    }

    private AgentIncomingDistributionOrder directRouteProcess(DialplanVariable variable, AgentIncomingDistributionOrder ado) {

        String last4OfCallee = variable.getCalleeIdNumber();

        dialplanRepository.LogDialplanInfoIntoDb(variable.getCall_uuid(), "last 40f callee {}", last4OfCallee);
        if (last4OfCallee != null && last4OfCallee.length() > 4) {
            last4OfCallee = last4OfCallee.substring(last4OfCallee.length() - 4);
        }
        dialplanRepository.LogDialplanInfoIntoDb(variable.getCall_uuid(), "last 40f callee {}", last4OfCallee);

        Integer ext = 0;
        try {
            ext = Integer.parseInt(last4OfCallee);
        } catch (NumberFormatException e) {
            return ado;
        }
        dialplanRepository.LogDialplanInfoIntoDb(variable.getCall_uuid(), "last 40f callee {}", ext);
        User user = amsService.getUser(ext);
        if (user == null) {
            dialplanRepository.LogDialplanInfoIntoDb(variable.getCall_uuid(), "user was null {}", ext);
            return ado;
        }

        if (user.isDirectRoute() != null && user.isDirectRoute()) {
            dialplanRepository.LogDialplanInfoIntoDb(variable.getCall_uuid(), "user.isDirectRoute() {}", user.isDirectRoute());
            Boolean multiLine = user.isMultiLine();
            if (multiLine == null) {
                multiLine = Boolean.FALSE;
            }
            if (inboundCallService.shouldRecieveCall(ext, multiLine, CallDirection.INBOUND, false)) {
                ado.addAgentOnTop(agentService.getAgent(ext), multiLine, "Directline");
                ado.setDirectLine(true);
            }
        }
        return ado;
    }

    public WorkHours isWorkingHour(AgentIncomingDistributionOrder aido, String destinationNumber) {
        WorkHours globalWorkingHours = isWorkingHourGlobal(destinationNumber);
        if (globalWorkingHours.isOpen() == false) {
            log.info("GLOBAL Working ours for {} is false. So we are closed.", destinationNumber);
            return globalWorkingHours;
        }
        if (aido == null || aido.getSettings() == null) {
            return new WorkHours(LocalTime.MIDNIGHT, LocalTime.now().plusHours(2));
        }
        //return true;

        int day = LocalDate.now().getDayOfWeek();
        if (day == 7) {
            day = 0;
        }
        return isWorkingHourGlobal(destinationNumber);
//        return isWorkingHour(aido.getSettings().getDialerSchedule().get(day).getStartTime(), aido.getSettings().getDialerSchedule().get(day).getEndTime());

        //return isWorkingHour(aido.getSettings().getStartTime(), aido.getSettings().getEndTime());
    }

    public WorkHours isWorkingHour(LocalTime start, LocalTime end) {
        WorkHours working = new WorkHours(start, end);
        if (working.isOpen() == false) {
            log.info("Working ours is false. So we are closed {} ", working.getStart().getHourOfDay());
        }
        return working;
    }

    public WorkHours isWorkingHourGlobal(String destinationNumber) {
        log.info("Today is {}", LocalDate.now().getDayOfWeek());
        LocalTime start = new LocalTime(configuration.getStartWorkingHourGlobal(LocalDate.now().getDayOfWeek(), destinationNumber), configuration.getStartWorkingMinuteOfHourGlobal(LocalDate.now().getDayOfWeek(), destinationNumber), 0);
        LocalTime end = new LocalTime(configuration.getEndWorkingHourGlobal(LocalDate.now().getDayOfWeek(), destinationNumber), configuration.getEndWorkingMinuteOfHourGlobal(LocalDate.now().getDayOfWeek()), 0);
        WorkHours workHours = new WorkHours(start, end);
        return workHours;
    }
}
