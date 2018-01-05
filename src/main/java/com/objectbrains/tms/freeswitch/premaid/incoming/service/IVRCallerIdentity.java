/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.incoming.service;

import com.objectbrains.sti.pojo.TMSBasicAccountInfo;
import com.objectbrains.sti.pojo.TMSCallDetails;
import com.objectbrains.sti.service.tms.TMSService;
import com.objectbrains.tms.db.entity.cdr.CallDetailRecord;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.RecordedPhrases;
import com.objectbrains.tms.enumerated.refrence.HOLDOrder;
import com.objectbrains.tms.enumerated.refrence.IVROrder;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.freeswitch.dialplan.action.Answer;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToAgent;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToFifo;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToIVR;
import com.objectbrains.tms.freeswitch.dialplan.action.PlayAndGetDigits;
import com.objectbrains.tms.freeswitch.dialplan.action.Playback;
import com.objectbrains.tms.freeswitch.dialplan.action.Set;
import com.objectbrains.tms.freeswitch.dialplan.action.Sleep;
import com.objectbrains.tms.freeswitch.dialplan.action.TMSOrder;
import com.objectbrains.tms.freeswitch.dialplan.action.Transfer;
import com.objectbrains.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingDialerOrder;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingIVR;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingPlaceOnHold;
import com.objectbrains.tms.pojo.BorrowerInfo;
import com.objectbrains.tms.service.TMSAgentService;
import com.objectbrains.tms.service.CallDetailRecordService;
import com.objectbrains.tms.service.DialplanService;
import com.objectbrains.tms.service.DncService;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import com.objectbrains.tms.service.FreeswitchService;
import com.objectbrains.tms.service.InboundCallService;
import com.objectbrains.tms.service.TextToSpeechService;
import com.objectbrains.tms.service.freeswitch.common.Incoming2;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service("IVRCallerIdentity")
public class IVRCallerIdentity {

    protected final static Logger log = LoggerFactory.getLogger(IVRCallerIdentity.class);

    @Autowired
    private TMSService tmsIWS;
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
    private TextToSpeechService textToSpeechService;

    @Autowired
    private InboundCallService inboundCallService;
    
    @Autowired
    private FreeswitchService freeswitchService;

    public TMSDialplan AskForSSN(DialplanVariable variable, TMSDialplan tmsDialplan) {

        if (tmsDialplan.getCounter() > 4) {
            sendToCustomerServiceFifo(variable, tmsDialplan, false);
            return tmsDialplan;
        }
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));
        tmsDialplan.setOnce(Boolean.FALSE);
        String fileToPlay = RecordedPhrases.ENTER_YOU_SSN.getAudioPath();
        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(1, 4, 1, 10000, "#*", fileToPlay, invalidToPlay, "SSN_ID", "\\d+", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        //tmsDialplan.addAction(new PlayAndGetDigits(4, 4, 1, 10000, "#*", fileToPlay, invalidToPlay, "SSN_ID", "\\d{4}", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.ssn_id, "${SSN_ID}"));
        tmsDialplan.addAction(new TMSOrder(IVROrder.VERIFY_SSN));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        return tmsDialplan;

    }

    public TMSDialplan VerifySSN(DialplanVariable variable, TMSDialplan tmsDialplan) {
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));

        if (tmsDialplan.getCounter() > 3) {
            sendToCustomerServiceFifo(variable, tmsDialplan, false);
            return tmsDialplan;
        }
        String ssn = variable.getSsn();
        
        if (ssn == null || "0".equals(ssn)) {
            sendToCustomerServiceFifo(variable, tmsDialplan, false);
        } else {
            
            //CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());

            playbackEnteredNumber(tmsDialplan, ssn, true);
            //callDetailRecord.setIvrSSN(ssn);
            //callDetailRecordService.saveCDR(callDetailRecord);
            callDetailRecordService.updateIVRSSN(tmsDialplan.getCall_uuid(), ssn);

            String fileToPlay = RecordedPhrases.PRESS_ONE_OTHERWISE_ANY.getAudioPath();
            String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
            tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 1, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
            tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
            tmsDialplan.addAction(new TMSOrder(IVROrder.ASK_FOR_ZIP));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        }
        return tmsDialplan;
    }

    public TMSDialplan AskForZip(DialplanVariable variable, TMSDialplan tmsDialplan) {
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));

        if (tmsDialplan.getCounter() > 3) {
            sendToCustomerServiceFifo(variable, tmsDialplan, false);
            return tmsDialplan;
        }

        if (variable.getOptionSelectedId() != null && variable.getOptionSelectedId() == 1) {
            String fileToPlay = RecordedPhrases.ENTER_ZIP_CODE_ON_FILE.getAudioPath();
            String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
            tmsDialplan.addAction(new PlayAndGetDigits(5, 5, 1, 10000, "#*", fileToPlay, invalidToPlay, "ZIP_CODE", "\\d{5}", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
            tmsDialplan.addAction(new Set(FreeswitchVariables.zip_code_id, "${ZIP_CODE}"));
            tmsDialplan.addAction(new TMSOrder(IVROrder.VERIFY_ZIP));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        } else {

            TMSDialplan dialplan = dialplanRepository.findTMSDialplan(tmsDialplan.getKey().getTms_uuid(), FreeswitchContext.ivr_dp, IVROrder.ASK_FOR_SSN.name());
            if (dialplan != null && dialplan.getCounter() > 3) {
                sendToCustomerServiceFifo(variable, tmsDialplan, false);
            } else {
                tmsDialplan.addAction(new Set(FreeswitchVariables.ssn_id, ""));
                tmsDialplan.addAction(new TMSOrder(IVROrder.ASK_FOR_SSN));
                tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
            }
        }
        return tmsDialplan;
    }

    public TMSDialplan VerifyZip(DialplanVariable variable, TMSDialplan tmsDialplan) {
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));

        if (tmsDialplan.getCounter() > 3) {
            sendToCustomerServiceFifo(variable, tmsDialplan, false);
            return tmsDialplan;
        }

        if (variable.getZipCodeId() == null) {
            sendToCustomerServiceFifo(variable, tmsDialplan, false);
        } else {

            if (tmsDialplan.getCounter() > 3) {
                sendToCustomerServiceFifo(variable, tmsDialplan, false);
            } else {
                String zipCode = variable.getZipCodeId();
                //CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
                //callDetailRecord.setIvrZipCode(zipCode);
                playbackEnteredNumber(tmsDialplan, zipCode, true);
                //callDetailRecordService.saveCDR(callDetailRecord);
                callDetailRecordService.updateIVRZip(tmsDialplan.getCall_uuid(), zipCode);
                
                String fileToPlay = (RecordedPhrases.PRESS_ONE_OTHERWISE_ANY.getAudioPath());
                String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();

                tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 3, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
                tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
                tmsDialplan.addAction(new TMSOrder(IVROrder.FOUND_LOAN_ID));
                tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
            }
        }
        return tmsDialplan;
    }

    public TMSDialplan FoundLoanId(DialplanVariable variable, TMSDialplan tmsDialplan) {
        if (variable.getOptionSelectedId() != null && variable.getOptionSelectedId() == 1) {
            CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
            String ssn = callDetailRecord.getIvrSSN();
            String zipCode = callDetailRecord.getIvrZipCode();
            TMSCallDetails callDetails = null;// tmsIWS.getLoanInfoBySsnAndZip(ssn, zipCode);

            if (callDetails != null && callDetails.getZip() != null && callDetails.getZip().equalsIgnoreCase(zipCode)) {

                tmsDialplan.addAction(new Playback((RecordedPhrases.I_FOUND_A_LOAN)));

                String loanID = callDetails.getLoanPk() + "";
                if (loanID.length() > 5) {
                    loanID = loanID.substring(loanID.length() - 5);
                }
                playbackEnteredNumber(tmsDialplan, loanID, false);

                String fileToPlay = RecordedPhrases.IS_THAT_CORRECT.getAudioPath();
                String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
                tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 3, 10000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
                tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
                BorrowerInfo borrowerInfo = new BorrowerInfo();
                borrowerInfo.setLoanId(callDetails.getLoanPk());
                borrowerInfo.setBorrowerFirstName(callDetails.getFirstName());
                borrowerInfo.setBorrowerLastName(callDetails.getLastName());
                borrowerInfo.setBorrowerPhoneNumber(tmsDialplan.getCaller());
                tmsDialplan.setBorrowerInfo(borrowerInfo);
                //callDetailRecord.setBorrowerInfo(borrowerInfo);
                //callDetailRecordService.saveCDR(callDetailRecord);
                callDetailRecordService.updateBorrowerInfo(tmsDialplan.getCall_uuid(), borrowerInfo);
                tmsDialplan.addAction(new TMSOrder(IVROrder.VERIFY_LOAN_ID));
                tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

            } else {
                AgentIncomingDistributionOrder aido;
                if (tmsDialplan.getCallerLong() != null) {
                    aido = inboundCallService.inboundCallOrder(null, tmsDialplan.getCallerLong(), tmsDialplan.getCall_uuid());
                } else {
                    aido = inboundCallService.inboundCallOrder(null, 0l, tmsDialplan.getCall_uuid());
                }

                if (aido.getAgents().isEmpty()) {
                    sendToCustomerServiceFifo(variable, tmsDialplan, false);
                } else {
                    IncomingDialerOrder builder = new IncomingDialerOrder(variable, aido);
                    builder.setTMS_UUID(tmsDialplan.getKey().getTms_uuid());
                    TMSDialplan connectToAgentDialplan = builder.buildDialplansWithoutSBC();
                    tmsDialplan.addAction(new TMSOrder(connectToAgentDialplan.getKey().getOrderPower()));
                    tmsDialplan.addBridge(new BridgeToAgent(agentService.getFreeswitchIPForExt(connectToAgentDialplan.getCalleeInteger()), connectToAgentDialplan.getCalleeInteger()));
                }
            }
        } else {
            TMSDialplan dialplan = dialplanRepository.findTMSDialplan(tmsDialplan.getKey().getTms_uuid(), FreeswitchContext.ivr_dp, IVROrder.ASK_FOR_ZIP.name());
            if (dialplan != null && dialplan.getCounter() > 4) {
                sendToCustomerServiceFifo(variable, tmsDialplan, false);
            } else {
                tmsDialplan.addAction(new TMSOrder(IVROrder.ASK_FOR_ZIP));
                tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
            }
        }

        return tmsDialplan;
    }

    public TMSDialplan VerifyLoanId(DialplanVariable variable, TMSDialplan tmsDialplan) {
        if (variable.getOptionSelectedId() != null && variable.getOptionSelectedId() == 1) {

            AgentIncomingDistributionOrder aido;
            if (tmsDialplan.getCallerLong() != null) {
                aido = inboundCallService.inboundCallOrder(null, tmsDialplan.getCallerLong(), tmsDialplan.getCall_uuid(), variable.getLoanId());
            } else {
                aido = inboundCallService.inboundCallOrder(null, 0l, tmsDialplan.getCall_uuid(), variable.getLoanId());
            }
            TMSBasicAccountInfo basicLoanInfo = null;
            basicLoanInfo = tmsIWS.getBasicAccountInfoForTMS(variable.getLoanId());

            log.info("incoming.isNotDelinquent: " + incoming.isNotDelinquent());
            log.info("aido: " + aido.toJson());
            if (incoming.isNotDelinquent()) {
                log.info("incoming.isNotDelinquent: " + incoming.isNotDelinquent());
                IncomingIVR builder = new IncomingIVR(variable, aido);
                builder.setTMS_UUID(tmsDialplan.getKey().getTms_uuid());
                TMSDialplan ivrD = builder.buildDialplansNoSBC();
                tmsDialplan.addAction(new TMSOrder(ivrD.getKey().getOrderPower()));
                tmsDialplan.addBridge(new BridgeToIVR(freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.ivr_dp)));

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

        } else {
            callDetailRecordService.updateBorrowerInfo(tmsDialplan.getCall_uuid(), null);
            sendToCustomerServiceFifo(variable, tmsDialplan, false);
        }
        return tmsDialplan;
    }

    private void sendToCustomerServiceFifo(DialplanVariable variable, TMSDialplan tmsDialplan, boolean isBorrowerKnown) {
        tmsDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_ON_HOLD));
        tmsDialplan.addAction(new Playback((RecordedPhrases.I_AM_SORRY_I_CANT_FIND_ACCOUNT)));
        tmsDialplan.addAction(new Sleep(1500l));
        tmsDialplan.addAction(new Playback((RecordedPhrases.PLEASE_WAIT_FOR_NEXT_AGENT)));

        AgentIncomingDistributionOrder aido = inboundCallService.inboundCallOrder(null, tmsDialplan.getCallerLong(), tmsDialplan.getCall_uuid());
        if (aido.getAgents().isEmpty()) {
            //tmsDialplan.addAction(new Playback(RecordedPhrases.PLEASE_WAIT_FOR_NEXT_AGENT));
            tmsDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_ON_HOLD));
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
