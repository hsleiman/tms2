/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.freeswitch.common;

import com.objectbrains.ams.iws.User;
import com.objectbrains.sti.pojo.TMSBasicAccountInfo;
import com.objectbrains.svc.iws.IvrAchInformationPojo;
import com.objectbrains.svc.iws.TMSService;
import com.objectbrains.svc.iws.TmsBasicLoanInfo;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.IncomingCallRouting;
import com.objectbrains.tms.enumerated.WorkHours;
import com.objectbrains.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingDialerOrder;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingIVR;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingIVR2;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingIVR2AfterHour;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingIVRIdentity;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingIVRIdentity2;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingPlaceOnHold;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingVoicemail;
import com.objectbrains.tms.freeswitch.premaid.local.P1AgentToP2Agent;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.service.AgentService;
import com.objectbrains.tms.service.AgentStatsService;
import com.objectbrains.tms.service.AmsService;
import com.objectbrains.tms.service.CallDetailRecordService;
import com.objectbrains.tms.service.DialplanService;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import com.objectbrains.tms.service.InboundCallService;
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
public class Incoming2 {

    @Autowired
    private InboundCallService inboundCallService;

    @Autowired
    private TMSService tmsIWS;

    @Autowired
    private AmsService amsService;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentStatsService agentStatsService;

    @Autowired
    private DialplanService dialplanRepository;

    private static final Logger log = LoggerFactory.getLogger(Incoming2.class);

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    public DialplanBuilder getDialplanBuilderSBC(DialplanVariable variable) {

        AgentIncomingDistributionOrder ado = null;
//
//       if (variable.getCalleeIdNumber().endsWith("8889")) {
//            log.info("Load test. ");
//            return (new IncomingBlackhole(variable));
//        }

        if(variable.getCallerIdNumber().equalsIgnoreCase("7147182832")){
            
        }


        String TMS_UUID = UUID.randomUUID().toString();
        String destinationNumber = variable.getCalleeIdNumber();
        callDetailRecordService.updateInboundDIDNumber(TMS_UUID, destinationNumber);

        if (destinationNumber != null && destinationNumber.length() == "221711".length() && destinationNumber.startsWith("22")) {
            try {
                Integer agentExt = Integer.parseInt(destinationNumber.substring(2));
                log.info("Finding Agent : {}", agentExt);
                Agent agent = agentService.getAgent(agentExt);
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

        long queuePk = tmsIWS.getDialerQueuePkForPhoneNumber(destinationNumber);
        log.info("Checking Distination {} is in queue {}", variable.getCalleeIdLong(), queuePk);
        dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Checking Distination {} is in queue {}", variable.getCalleeIdLong(), queuePk);

        if (queuePk != -1) {
            callDetailRecordService.updateQueueId(TMS_UUID, queuePk);
            ado = inboundCallService.inboundCallOrder(queuePk, variable.getCallerIdLong(), TMS_UUID);

            if (configuration.enableDirectRoutingForInboundDistinationNumber()) {
                ado = directRouteProcess(TMS_UUID, variable, ado);
            }

            log.info("Sending to IncomingDialerOrder. Distination was {}", variable.getCalleeIdLong());
            dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Sending to IncomingDialerOrder. Distination was {}", variable.getCalleeIdLong());
            WorkHours workHours = isWorkingHour(ado, destinationNumber, TMS_UUID);
            callDetailRecordService.updateIsOpen(TMS_UUID, workHours.isOpen());
            log.info(workHours.dump());
            if (workHours.isOpen() == false) {
                DialplanBuilder incomingVoicemail;
                incomingVoicemail = new IncomingVoicemail(variable, ado, workHours, Boolean.FALSE);
                incomingVoicemail.setTMS_UUID(TMS_UUID);
                return incomingVoicemail;
            }
            
            log.info("ado.getAgents().isEmpty() {}", ado.getAgents().isEmpty());
            if (ado.getAgents().isEmpty() == false) {
                DialplanBuilder incomingDialerOrder = new IncomingDialerOrder(variable, ado);
                incomingDialerOrder.setTMS_UUID(TMS_UUID);
                return incomingDialerOrder;
            }
            
            log.info("ado.getSettings() {}", ado.getSettings());
            if(ado.getSettings() != null && ado.getSettings().isForceVoicemail()) {
                log.info("ado.getSettings().isForceVoicemail() {}", ado.getSettings().isForceVoicemail());
                DialplanBuilder incomingPlaceOnHold = new IncomingPlaceOnHold(variable, ado);
                incomingPlaceOnHold.setTMS_UUID(TMS_UUID);
                return incomingPlaceOnHold;
            }
        }

        log.info("Checking caller id length {} size was {}", variable.getCallerIdNumber(), variable.getCallerIdNumber().length());
        dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Checking caller id length {} size was {}", variable.getCallerIdNumber(), variable.getCallerIdNumber().length());

        if (variable.getCallerIdLong() != null) {
            ado = inboundCallService.inboundCallOrder(null, variable.getCallerIdLong(), TMS_UUID);
        } else {
            ado = inboundCallService.inboundCallOrder(null, 0, TMS_UUID);
        }

        if (variable.getCallerIdNumber().length() != "7147182832".length()) {
            log.info("Sending to IVR Identity. Caller ID is not full 10 digit number. ");
            dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Sending to IVR Identity. Caller ID is not full 10 digit number. ");
            WorkHours workHours = isWorkingHour(ado, destinationNumber, TMS_UUID);
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

        WorkHours workHours = isWorkingHour(ado, destinationNumber, TMS_UUID);
        log.info("Work Hours {}", workHours.dump());
        dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Work Hours {}", workHours.dump());
        callDetailRecordService.updateIsOpen(TMS_UUID, workHours.isOpen());

        if (destinationNumber.startsWith("18") || destinationNumber.startsWith("8")) {
            log.info("Inbound call on (18|8)" + destinationNumber);
            dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Inbound call on (18|8)" + destinationNumber);
        } else {
            ado = directRouteProcess(TMS_UUID, variable, ado);
        }

        IncomingCallRouting callRouting = getIncomingCallRouting(variable, ado);
        log.info("IncomingCallRouting: {} - TMS_UUID {}", callRouting, TMS_UUID);
        dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "IncomingCallRouting: {}", callRouting);

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

                boolean isBorrowerKnown = false;
                if (ado.getCallDetails().isHasMultipleMatches() == false && configuration.enableHasMutlipleMatchesForInboundCallingCheck()) {
                    log.info("Setting borrower to known ado.getCallDetails().isHasMultipleMatches() == false - {}", TMS_UUID);
                    dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Setting borrower to known ado.getCallDetails().isHasMultipleMatches() == false");
                    isBorrowerKnown = true;
                }

                DialplanBuilder incomingDialerOrder = new IncomingDialerOrder(variable, ado, isBorrowerKnown);
                incomingDialerOrder.setTMS_UUID(TMS_UUID);
                return incomingDialerOrder;
        }
    }

    private DialplanBuilder closedOption(IncomingCallRouting callRouting, DialplanVariable variable, AgentIncomingDistributionOrder ado, WorkHours workHours, String TMS_UUID) {
        if (callRouting == IncomingCallRouting.SEND_TO_IVR && ado.getBorrowerInfo() != null) {
            try {
                IvrAchInformationPojo ivrAchInformationPojo = null;
                ivrAchInformationPojo = tmsIWS.getAchDayAndAmountForLoan(ado.getBorrowerInfo().getLoanId());
                boolean isGood = true;
                if (isGood && ivrAchInformationPojo == null) {
                    log.warn("Check ACH Condition {} - ivrAchInformationPojo is null", variable.getCall_uuid());
                    dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Check ACH Condition {} - ivrAchInformationPojo is null", variable.getCall_uuid());
                    isGood = false;
                }
                if (isGood && ivrAchInformationPojo.getAutoPaymentOption() != 1) {
                    log.warn("Check ACH Condition {} - Auto payment option ACH is {}", variable.getCall_uuid(), ivrAchInformationPojo.getAutoPaymentOption());
                    dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Check ACH Condition {} - Auto payment option ACH is {}", variable.getCall_uuid(), ivrAchInformationPojo.getAutoPaymentOption());
                    isGood = false;
                }
                if (isGood && ivrAchInformationPojo.getPendingAchPk() > 0) {
                    log.warn("Check ACH Condition {} - Pending ACH is {}", variable.getCall_uuid(), ivrAchInformationPojo.getPendingAchPk());
                    dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Check ACH Condition {} - Pending ACH is {}", variable.getCall_uuid(), ivrAchInformationPojo.getPendingAchPk());
                    isGood = false;
                }
                if (isGood && ivrAchInformationPojo.getAchAmount() == null) {
                    log.warn("Check ACH Condition {} - ivrAchInformationPojo AchAmount is null", variable.getCall_uuid());
                    dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Check ACH Condition {} - ivrAchInformationPojo AchAmount is null", variable.getCall_uuid());
                    isGood = false;
                }
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
        TmsBasicLoanInfo basicLoanInfo = null;
        basicLoanInfo = tmsIWS.getBasicLoanInfoForTMS(loanId);
        //This logic is also in the IVR it should 
        if (isNotDelinquent(basicLoanInfo)) {//} else if (ado.getCallDetails().getLoanServicingStatus() == 1) {
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

    public boolean isNotDelinquent(TMSBasicAccountInfo blip) {
        try {
            if (blip != null) {
                log.info("Is Not Delinquent: {} - {} - {}", blip.getAccountPk(), blip.getNextDueDate());
                dialplanRepository.LogDialplanInfoIntoDb("", "Is Not Delinquent: {} - {} - {}", blip.getAccountPk(), blip.getNextDueDate());
                if (blip.getNextDueDate() != null) {
                    log.info("Is Not Delinquent: {} - {}", blip.getAccountPk(), blip.getNextDueDate().isAfter(LocalDate.now()));
                    dialplanRepository.LogDialplanInfoIntoDb("", "Is Not Delinquent: {} - {}", blip.getAccountPk(), blip.getNextDueDate().isAfter(LocalDate.now()));
                }
            }
            return blip != null && blip.getNextDueDate().isAfter(LocalDate.now());
        } catch (Exception ex) {
            log.error("Exception Is Not Delinquent: TMS basic loan info {}", blip == null, ex);
            dialplanRepository.LogDialplanInfoIntoDb("", "Exception Is Not Delinquent: TMS basic loan info {}", blip == null);
            return true;
        }
    }

    private AgentIncomingDistributionOrder directRouteProcess(String TMS_UUID, DialplanVariable variable, AgentIncomingDistributionOrder ado) {

        String last4OfCallee = variable.getCalleeIdNumber();

        dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "last 40f callee {}", last4OfCallee);
        if (last4OfCallee != null && last4OfCallee.length() > 4) {
            last4OfCallee = last4OfCallee.substring(last4OfCallee.length() - 4);
        }
        dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "last 40f callee {}", last4OfCallee);

        Integer ext = 0;
        try {
            ext = Integer.parseInt(last4OfCallee);
        } catch (NumberFormatException e) {
            return ado;
        }
        dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "last 40f callee {}", ext);
        User user = amsService.getUser(ext);
        if (user == null) {
            dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "user was null {}", ext);
            return ado;
        }

        if (user.isDirectRoute() != null && user.isDirectRoute()) {
            dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "user.isDirectRoute() {}", user.isDirectRoute());
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

    public WorkHours isWorkingHour(AgentIncomingDistributionOrder aido, String destinationNumber, String TMS_UUID) {
        WorkHours globalWorkingHours = isWorkingHourGlobal(destinationNumber);
        if (globalWorkingHours.isOpen() == false) {
            log.info("GLOBAL Working ours for {} is false. So we are closed.", destinationNumber);
            dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "GLOBAL Working ours for {} is false. So we are closed.", destinationNumber);
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

        return isWorkingHour(aido.getSettings().getDialerSchedule().get(day).getStartTime(), aido.getSettings().getDialerSchedule().get(day).getEndTime(), TMS_UUID);

        //return isWorkingHour(aido.getSettings().getStartTime(), aido.getSettings().getEndTime());
    }

    public WorkHours isWorkingHour(LocalTime start, LocalTime end, String TMS_UUID) {
        WorkHours working = new WorkHours(start, end);
        if (working.isOpen() == false) {
            log.info("Working ours is false. So we are closed {} ", working.getStart().getHourOfDay());
            dialplanRepository.LogDialplanInfoIntoDb(TMS_UUID, "Working ours is false. So we are closed {} ", working.getStart().getHourOfDay());
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
