/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.incoming.service;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.amp.crm.pojo.TMSCallDetails;
import com.amp.crm.service.tms.TMSService;
import com.amp.tms.db.entity.cdr.CallDetailRecordTMS;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.RecordedPhrases;
import com.amp.tms.enumerated.RecordedWords;
import com.amp.tms.enumerated.refrence.IVROrder;
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
import com.amp.tms.service.FreeswitchConfiguration;
import com.amp.tms.service.FreeswitchService;
import com.amp.tms.service.InboundCallService;
import com.amp.tms.service.TextToSpeechService;
import com.amp.tms.service.freeswitch.common.Incoming2;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service("IVRMain2AfterHour")
public class IVRMain2AfterHour {

    protected final static Logger log = LoggerFactory.getLogger(IVRMain2AfterHour.class);

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private TMSService tmsIWS;

    @Autowired
    private FreeswitchConfiguration freeswitchConfiguration;

    @ConfigContext
    private ConfigurationUtility config;

    @Autowired
    private TMSAgentService agentService;

    @Autowired
    private InboundCallService inboundCallService;

    @Autowired
    private DialerQueueRecordService dialerQueueRecordRepository;

    @Autowired
    private Incoming2 incoming;

    @Autowired
    private TextToSpeechService textToSpeechService;
    
    @Autowired
    private FreeswitchService freeswitchService;

    public TMSDialplan CheckSecurityForLoan(DialplanVariable variable, TMSDialplan tmsDialplan) {
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));
        tmsDialplan.setOnce(Boolean.FALSE);

        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());

        if (tmsDialplan.getCounter() > 2) {
            sendToCustomerServiceFifo(variable, tmsDialplan, false);
            return tmsDialplan;
        }

        if (variable.getOptionSelectedId() == null || (variable.getOptionSelectedId() != null && variable.getOptionSelectedId() != 2)) {
            sendToCustomerServiceFifo(variable, tmsDialplan, false);
            return tmsDialplan;
        }

        if (tmsDialplan.getCounter() == 1) {
            tmsDialplan.addAction(new Playback(RecordedPhrases.I_SEE_THE_PHONE_NUMBER_YOUR_CALLING_FROM_IN_OUR_SYSTEM));
        } else {
            tmsDialplan.addAction(new Playback(RecordedPhrases.SORRY_I_WAS_UNABLE_TO_VERIFY_THAT_ENTRY));
        }
        tmsDialplan.addAction(new Sleep(500l));
        String fileToPlay = RecordedPhrases.FOR_SECURITY_PURPOSES_PLEASE_ENTER_THE_LAST_4_DIGITS_OF_YOUR_SOCIAL_SECURITY_NUMBER.getAudioPath();
        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(1, 4, 1, 10000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d+", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.ssn_id, "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + OptionTextParser.IVR2_SSN + "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new TMSOrder(IVROrder2.VERIFIED_SECURITY_FOR_LOAN));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan VerifiedSecurityForLoan(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));
        tmsDialplan.setOnce(Boolean.FALSE);

        Long loanPk = tmsDialplan.getBorrowerInfo().getLoanId();
        String ssn = variable.getSsn();

        TMSCallDetails callDetails = null;

        try {
            callDetails = tmsIWS.getLoanInfoByLoanPk(loanPk);
        } catch (Exception ex) {
            log.error("SVC Exception {} - {}", variable.getCall_uuid(), ex);
            sendToCustomerServiceFifo(variable, tmsDialplan, false);
            return tmsDialplan;
        }

        if (callDetails.getSsn().endsWith(ssn)) {
            tmsDialplan.addAction(new TMSOrder(IVROrder2.ASK_ARE_YOU_CALLING_FOR_LOAN));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        } else {
            tmsDialplan.addAction(new TMSOrder(IVROrder2.CHECK_SECURITY_FOR_LOAN));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        }

        return tmsDialplan;
    }

    public TMSDialplan ASKAreYouCallingForLoan(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));
        tmsDialplan.setOnce(Boolean.FALSE);

        if (config.getBoolean("enable.prompt.for.verifing.loan.id.for.ivr", false)) {
            Long loanPk = tmsDialplan.getBorrowerInfo().getLoanId();
            if (loanPk == null) {
                loanPk = variable.getLoanId();
            }
            String temp = loanPk + "";
            if (temp.length() > 5) {
                temp = temp.substring(temp.length() - 5);
            }
            tmsDialplan.addAction(new Playback(RecordedPhrases.OK_ARE_CALLING_FOR_LOAN_ID_ENDING_IN));
            playbackEnteredNumber(tmsDialplan, temp, Boolean.FALSE);

            tmsDialplan.addAction(new Sleep(500l));
            String fileToPlay = RecordedPhrases.PRESS_1_IF_CORRECT.getAudioPath();
            String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
            tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 1, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d+", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
            tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
            tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + OptionTextParser.IVR2_ASKED_LOAN_CORRECT + "${OPTION_SELECTED}"));
        } else {
            tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "1"));
        }
        tmsDialplan.addAction(new TMSOrder(IVROrder2.VERIFIED_CORRECT_LOAN_ID));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan VerifiedCorrectLoanId(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));
        tmsDialplan.setOnce(Boolean.FALSE);

        if (variable.getOptionSelectedId() == 1) {
            tmsDialplan.addAction(new TMSOrder(IVROrder2.CHECK_PORTFOLIO_CONDITION));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        } else {
            sendToCustomerServiceFifo(variable, tmsDialplan, false);
            return tmsDialplan;
        }
        return tmsDialplan;
    }

    public TMSDialplan CheckProtfolioCondition(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));
        tmsDialplan.setOnce(Boolean.FALSE);

        Long loanPk = tmsDialplan.getBorrowerInfo().getLoanId();
        if(loanPk == null){
            loanPk = variable.getLoanId();
        }

        AgentIncomingDistributionOrder aido;
        if (tmsDialplan.getCallerLong() != null) {
            aido = inboundCallService.inboundCallOrder(null, tmsDialplan.getCallerLong(), tmsDialplan.getCall_uuid(), loanPk);
        } else {
            aido = inboundCallService.inboundCallOrder(null, 0l, tmsDialplan.getCall_uuid(), loanPk);
        }
       

        boolean notDelinquent = false;
        log.info("incoming.isNotDelinquent: {}", notDelinquent);
        log.info("aido: " + aido.toJson());
        

        if (notDelinquent) {
            log.info("incoming.isNotDelinquent: {}", notDelinquent);
            tmsDialplan.addAction(new TMSOrder(IVROrder2.CHECK_ACH_CONDITION));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        } else if (aido.getAgents().isEmpty() && aido.getBorrowerInfo().getLoanId() != null) {
            IncomingPlaceOnHold builder = new IncomingPlaceOnHold(variable, aido);
            builder.setTMS_UUID(tmsDialplan.getKey().getTms_uuid());
            TMSDialplan onHoldDialplan = builder.callEnteringFifo();
            tmsDialplan.addAction(new TMSOrder(onHoldDialplan.getKey().getOrderPower()));
            tmsDialplan.addBridge(new BridgeToFifo(FreeswitchContext.ivr_dp, freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));

        } else {
            IncomingDialerOrder builder = new IncomingDialerOrder(variable, aido);
            builder.setTMS_UUID(tmsDialplan.getKey().getTms_uuid());
            TMSDialplan connectToAgentDialplan = builder.buildDialplansWithoutSBC();
            tmsDialplan.addAction(new TMSOrder(connectToAgentDialplan.getKey().getOrderPower()));
            tmsDialplan.addBridge(new BridgeToAgent(agentService.getFreeswitchIPForExt(connectToAgentDialplan.getCalleeInteger()), connectToAgentDialplan.getCalleeInteger()));
        }

        return tmsDialplan;

    }

    public TMSDialplan InboundLeaveVoicemailAfterHourClosed(DialplanVariable variable, TMSDialplan tmsDialplan) {
        tmsDialplan.addAction(new TMSOrder(IVROrder.INBOUND_LEAVE_VOICE_MAIL.name()));
        if (variable.getOptionSelectedId() != null && variable.getOptionSelectedId() != 1) {
            tmsDialplan.addAction(new Playback(RecordedPhrases.SORRY_COULD_NOT_PROCESS_YOUR_REQUEST_AT_THIS_TIME));
            String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
            tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 2, 5000, "#*", RecordedPhrases.IF_YOU_LIKE_TO_LEAVE_VOICE_MAIL.getAudioPath(), invalidToPlay, "INBOUND_LEAVE_VM_INPUT", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
            tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${INBOUND_LEAVE_VM_INPUT}"));
        }
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    private void sendToCustomerServiceFifo(DialplanVariable variable, TMSDialplan tmsDialplan, boolean isBorrowerKnown) {
        CallDetailRecordTMS mcdr = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());

        tmsDialplan.addAction(new TMSOrder(IVROrder2.INBOUND_LEAVE_VOICE_MAIL_AFTER_HOUR_CLOSED));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

    }

    private void playbackEnteredNumber(TMSDialplan tmsDialplan, String value, Boolean includeYouEntered) {
        List<Integer> digitEntered = textToSpeechService.getNumberSinglePatern(value);
        if (digitEntered != null && digitEntered.isEmpty() == false) {
            if (includeYouEntered) {
                tmsDialplan.addAction(new Playback((RecordedPhrases.YOU_ENTERED)));
            }
            for (Integer get : digitEntered) {
                tmsDialplan.addAction(new Playback(freeswitchConfiguration.getRecordingFile(get)));
            }
        }
    }
    
     private void playbackAmount(TMSDialplan tmsDialplan, Double amount) {
        if (amount < 1000.00) {
            tmsDialplan.addAction(new Playback(freeswitchConfiguration.getRecordingForAmountFile(amount)));
        } else {
            playbackAmountBrokenup(tmsDialplan, amount);
        }
    }

    private void playbackAmountBrokenup(TMSDialplan tmsDialplan, Double amount) {
        String value = amount + "";
        List<Integer> digitEntered = null;
        if (value.contains(".")) {
            digitEntered = textToSpeechService.getNumberPattern(value.substring(0, value.indexOf(".")));
            if (digitEntered != null && digitEntered.isEmpty() == false) {
                for (Integer get : digitEntered) {
                    tmsDialplan.addAction(new Playback(freeswitchConfiguration.getRecordingFile(get)));
                }
                tmsDialplan.addAction(new Playback(RecordedWords.DOLLARS));
            }

            if (value.substring(value.indexOf(".") + 1).equalsIgnoreCase("00") == false && value.substring(value.indexOf(".") + 1).equalsIgnoreCase("0") == false) {
                digitEntered = textToSpeechService.getNumberPattern(value.substring(value.indexOf(".") + 1));
                if (digitEntered != null && digitEntered.isEmpty() == false) {
                    tmsDialplan.addAction(new Playback(RecordedWords.AND));
                    for (Integer get : digitEntered) {
                        tmsDialplan.addAction(new Playback(freeswitchConfiguration.getRecordingFile(get)));
                    }
                    tmsDialplan.addAction(new Playback(RecordedWords.CENTS));
                }
            }

        } else {
            digitEntered = textToSpeechService.getNumberPattern(value);
            if (digitEntered != null && digitEntered.isEmpty() == false) {
                for (Integer get : digitEntered) {
                    tmsDialplan.addAction(new Playback(freeswitchConfiguration.getRecordingFile(get)));
                }
                tmsDialplan.addAction(new Playback(RecordedWords.DOLLARS));
            }
        }

    }

}
