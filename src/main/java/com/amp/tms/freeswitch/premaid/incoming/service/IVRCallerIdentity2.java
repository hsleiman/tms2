/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.incoming.service;

import com.amp.crm.embeddable.InboundDialerQueueRecord;
import com.amp.crm.service.dialer.DialerQueueService;
import com.amp.crm.service.tms.TMSService;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.RecordedPhrases;
import com.amp.tms.enumerated.refrence.HOLDOrder;
import com.amp.tms.enumerated.refrence.IVROrder2;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.Answer;
import com.amp.tms.freeswitch.dialplan.action.BridgeToAgent;
import com.amp.tms.freeswitch.dialplan.action.BridgeToFifo;
import com.amp.tms.freeswitch.dialplan.action.PlayAndGetDigits;
import com.amp.tms.freeswitch.dialplan.action.Playback;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.dialplan.action.Sleep;
import com.amp.tms.freeswitch.dialplan.action.TMSOrder;
import com.amp.tms.freeswitch.dialplan.action.Transfer;
import com.amp.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.incoming.IncomingDialerOrder;
import com.amp.tms.freeswitch.premaid.incoming.IncomingPlaceOnHold;
import com.amp.tms.service.TMSAgentService;
import com.amp.tms.service.CallDetailRecordService;
import com.amp.tms.service.DialerQueueRecordService;
import com.amp.tms.service.DialplanService;
import com.amp.tms.service.DncService;
import com.amp.tms.service.FreeswitchConfiguration;
import com.amp.tms.service.FreeswitchService;
import com.amp.tms.service.InboundCallService;
import com.amp.tms.service.TextToSpeechService;
import com.amp.tms.service.freeswitch.common.Incoming2;
import java.util.List;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service("IVRCallerIdentity2")
public class IVRCallerIdentity2 {

    protected final static Logger log = LoggerFactory.getLogger(IVRCallerIdentity2.class);

    @Autowired
    private TMSService tmsIWS;
    
    @Autowired
    private DialerQueueService dialerQueueService;
    
    @Autowired
    private DncService dnc;

    @Autowired
    private Incoming2 incoming;

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private TMSAgentService agentService;

    @Autowired
    private DialplanService dialplanRepository;

    @Autowired
    private DialerQueueRecordService dialerQueueRecordRepository;

    @Autowired
    private TextToSpeechService textToSpeechService;

    @Autowired
    private InboundCallService inboundCallService;
    
    @Autowired
    private FreeswitchService freeswitchService;

    public TMSDialplan AskForSSNOrLoan(DialplanVariable variable, TMSDialplan tmsDialplan) {
        if (tmsDialplan.getCounter() > 4) {
            sendToCustomerServiceFifo(variable, tmsDialplan, false, 1);
            return tmsDialplan;
        }
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));
        tmsDialplan.setOnce(Boolean.FALSE);
        String fileToPlay = RecordedPhrases.IF_YOU_KNOW_YOUR_LOAN_ID_PLEASE_PRESS_1_TO_ENTER_BY_SOCIAL_SECURITY_NUMBER_PLEASE_PRESS_2_TO_SPEAK_TO_A_CUSTOMER_SERVICE_REPRESENTATIVE_PLEASE_PRESS_0_AT_ANY_TIME.getAudioPath();
        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 1, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d+", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        //tmsDialplan.addAction(new PlayAndGetDigits(4, 4, 1, 10000, "#*", fileToPlay, invalidToPlay, "SSN_ID", "\\d{4}", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + OptionTextParser.IVR2_SSN_OR_LOANID + "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new TMSOrder(IVROrder2.CHECK_FOR_SSN_OR_LOAN));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan CheckForSSNOrLoan(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        if (variable.getOptionSelectedId() == 1) {
            tmsDialplan.addAction(new TMSOrder(IVROrder2.ASK_FOR_LOAN));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        } else if (variable.getOptionSelectedId() == 2) {
            tmsDialplan.addAction(new TMSOrder(IVROrder2.ASK_FOR_SSN));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        } else {
            sendToCustomerServiceFifo(variable, tmsDialplan, false, 2);
            return tmsDialplan;
        }

        return tmsDialplan;
    }

    public TMSDialplan AskForLoan(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        if (tmsDialplan.getCounter() > 4) {
            sendToCustomerServiceFifo(variable, tmsDialplan, false, 3);
            return tmsDialplan;
        }
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(502l));
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.addAction(new TMSOrder(IVROrder2.ASK_FOR_LOAN));
        String fileToPlay = RecordedPhrases.PLEASE_ENTER_YOUR_LOAN_NUMBER.getAudioPath();
        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(3, 9, 1, 20000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d+", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        //tmsDialplan.addAction(new PlayAndGetDigits(4, 4, 1, 10000, "#*", fileToPlay, invalidToPlay, "SSN_ID", "\\d{4}", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + OptionTextParser.IVR2_LOANID + "${OPTION_SELECTED}"));

        tmsDialplan.addAction(new TMSOrder(IVROrder2.ASK_FOR_DOB));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        return tmsDialplan;

    }

    public TMSDialplan AskForSSN(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        if (tmsDialplan.getCounter() > 4) {
            sendToCustomerServiceFifo(variable, tmsDialplan, false, 4);
            return tmsDialplan;
        }
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(500l));
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.addAction(new TMSOrder(IVROrder2.ASK_FOR_SSN));
        String fileToPlay = RecordedPhrases.PLEASE_ENTER_THE_LAST_FOUR_DIGITS_OF_THE_SOCIAL_SECURITY_NUMBER_FOR_THIS_LOA.getAudioPath();
        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(4, 4, 1, 120000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d+", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.ssn_id, "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + OptionTextParser.IVR2_SSN + "${OPTION_SELECTED}"));

        tmsDialplan.addAction(new TMSOrder(IVROrder2.ASK_FOR_DOB));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        return tmsDialplan;

    }

    public TMSDialplan AskForDOB(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        if (tmsDialplan.getCounter() > 4) {
            sendToCustomerServiceFifo(variable, tmsDialplan, false, 5);
            return tmsDialplan;
        }
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(500l));
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.addAction(new TMSOrder(IVROrder2.ASK_FOR_DOB));
        String fileToPlay = RecordedPhrases.FOR_SECURITY_PURPOSES_PLEASE_ENTER_YOUR_DATE_OF_BIRTH_PLEASE_ENTER_THE_2_DIGITS_MONTH_2_DIGITS_DAY_AND_2_DIGITS_YEAR_FOR_EXAMPLE_FOR_JANUARY_13_1980_YOU_WOULD_ENTER_011380.getAudioPath();
        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();

        tmsDialplan.addAction(new PlayAndGetDigits(6, 6, 3, 10000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d{6}", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + OptionTextParser.IVR2_DOB + "${OPTION_SELECTED}"));

        tmsDialplan.addAction(new TMSOrder(IVROrder2.CHECK_SECURITY_FOR_DOB));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan CheckSecurityForDOB(DialplanVariable variable, TMSDialplan tmsDialplan) {

        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        OptionTextParser optionTextParser = new OptionTextParser(variable.getOptionText());

        LocalDate DOB = optionTextParser.getDOB();
        if (DOB == null) {
            log.info("DOB was null for CallUUID {}", variable.getCall_uuid());
            sendToCustomerServiceFifo(variable, tmsDialplan, false, 6);
            return tmsDialplan;
        }

//        IvrInformationVerificationPojo informationVerificationPojo = null;
////        if (optionTextParser.getSSNOrLoan() == 1) {
////            Long loanPk = optionTextParser.getLoanId();
////            informationVerificationPojo = tmsIWS.getLoanByLoanPkAndDob(loanPk, DOB);
////
////        } else if (optionTextParser.getSSNOrLoan() == 2) {
////            String ssn = optionTextParser.getSSN();
////            try {
////                informationVerificationPojo = tmsIWS.getLoanBySsnAndDob(ssn, DOB);
////            } catch (Throwable ex) {
////                log.info("CallUUID {} was null for Exception {}", variable.getCall_uuid(), ex);
////                sendToCustomerServiceFifo(variable, tmsDialplan, false, 6);
////                return tmsDialplan;
////            }
////
////        }
//
//        if (informationVerificationPojo == null) {
//            log.info("Checked CallUUI {} - informationVerificationPojo is null", variable.getCall_uuid());
//        }
//
//        if (informationVerificationPojo == null || informationVerificationPojo.isMultipleResults()) {
//            if (informationVerificationPojo != null) {
//                log.info("Checked CallUUI {} - Multiple Results {}", variable.getCall_uuid(), informationVerificationPojo.isMultipleResults());
//            }
//            sendToCustomerServiceFifo(variable, tmsDialplan, false, 7);
//            return tmsDialplan;
//        }
//
//        log.info("Checked CallUUI {} - Correct Information {} LoanId {}", variable.getCall_uuid(), informationVerificationPojo.isCorrectInformation(), informationVerificationPojo.getLoanPk());
//
//        if (informationVerificationPojo.getLoanPk() != null && informationVerificationPojo.isCorrectInformation()) {
//            tmsDialplan.getBorrowerInfo().setLoanId(informationVerificationPojo.getLoanPk());
//            callDetailRecordService.updateIVRAuthorized(variable.getCall_uuid(), Boolean.TRUE);
//            tmsDialplan.addAction(new TMSOrder(IVROrder2.VERIFIED_SECURITY_FOR_DOB));
//            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
//        } else {
            callDetailRecordService.updateIVRAuthorized(variable.getCall_uuid(), Boolean.FALSE);
            sendToCustomerServiceFifo(variable, tmsDialplan, false, 8);
            return tmsDialplan;
//        }

//        return tmsDialplan;
    }

    public TMSDialplan VerifiedSecurityForDOB(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        callDetailRecordService.updateLoanId(variable.getCall_uuid(), variable.getLoanId());

        log.info("VerifiedSecurityForDOB: CallUUID: {} CallerTMSDialPlan: {} CallerTMSDialPlan: {} LoanID: {}", variable.getCall_uuid(), tmsDialplan.getCallerLong(), variable.getCallerIdLong(), variable.getLoanId());

        AgentIncomingDistributionOrder aido;
        if (tmsDialplan.getCallerLong() != null) {
            aido = inboundCallService.inboundCallOrder(null, variable.getCallerIdLong(), variable.getCall_uuid(), variable.getLoanId());
        } else {
            aido = inboundCallService.inboundCallOrder(null, 0l, variable.getCall_uuid(), variable.getLoanId());
        }

        
        if (aido.getAgents().isEmpty() && aido.getBorrowerInfo().getLoanId() != null) {
            IncomingPlaceOnHold builder = new IncomingPlaceOnHold(variable, aido);
            builder.setTMS_UUID(tmsDialplan.getKey().getTms_uuid());
            TMSDialplan onHoldDialplan = builder.callEnteringFifo();
            tmsDialplan.addAction(new TMSOrder(onHoldDialplan.getKey().getOrderPower()));
            tmsDialplan.addBridge(new BridgeToFifo(FreeswitchContext.ivr_dp, freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));
        } else {
            IncomingDialerOrder builder = new IncomingDialerOrder(variable, aido, true);
            builder.setTMS_UUID(tmsDialplan.getKey().getTms_uuid());
            TMSDialplan connectToAgentDialplan = builder.buildDialplansWithoutSBC();
            if (connectToAgentDialplan != null) {
                tmsDialplan.addAction(new TMSOrder(connectToAgentDialplan.getKey().getOrderPower()));
                tmsDialplan.addBridge(new BridgeToAgent(agentService.getFreeswitchIPForExt(connectToAgentDialplan.getCalleeInteger()), connectToAgentDialplan.getCalleeInteger()));
            } else {
                sendToCustomerServiceFifo(variable, tmsDialplan, true, 9);
            }
        }

        return tmsDialplan;
    }

    private void sendToCustomerServiceFifo(DialplanVariable variable, TMSDialplan tmsDialplan, boolean isBorrowerKnown, Integer lastTransferIvr) {

        tmsDialplan.addAction(new Sleep(500l));
        tmsDialplan.addAction(new Playback((RecordedPhrases.PLEASE_WAIT_FOR_NEXT_AGENT)));

        callDetailRecordService.updateLastTransferStep(tmsDialplan.getCall_uuid(), lastTransferIvr);

        Long qPk = 1l;
        try {
            InboundDialerQueueRecord record = dialerQueueService.getDefaultInboundQueueRecord();
            dialerQueueRecordRepository.storeInboundDialerQueueRecord(record);
            qPk = record.getDqPk();
        } catch (Exception ex) {
            log.error("This is error in calling defaul inbound queue: {}", ex);
        }

        AgentIncomingDistributionOrder aido = null;
        if (tmsDialplan.getCallerLong() == null) {
            aido = inboundCallService.inboundCallOrderDefult(qPk, tmsDialplan.getCall_uuid());
        } else {
            aido = inboundCallService.inboundCallOrder(qPk, tmsDialplan.getCallerLong(), tmsDialplan.getCall_uuid());
        }
        log.info("Agent Size: {} aido: ", aido.getAgents().size(), aido.toJson());
        if (aido.getAgents().isEmpty()) {
            tmsDialplan.addAction(Set.create(FreeswitchVariables.ivr_authorized, isBorrowerKnown));
            tmsDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_ON_HOLD_CUSTOMER_SERVICE));
            tmsDialplan.addBridge(new BridgeToFifo(FreeswitchContext.ivr_dp, freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));
        } else {
            IncomingDialerOrder builder = new IncomingDialerOrder(variable, aido, isBorrowerKnown);
            builder.setTMS_UUID(tmsDialplan.getKey().getTms_uuid());
            TMSDialplan connectToAgentDialplan = builder.buildDialplansWithoutSBC();
            tmsDialplan.addAction(new TMSOrder(connectToAgentDialplan.getKey().getOrderPower()));
            tmsDialplan.addBridge(new BridgeToAgent(freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.agent_dp), connectToAgentDialplan.getCalleeInteger()));
        }
    }

    private void playbackEnteredNumber(TMSDialplan tmsDialplan, String value, Boolean includeYouEntered) {
        List<Integer> digitEntered = textToSpeechService.getNumberSinglePatern(value);
        if (digitEntered != null && digitEntered.isEmpty() == false) {
            if (includeYouEntered) {
                tmsDialplan.addAction(new Playback((RecordedPhrases.YOU_ENTERED)));
            }
            for (Integer get : digitEntered) {
                tmsDialplan.addAction(new Playback(configuration.getRecordingFile(get)));
            }
        }
    }
}
