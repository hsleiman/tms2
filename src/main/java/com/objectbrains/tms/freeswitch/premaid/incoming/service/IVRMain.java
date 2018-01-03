/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.incoming.service;

import com.objectbrains.svc.iws.AchPaymentRequest;
import com.objectbrains.svc.iws.Address;
import com.objectbrains.svc.iws.AllocationIWS;
import com.objectbrains.svc.iws.BorrowerNotFoundException;
import com.objectbrains.svc.iws.CannotProcessPaymentException;
import com.objectbrains.svc.iws.CollectionQueueNotFoundException_Exception;
import com.objectbrains.svc.iws.CreditCardInformation;
import com.objectbrains.svc.iws.CreditCardPaymentRequest;
import com.objectbrains.svc.iws.CreditCardType;
import com.objectbrains.svc.iws.DueAmount;
import com.objectbrains.svc.iws.EftPaymentBasicData;
import com.objectbrains.svc.iws.NextDueResult;
import com.objectbrains.svc.iws.PaymentHistoryData;
import com.objectbrains.svc.iws.PaymentServiceIWS;
import com.objectbrains.svc.iws.PayoffQuote;
import com.objectbrains.svc.iws.SvcException;
import com.objectbrains.svc.iws.TMSService;
import com.objectbrains.svc.iws.TmsBasicLoanInfo;
import com.objectbrains.svc.iws.TmsCallDetails;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.PaymentType;
import com.objectbrains.tms.enumerated.RecordedPhrases;
import com.objectbrains.tms.enumerated.RecordedWords;
import com.objectbrains.tms.enumerated.refrence.HOLDOrder;
import com.objectbrains.tms.enumerated.refrence.IVROrder;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.freeswitch.dialplan.action.Answer;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToAgent;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToFifo;
import com.objectbrains.tms.freeswitch.dialplan.action.Hangup;
import com.objectbrains.tms.freeswitch.dialplan.action.Info;
import com.objectbrains.tms.freeswitch.dialplan.action.PlayAndGetDigits;
import com.objectbrains.tms.freeswitch.dialplan.action.Playback;
import com.objectbrains.tms.freeswitch.dialplan.action.Set;
import com.objectbrains.tms.freeswitch.dialplan.action.Sleep;
import com.objectbrains.tms.freeswitch.dialplan.action.TMSOrder;
import com.objectbrains.tms.freeswitch.dialplan.action.Transfer;
import com.objectbrains.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingDialerOrder;
import com.objectbrains.tms.pojo.BorrowerInfo;
import com.objectbrains.tms.service.AgentService;
import com.objectbrains.tms.service.CallDetailRecordService;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import com.objectbrains.tms.service.FreeswitchService;
import com.objectbrains.tms.service.InboundCallService;
import com.objectbrains.tms.service.TextToSpeechService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service("IVRMain")
public class IVRMain {

    protected final static Logger log = LoggerFactory.getLogger(IVRMain.class);

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private TMSService tmsIWS;

    @Autowired
    private FreeswitchConfiguration configuration;
    
    @Autowired
    private AgentService agentService;

    @Autowired
    private InboundCallService inboundCallService;

    @Autowired
    private PaymentServiceIWS paymentServiceIWS;

    @Autowired
    private AllocationIWS allocationIWS;

    @Autowired
    private TextToSpeechService textToSpeechService;
    
    @Autowired
    private FreeswitchService freeswitchService;

    public TMSDialplan VerifyMain(DialplanVariable variable, TMSDialplan tmsDialplan) {
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));

        if (tmsDialplan.getCounter() > 6) {
            sentToCustomerServiceFiFo(variable, tmsDialplan);
        } else {
            //AgentIncomingDistributionOrder aido = inboundCallService.inboundCallOrder(tmsDialplan.getCallerLong(), tmsDialplan.getBorrowerInfo().getLoanId());
            switch (variable.getOptionSelectedId()) {
                case 0:
                    sentToCustomerServiceFiFo(variable, tmsDialplan);
                    break;
//                case 1:
//                    String fileToPlay = RecordedPhrases.TYPE_OF_PAYMENT.getAudioPath();
//                    String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
//                    tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 3, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
//                    tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
//                    tmsDialplan.addAction(new TMSOrder(IVROrder.SELECT_PAYMENT_TYPE));
//                    tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
//                    break;
                case 2:
                    playCustomerBalanceThenSendToMainMenu(variable, tmsDialplan);
                    break;
                case 3:
                    PlayCustomerNextDueDateThenSendToMainMenu(variable, tmsDialplan);
                    break;
                case 4:
                    PlayCustomerLastPaymentThenSendToMainMenu(variable, tmsDialplan);
                    break;
                case 5:
                    PlayCustomerPayOffThenSendToMainMenu(variable, tmsDialplan);
                    break;
                default:
                    sendToMainMenu(tmsDialplan);

            }
        }
        return tmsDialplan;
    }

    public TMSDialplan SelectPaymentType(DialplanVariable variable, TMSDialplan tmsDialplan) {
        switch (variable.getOptionSelectedId()) {
            case 0:
                sentToCustomerServiceFiFo(variable, tmsDialplan);
                break;
            case 1:
                startCreditCardPayment(variable, tmsDialplan);
                break;
            case 2:
                startACHPayment(variable, tmsDialplan);
                break;
            case 3:
                tmsDialplan.addAction(new TMSOrder(IVROrder.MAIN_MENU));
                tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
                break;
            default:
                sendToMainMenu(tmsDialplan);

        }
        return tmsDialplan;
    }

    public TMSDialplan SelectPaymentEntry(DialplanVariable variable, TMSDialplan tmsDialplan) {
        String optionText = variable.getOptionText();
//        CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
//        callDetailRecord.setOptionText(optionText);
//        callDetailRecordService.saveCDR(callDetailRecord);
        callDetailRecordService.updateOptionText(tmsDialplan.getCall_uuid(), optionText);

        List<DueAmount> dueAmounts = null;
        dueAmounts = allocationIWS.getDueAmounts(variable.getLoanId(), null);

        if (dueAmounts == null) {
            sentToCustomerServiceFiFo(variable, tmsDialplan);
            return tmsDialplan;
        }

        BigDecimal amount = new BigDecimal(BigInteger.ZERO);
        for (int i = 0; i < dueAmounts.size(); i++) {
            DueAmount get = dueAmounts.get(i);
            amount.add(get.getAmountDue());
        }

        String paymentAmountOnFile = "POF_";
        String paymentOnType = "POT_";
        if (amount == BigDecimal.ZERO) {
            tmsDialplan.addAction(new Playback((RecordedPhrases.PAYMENT_SELECTION_DUE)));
            playbackEnteredAmount(tmsDialplan, amount.doubleValue(), true);
            paymentAmountOnFile = paymentAmountOnFile + amount.doubleValue();
            paymentOnType = paymentOnType + "DUE";
        } else {
            tmsDialplan.addAction(new Playback((RecordedPhrases.PAYMENT_SELECTION_MONTHLY)));
            NextDueResult ndr = allocationIWS.getNextDueInformation(variable.getLoanId(), LocalDate.now(), null);
            playbackEnteredAmount(tmsDialplan, ndr.getDueAmount().doubleValue(), true);
            paymentAmountOnFile = paymentAmountOnFile + ndr.getDueAmount().doubleValue();
            paymentOnType = paymentOnType + "MONTHLY";
        }

        tmsDialplan.addAction(new Playback((RecordedPhrases.PAYMENT_SELECTION_2)));

        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
        String fileToPlay = "silence_stream://10000";
        tmsDialplan.addAction(new TMSOrder(IVROrder.MAIN_MENU.name()));
        tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 3, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_AMOUNT", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + "-" + paymentAmountOnFile + "-" + "-" + paymentOnType + "-" + "-SPE_${OPTION_AMOUNT}"));

        tmsDialplan.addAction(new TMSOrder(IVROrder.SELECTED_PAYMENT_ENTRY.name()));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan SelectedPaymentEntry(DialplanVariable variable, TMSDialplan tmsDialplan) {

        String optionText = variable.getOptionText();

//        CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
//        callDetailRecord.setOptionText(optionText);
//        callDetailRecordService.saveCDR(callDetailRecord);
        callDetailRecordService.updateOptionText(tmsDialplan.getCall_uuid(), optionText);
        
        OptionTextDialplanPayment otdp = new OptionTextDialplanPayment(optionText);

        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
        if (otdp.isSystemAmount()) {
            tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText()));
        } else {
            tmsDialplan.addAction(new TMSOrder(IVROrder.MAIN_MENU.name()));
            tmsDialplan.addAction(new PlayAndGetDigits(2, 7, 3, 5000, "#*", RecordedPhrases.ENTER_PAYMENT_AMOUNT.getAudioPath(), invalidToPlay, "OPTION_AMOUNT", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
            tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + "-SDPE_${OPTION_AMOUNT}"));
        }
        tmsDialplan.addAction(new TMSOrder(IVROrder.REVIEW_PAYMENT_ENTRY.name()));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan ReviewPaymentEntry(DialplanVariable variable, TMSDialplan tmsDialplan) {

        tmsDialplan.addAction(new Playback((RecordedPhrases.YOU_SELECT_PAYMENT_OF)));
        String optionText = variable.getOptionText();
        
//        CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
//        callDetailRecord.setOptionText(optionText);
//        callDetailRecordService.saveCDR(callDetailRecord);
        callDetailRecordService.updateOptionText(tmsDialplan.getCall_uuid(), optionText);
        
        OptionTextDialplanPayment otdp = new OptionTextDialplanPayment(optionText);

        BigDecimal amount = otdp.getSelectedAmount();

        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();

        playbackEnteredAmount(tmsDialplan, amount.doubleValue(), true);
        tmsDialplan.addAction(new TMSOrder(IVROrder.SELECT_PAYMENT_ENTRY.name()));
        tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 3, 5000, "#*", RecordedPhrases.PRESS_ONE_OTHERWISE_ANY.getAudioPath(), invalidToPlay, "OPTION_AMOUNT", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + "-RPE_${OPTION_AMOUNT}"));

        tmsDialplan.addAction(new TMSOrder(IVROrder.APPLY_PAYMENT_ENTRY.name()));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan ApplyPaymentEntry(DialplanVariable variable, TMSDialplan tmsDialplan) {
        String optionText = variable.getOptionText();

        //CallDetailRecord callDetailRecord = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());
        //callDetailRecord.setOptionText(optionText);
        //callDetailRecordService.saveCDR(callDetailRecord);
        callDetailRecordService.updateOptionText(tmsDialplan.getCall_uuid(), optionText);
        
        
        OptionTextDialplanPayment otdp = new OptionTextDialplanPayment(optionText);

        if (otdp.getPaymentType() == PaymentType.CREDIT_CARD) {
            CreditCardPaymentRequest cardPaymentRequest = new CreditCardPaymentRequest();

            CreditCardInformation cardInformation = new CreditCardInformation();
            cardInformation.setCcNumber(otdp.getCreditCardNumber());
            cardInformation.setCcExp(otdp.getCreditCardExp());
            Address address = new Address();
            address.setZip(otdp.getCreditCardZip());
            cardInformation.setCcAddress(address);
            cardInformation.setCcVV(otdp.getCreditCardCCV());
            if (otdp.getCreditCardNumber().startsWith("4")) {
                cardInformation.setCcType(CreditCardType.VISA);
            } else if (otdp.getCreditCardNumber().startsWith("5")) {
                cardInformation.setCcType(CreditCardType.MASTERCARD);
            } else {
                cardInformation.setCcType(CreditCardType.VISA);
            }

            cardPaymentRequest.setCcInfo(cardInformation);
            cardPaymentRequest.setLoanPk(variable.getLoanId());

            EftPaymentBasicData eftPaymentBasicData = new EftPaymentBasicData();
            eftPaymentBasicData.setPaymentAmount(otdp.getSelectedAmount());
            eftPaymentBasicData.setComments("IVR Payment");
            eftPaymentBasicData.setPostingDate(LocalDate.now());
            cardPaymentRequest.setEftPaymentBasicData(eftPaymentBasicData);

            try {
                paymentServiceIWS.makeCreditCardPayment(cardPaymentRequest, null);
                tmsDialplan.addAction(new Playback((RecordedPhrases.AMOUNT_APPLIED_TO_ACCOUNT_1)));
                playbackEnteredAmount(tmsDialplan, otdp.getSelectedAmount().doubleValue(), true);
                tmsDialplan.addAction(new Playback((RecordedPhrases.AMOUNT_APPLIED_TO_ACCOUNT_2)));
                tmsDialplan.addAction(new TMSOrder(IVROrder.MAIN_MENU.name()));
                tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

            } catch (CannotProcessPaymentException | SvcException ex) {
                tmsDialplan.addAction(new Playback((RecordedPhrases.CREDIT_CARD_FAIL)));
                sentToCustomerServiceFiFo(variable, tmsDialplan);
            }

            return tmsDialplan;
        } else if (otdp.getPaymentType() == PaymentType.ACH) {
            AchPaymentRequest achPaymentRequest = new AchPaymentRequest();
            achPaymentRequest.setUseBankDataOnFile(true);
            achPaymentRequest.setLoanPk(variable.getLoanId());
            achPaymentRequest.setSendPaymentReminder(true);
            EftPaymentBasicData eftPaymentBasicData = new EftPaymentBasicData();
            eftPaymentBasicData.setPaymentAmount(otdp.getSelectedAmount());
            eftPaymentBasicData.setComments("IVR Payment");
            eftPaymentBasicData.setPostingDate(LocalDate.now());
            achPaymentRequest.setEftPaymentBasicData(eftPaymentBasicData);

            try {
                paymentServiceIWS.createAchRequest(achPaymentRequest, null);
                tmsDialplan.addAction(new Playback((RecordedPhrases.ACH_AMOUNT_OF_1)));
                playbackEnteredAmount(tmsDialplan, otdp.getSelectedAmount().doubleValue(), true);
                tmsDialplan.addAction(new Playback((RecordedPhrases.ACH_AMOUNT_OF_1)));
                tmsDialplan.addAction(new TMSOrder(IVROrder.MAIN_MENU.name()));
                tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

            } catch (SvcException ex) {
                tmsDialplan.addAction(new Playback((RecordedPhrases.ACH_FAIL)));
                sentToCustomerServiceFiFo(variable, tmsDialplan);
            }
            return tmsDialplan;
        }

        tmsDialplan.addAction(new TMSOrder(IVROrder.MAIN_MENU.name()));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan PlayCustomerPayOffThenSendToMainMenu(DialplanVariable variable, TMSDialplan tmsDialplan) {
        PayoffQuote payoffQuote = null;
        try {
            payoffQuote = allocationIWS.getPayoffQuote(tmsDialplan.getBorrowerInfo().getLoanId(), LocalDate.now(), null);
        } catch (SvcException | CollectionQueueNotFoundException_Exception ex) {

        }
        if (payoffQuote == null) {
            sentToCustomerServiceFiFo(variable, tmsDialplan);
            return tmsDialplan;
        }
        BigDecimal amount = payoffQuote.getTotalPayoff();

        tmsDialplan.addAction(new Playback(RecordedPhrases.PAY_OFF_AS_OF));
        playbackEnteredAmount(tmsDialplan, amount.doubleValue(), true);

        sendToMainMenu(tmsDialplan);

        return tmsDialplan;
    }

    public TMSDialplan PlayCustomerLastPaymentThenSendToMainMenu(DialplanVariable variable, TMSDialplan tmsDialplan) {
        PaymentHistoryData paymentHistoryData = null;
        try {
            paymentHistoryData = paymentServiceIWS.getRecentPaymentHistory(tmsDialplan.getBorrowerInfo().getLoanId(), null);
        } catch (SvcException | BorrowerNotFoundException ex) {

        }
        if (paymentHistoryData == null) {
            sentToCustomerServiceFiFo(variable, tmsDialplan);
            return tmsDialplan;
        }
        BigDecimal amount = paymentHistoryData.getActualPayments().get(0).getDueAmount();
        int month = paymentHistoryData.getActualPayments().get(0).getDueDate().getMonthOfYear();
        int day = paymentHistoryData.getActualPayments().get(0).getDueDate().getDayOfMonth();
        int year = paymentHistoryData.getActualPayments().get(0).getDueDate().getYear();
        List<Integer> digityear = textToSpeechService.getNumberPattern(year);

        tmsDialplan.addAction(new Playback(RecordedPhrases.YOUR_LAST_PAYMET));

        playbackEnteredAmount(tmsDialplan, amount.doubleValue(), true);
        tmsDialplan.addAction(new Playback(RecordedPhrases.MADE_ON));
        tmsDialplan.addAction(new Playback(RecordedWords.getMonth(month)));
        tmsDialplan.addAction(new Playback(configuration.getRecordingFile(day)));
        for (int i = 0; i < digityear.size(); i++) {
            Integer get = digityear.get(i);
            tmsDialplan.addAction(new Playback(configuration.getRecordingFile(get)));
        }

        sendToMainMenu(tmsDialplan);

        return tmsDialplan;
    }

    public TMSDialplan PlayCustomerNextDueDateThenSendToMainMenu(DialplanVariable variable, TMSDialplan tmsDialplan) {
        TmsBasicLoanInfo basicLoanInfo = null;
        basicLoanInfo = tmsIWS.getBasicLoanInfoForTMS(tmsDialplan.getBorrowerInfo().getLoanId());

        if (basicLoanInfo == null) {
            sentToCustomerServiceFiFo(variable, tmsDialplan);
            return tmsDialplan;
        }
        int month = basicLoanInfo.getNextDueDate().getMonthOfYear();
        int day = basicLoanInfo.getNextDueDate().getDayOfMonth();
        int year = basicLoanInfo.getNextDueDate().getYear();
        List<Integer> digityear = textToSpeechService.getNumberPattern(year);

        tmsDialplan.addAction(new Playback(RecordedPhrases.YOUR_NEXT_DUE_DATE));
        tmsDialplan.addAction(new Playback(RecordedWords.getMonth(month)));
        tmsDialplan.addAction(new Playback(configuration.getRecordingFile(day)));
        for (int i = 0; i < digityear.size(); i++) {
            Integer get = digityear.get(i);
            tmsDialplan.addAction(new Playback(configuration.getRecordingFile(get)));
        }

        sendToMainMenu(tmsDialplan);

        return tmsDialplan;
    }

    public TMSDialplan playCustomerBalanceThenSendToMainMenu(DialplanVariable variable, TMSDialplan tmsDialplan) {
        TmsBasicLoanInfo basicLoanInfo = null;
        basicLoanInfo = tmsIWS.getBasicLoanInfoForTMS(tmsDialplan.getBorrowerInfo().getLoanId());

        if (basicLoanInfo == null) {
            sentToCustomerServiceFiFo(variable, tmsDialplan);
            return tmsDialplan;
        }
        tmsDialplan.addAction(new Playback(RecordedPhrases.YOUR_BALANCE_AS_OF_TODAY));
        playbackEnteredAmount(tmsDialplan, basicLoanInfo.getPrincipalBalance().doubleValue(), true);
        sendToMainMenu(tmsDialplan);

        return tmsDialplan;
    }

    public TMSDialplan FoundLoanId(DialplanVariable variable, TMSDialplan tmsDialplan) {

        TmsCallDetails callDetails = tmsIWS.getLoanInfoByLoanPk(tmsDialplan.getBorrowerInfo().getLoanId());

        tmsDialplan.addAction(new Set("playback_terminators", "any"));

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
        tmsDialplan.addAction(new TMSOrder(IVROrder.VERIFY_LOAN_ID));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan VerifyLoanId(DialplanVariable variable, TMSDialplan tmsDialplan) {
        if (variable.getOptionSelectedId() != null && variable.getOptionSelectedId() == 1) {
            tmsDialplan.addAction(new Playback(RecordedPhrases.MENU_RECENTLY_CHANGED));
            tmsDialplan.addAction(new TMSOrder(IVROrder.MAIN_MENU));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        } else {
            tmsDialplan.setBorrowerInfo(new BorrowerInfo());
            sentToCustomerServiceFiFo(variable, tmsDialplan);
        }
        return tmsDialplan;
    }
    
    public TMSDialplan PlaceInVoicemail(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateInboundLeftVoicemail(tmsDialplan.getCall_uuid(), Boolean.TRUE);
        return tmsDialplan;
    }
            

    public TMSDialplan InboundLeaveVoicemail(DialplanVariable variable, TMSDialplan tmsDialplan) {
        tmsDialplan.addAction(new Info());
        if (variable.getOptionSelectedId() != null && variable.getOptionSelectedId() == 1) {
            tmsDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_IN_VOICEMAIL));
            tmsDialplan.addBridge(new BridgeToFifo(FreeswitchContext.ivr_dp, freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));
        }
        else{
            tmsDialplan.addAction(new Sleep(1000l));
            tmsDialplan.addBridge(new Hangup("NORMAL_CLEARING"));
        }
        return tmsDialplan;
    }

    private void sentToCustomerServiceFiFo(DialplanVariable variable, TMSDialplan tmsDialplan) {
        AgentIncomingDistributionOrder aido = inboundCallService.inboundCallOrder(null, tmsDialplan.getCallerLong(), tmsDialplan.getCall_uuid());
        if (aido.getAgents().isEmpty()) {
            tmsDialplan.addAction(new Playback(RecordedPhrases.PLEASE_WAIT_FOR_NEXT_AGENT));
            tmsDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_ON_HOLD_CUSTOMER_SERVICE));
            tmsDialplan.addBridge(new BridgeToFifo(FreeswitchContext.ivr_dp, freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));
        } else {
            IncomingDialerOrder builder = new IncomingDialerOrder(variable, aido);
            builder.setTMS_UUID(tmsDialplan.getKey().getTms_uuid());
            TMSDialplan connectToAgentDialplan = builder.buildDialplansWithoutSBC();
            tmsDialplan.addAction(new TMSOrder(connectToAgentDialplan.getKey().getOrderPower()));
            tmsDialplan.addBridge(new BridgeToAgent(agentService.getFreeswitchIPForExt(connectToAgentDialplan.getCalleeInteger()), connectToAgentDialplan.getCalleeInteger()));
        }
    }

    private void sendToMainMenu(TMSDialplan tmsDialplan) {
        tmsDialplan.addAction(new TMSOrder(IVROrder.MAIN_MENU));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
    }

    private void playbackEnteredAmount(TMSDialplan tmsDialplan, Double amount, Boolean doNotPlayYouEntered) {
        String value = amount + "";

        List<Integer> digitEntered = null;
        if (value.contains(".")) {
            digitEntered = textToSpeechService.getNumberPattern(value.substring(0, value.indexOf(".")));
            if (digitEntered != null && digitEntered.isEmpty() == false) {
                if (doNotPlayYouEntered == false) {
                    tmsDialplan.addAction(new Playback(RecordedPhrases.YOU_ENTERED));
                }
                for (Integer get : digitEntered) {
                    tmsDialplan.addAction(new Playback(configuration.getRecordingFile(get)));
                }
                tmsDialplan.addAction(new Playback(RecordedWords.DOLLARS));
            }

            if (value.substring(value.indexOf(".")).equalsIgnoreCase("00") == false) {
                digitEntered = textToSpeechService.getNumberPattern(value.substring(value.indexOf(".") + 1));
                if (digitEntered != null && digitEntered.isEmpty() == false) {
                    tmsDialplan.addAction(new Playback(RecordedWords.AND));
                    if (doNotPlayYouEntered == false) {
                        tmsDialplan.addAction(new Playback(RecordedPhrases.YOU_ENTERED));
                    }
                    for (Integer get : digitEntered) {
                        tmsDialplan.addAction(new Playback(configuration.getRecordingFile(get)));
                    }
                    tmsDialplan.addAction(new Playback(RecordedWords.CENTS));
                }
            }

        } else {
            digitEntered = textToSpeechService.getNumberPattern(value);
            if (digitEntered != null && digitEntered.isEmpty() == false) {
                if (doNotPlayYouEntered == false) {
                    tmsDialplan.addAction(new Playback(RecordedPhrases.YOU_ENTERED));
                }
                for (Integer get : digitEntered) {
                    tmsDialplan.addAction(new Playback(configuration.getRecordingFile(get)));
                }
                tmsDialplan.addAction(new Playback(RecordedWords.DOLLARS));
            }
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

    private void startCreditCardPayment(DialplanVariable variable, TMSDialplan tmsDialplan) {

        tmsDialplan.addAction(new TMSOrder(IVROrder.MAIN_MENU));

        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();

        String fileToPlay = RecordedPhrases.CREDIT_CARD_NUMBER.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(13, 16, 3, 5000, "#*", fileToPlay, RecordedPhrases.CREDIT_CARD_NUMBER_INVALID.getAudioPath(), "CREDIT_CARD_NUMBER", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, "${OPTION_SELECTED}"));

        fileToPlay = RecordedPhrases.CREDIT_CARD_EXPIRATION.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(4, 4, 3, 5000, "#*", fileToPlay, invalidToPlay, "CREDIT_CARD_EXP", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));

        fileToPlay = RecordedPhrases.CREDIT_CARD_ZIP.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(5, 5, 3, 5000, "#*", fileToPlay, invalidToPlay, "CREDIT_CARD_ZIP_CODE", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));

        fileToPlay = RecordedPhrases.CREDIT_CARD_CCV.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(3, 3, 3, 5000, "#*", fileToPlay, invalidToPlay, "CREDIT_CARD_CCV_CODE", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));

        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, "PT_1-CCN_${CREDIT_CARD_NUMBER}-CCE_${CREDIT_CARD_EXP}-CCZ_${CREDIT_CARD_ZIP_CODE}-CCC_${CREDIT_CARD_CCV_CODE}"));

        tmsDialplan.addAction(new TMSOrder(IVROrder.SELECT_PAYMENT_ENTRY));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
    }

    private void startACHPayment(DialplanVariable variable, TMSDialplan tmsDialplan) {

        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, "PT_2"));
        tmsDialplan.addAction(new TMSOrder(IVROrder.SELECT_PAYMENT_ENTRY));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

    }
}
