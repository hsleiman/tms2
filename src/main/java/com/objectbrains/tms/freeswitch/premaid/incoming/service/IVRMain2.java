/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.incoming.service;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.svc.iws.AllocationIWS;
import com.objectbrains.svc.iws.InboundDialerQueueRecord;
import com.objectbrains.svc.iws.IvrAchInformationPojo;
import com.objectbrains.svc.iws.PaymentServiceIWS;
import com.objectbrains.svc.iws.SvBankData;
import com.objectbrains.svc.iws.SvcException;
import com.objectbrains.svc.iws.TMSServiceIWS;
import com.objectbrains.svc.iws.TmsBasicLoanInfo;
import com.objectbrains.svc.iws.TmsCallDetails;
import com.objectbrains.tms.db.entity.cdr.CallDetailRecord;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.RecordedPhrases;
import com.objectbrains.tms.enumerated.RecordedWords;
import com.objectbrains.tms.enumerated.refrence.HOLDOrder;
import com.objectbrains.tms.enumerated.refrence.IVROrder2;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.freeswitch.dialplan.action.Answer;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToAgent;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToFifo;
import com.objectbrains.tms.freeswitch.dialplan.action.Hangup;
import com.objectbrains.tms.freeswitch.dialplan.action.PlayAndGetDigits;
import com.objectbrains.tms.freeswitch.dialplan.action.Playback;
import com.objectbrains.tms.freeswitch.dialplan.action.Set;
import com.objectbrains.tms.freeswitch.dialplan.action.Sleep;
import com.objectbrains.tms.freeswitch.dialplan.action.TMSOrder;
import com.objectbrains.tms.freeswitch.dialplan.action.Transfer;
import com.objectbrains.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingDialerOrder;
import com.objectbrains.tms.freeswitch.premaid.incoming.IncomingPlaceOnHold;
import com.objectbrains.tms.service.AgentService;
import com.objectbrains.tms.service.CallDetailRecordService;
import com.objectbrains.tms.service.DialerQueueRecordService;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import com.objectbrains.tms.service.FreeswitchService;
import com.objectbrains.tms.service.InboundCallService;
import com.objectbrains.tms.service.TextToSpeechService;
import com.objectbrains.tms.service.freeswitch.common.Incoming2;
import java.math.BigDecimal;
import java.util.Calendar;
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
@Service("IVRMain2")
public class IVRMain2 {

    protected final static Logger log = LoggerFactory.getLogger(IVRMain2.class);

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private TMSServiceIWS tmsIWS;

    @Autowired
    private FreeswitchConfiguration freeswitchConfiguration;

    @ConfigContext
    private ConfigurationUtility config;

    @Autowired
    private AgentService agentService;

    @Autowired
    private InboundCallService inboundCallService;

    @Autowired
    private PaymentServiceIWS paymentServiceIWS;

    @Autowired
    private DialerQueueRecordService dialerQueueRecordRepository;

    @Autowired
    private AllocationIWS allocationIWS;

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
            sendToCustomerServiceFifo(variable, tmsDialplan, false, 1);
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
        if (loanPk == null) {
            loanPk = variable.getLoanId();
        }
        String ssn = variable.getSsn();

        TmsCallDetails callDetails = null;

        try {
            callDetails = tmsIWS.getLoanInfoByLoanPk(loanPk);
        } catch (Exception ex) {
            log.error("SVC Exception {} - {}", variable.getCall_uuid(), ex);
            sendToCustomerServiceFifo(variable, tmsDialplan, false, 2);
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
            callDetailRecordService.updateIVRAuthorized(variable.getCall_uuid(), Boolean.TRUE);
            tmsDialplan.addAction(new TMSOrder(IVROrder2.CHECK_PORTFOLIO_CONDITION));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        } else {
            callDetailRecordService.updateIVRAuthorized(variable.getCall_uuid(), Boolean.FALSE);
            sendToCustomerServiceFifo(variable, tmsDialplan, false, 2);
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
        if (loanPk == null) {
            loanPk = variable.getLoanId();
        }

        AgentIncomingDistributionOrder aido;
        if (tmsDialplan.getCallerLong() != null) {
            aido = inboundCallService.inboundCallOrder(null, tmsDialplan.getCallerLong(), tmsDialplan.getCall_uuid(), loanPk);
        } else {
            aido = inboundCallService.inboundCallOrder(null, 0l, tmsDialplan.getCall_uuid(), loanPk);
        }
        TmsBasicLoanInfo basicLoanInfo = null;

        try {
            basicLoanInfo = tmsIWS.getBasicLoanInfoForTMS(loanPk);
        } catch (Exception ex) {
            log.error("SVC Exception {} - {}", variable.getCall_uuid(), ex);
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 3);
            return tmsDialplan;
        }

        boolean notDelinquent = incoming.isNotDelinquent(basicLoanInfo);
        log.info("incoming.isNotDelinquent: {}", notDelinquent);
        log.info("aido: " + aido.toJson());
        if (basicLoanInfo != null) {
            log.info("The next due date is {} for {}", basicLoanInfo.getNextDueDate(), variable.getCall_uuid());
        } else {
            log.info("The borrower basic loan in info is null for {}", variable.getCall_uuid());
        }

        if (basicLoanInfo != null && basicLoanInfo.getNextDueDate() != null) {

            String var = OptionTextParser.SAVED_NEXT_DUE_DATE;
            if (basicLoanInfo.getNextDueDate().getDayOfMonth() < 10) {
                var = var + "0";
            }
            var = var + basicLoanInfo.getNextDueDate().getDayOfMonth();
            if (basicLoanInfo.getNextDueDate().getMonthOfYear() < 10) {
                var = var + "0";
            }
            var = var + basicLoanInfo.getNextDueDate().getMonthOfYear() + basicLoanInfo.getNextDueDate().getYear();

            var = var + OptionTextParser.SAVED_PRINCIPAL_BALANCE + basicLoanInfo.getPrincipalBalance();
            tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + var));
        } else {
            log.info("Could not save next due date for {}", variable.getCall_uuid());
        }

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

    public TMSDialplan CheckACHCondition(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));
        tmsDialplan.setOnce(Boolean.FALSE);

        Long loanPK = tmsDialplan.getBorrowerInfo().getLoanId();
        if (loanPK == null) {
            loanPK = variable.getLoanId();
        }

        IvrAchInformationPojo ivrAchInformationPojo = null;

        try {
            log.info("Calling TMS_IWS getAchDayAndAmountForLoan({}) for call_uuid {} ", loanPK, variable.getCall_uuid());
            ivrAchInformationPojo = tmsIWS.getAchDayAndAmountForLoan(loanPK);
        } catch (Exception ex) {
            log.error("SVC Exception {} - {}", variable.getCall_uuid(), ex);
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 4);
            return tmsDialplan;
        }
        if (ivrAchInformationPojo == null) {
            log.warn("Check ACH Condition {} - ivrAchInformationPojo is null", variable.getCall_uuid());
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 5);
            return tmsDialplan;
        }

        if (ivrAchInformationPojo.getPendingAchPk() > 0) {
            log.warn("Check ACH Condition {} - Pending ACH is {}", variable.getCall_uuid(), ivrAchInformationPojo.getPendingAchPk());
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 6);
            return tmsDialplan;
        }

        if (ivrAchInformationPojo.getAchAmount() == null) {
            log.warn("Check ACH Condition {} - ivrAchInformationPojo AchAmount is null", variable.getCall_uuid());
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 7);
            return tmsDialplan;
        }

        OptionTextParser optionTextParser = new OptionTextParser(variable.getOptionText());
        LocalDate nextDueDate = optionTextParser.getNextDueDate();

        if (nextDueDate == null) {
            log.warn("Next due date is null for call uuid {}", variable.getCall_uuid());
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 8);
            return tmsDialplan;
        }

        log.info("Check ACH Condition {} - ACHDate: {}, AchAmount {}, ACHDay: {} NextDue: {}", variable.getCall_uuid(), ivrAchInformationPojo.getAchDate(), ivrAchInformationPojo.getAchAmount(), ivrAchInformationPojo.getAchDayOfMonth(), optionTextParser.getNextDueDate());

        tmsDialplan.addAction(new Playback(RecordedPhrases.YOU_LOAN_IS_CURRENTLY_DUE_FOR));

        tmsDialplan.addAction(new Playback(RecordedWords.getMonthAndDay(nextDueDate)));
        tmsDialplan.addAction(new Playback(RecordedWords.getYear(nextDueDate.getYear())));

        tmsDialplan.addAction(new Playback(RecordedPhrases.IN_THE_AMOUNT_OF));
        playbackAmount(tmsDialplan, ivrAchInformationPojo.getAchAmount().doubleValue());

        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + OptionTextParser.IVR2_ASKED_TO_CHANGE_PAYMENT_AMOUNT + ivrAchInformationPojo.getAchAmount().doubleValue()));

        Integer autopaymentOption = 1;
        if (autopaymentOption != 1) {
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 10);
            return tmsDialplan;
        } else {
            tmsDialplan.addAction(new TMSOrder(IVROrder2.ASK_TO_CHANGE_SCHEDULED_PAYMENT));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        }

        return tmsDialplan;
    }

    public TMSDialplan AskToChangeScheduledPayment(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());

        Long loanPK = tmsDialplan.getBorrowerInfo().getLoanId();
        if (loanPK == null) {
            loanPK = variable.getLoanId();
        }

        IvrAchInformationPojo ivrAchInformationPojo = null;

        try {
            ivrAchInformationPojo = tmsIWS.getAchDayAndAmountForLoan(loanPK);
        } catch (Exception ex) {
            log.error("SVC Exception {} - {}", variable.getCall_uuid(), ex);
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 11);
            return tmsDialplan;
        }

        String var = OptionTextParser.IVR2_LATE_FEE_AMOUNT + ivrAchInformationPojo.getLateFee();
        log.info("Setting late fee amount {} for UUID {}", ivrAchInformationPojo.getLateFee(), variable.getCall_uuid());

        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(500l));
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.addAction(new Playback(RecordedPhrases.I_SEE_THAT_YOUR_PAYMENT_IS_SCHEDULED_TO_BE_PAID_ON_THE));
        tmsDialplan.addAction(new Playback(RecordedWords.getNumberTh(ivrAchInformationPojo.getAchDayOfMonth())));
        tmsDialplan.addAction(new Sleep(200l));
//        tmsDialplan.addAction(new Playback(RecordedPhrases.IF_YOU_ARE_CALLING_TO_CHANGE_THE_PAYMENT_DATE_PLEASE_PRESS_1_NOW_IF_YOU_WOULD_LIKE_TO_SPEAK_TO_A_CUSTOMER_SERVICE_REPRESENTATIVE_PLEASE_PRESS_0_NOW));
//        tmsDialplan.addAction(new Sleep(200l));
        String fileToPlay = RecordedPhrases.IF_YOU_ARE_CALLING_TO_CHANGE_THE_PAYMENT_DATE_PLEASE_PRESS_1_NOW_IF_YOU_WOULD_LIKE_TO_SPEAK_TO_A_CUSTOMER_SERVICE_REPRESENTATIVE_PLEASE_PRESS_0_NOW.getAudioPath();
        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 1, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d+", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + var + OptionTextParser.IVR2_ASKED_TO_CHANGE_PAYMENT + "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new TMSOrder(IVROrder2.CHANGE_SCHEDULED_PAYMENT));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan ChangeScheduledPayment(DialplanVariable variable, TMSDialplan tmsDialplan) {
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(500l));
        tmsDialplan.setOnce(Boolean.FALSE);

        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        if (tmsDialplan.getCounter() > 2) {
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 12);
            return tmsDialplan;
        }

        if (variable.getOptionSelectedId() == 1 || (tmsDialplan.getCounter() == 2 && variable.getOptionSelectedId() == 2)) {
            String fileToPlay = RecordedPhrases.PLEASE_ENTER_THE_DATE_YOU_WOULD_LIKE_THIS_PAYMENT_TO_BE_DRAFTED_PLEASE_ENTER_THE_2_DIGITS_MONTH_2_DIGITS_DAY_AND_2_DIGITS_YEAR__FOR_EXAMPLE_FOR_JANUARY_13_2016_YOU_WOULD_ENTER_011316.getAudioPath();
            String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
            tmsDialplan.addAction(new PlayAndGetDigits(6, 6, 2, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d{4}[17|18]", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
            tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + OptionTextParser.IVR2_DATE_CHANGED_PAYMENT + "${OPTION_SELECTED}"));
            tmsDialplan.addAction(new TMSOrder(IVROrder2.CHECK_IF_DAY_IS_LARGER_THEN_15));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        } else {
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 13);
            return tmsDialplan;
        }

        return tmsDialplan;
    }

    public TMSDialplan CheckIfDayIsLargerThen15(DialplanVariable variable, TMSDialplan tmsDialplan) {

        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(500l));
        tmsDialplan.setOnce(Boolean.FALSE);

        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        OptionTextParser optionTextParser = new OptionTextParser(variable.getOptionText());
        LocalDate paymentChangeDate = optionTextParser.getDateChangePayment();

        if (paymentChangeDate == null) {
            sendToCustomerServiceFifo(variable, tmsDialplan, false, 14);
            return tmsDialplan;
        }

        int dateOfMonthToCutoff = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);

        if (config.getBoolean("overide.last.day.of.month.cutoff.is.on", false)) {
            dateOfMonthToCutoff = config.getInteger("last.day.of.month.cutoff.date", dateOfMonthToCutoff);
        }

        LocalDate now = LocalDate.now();

        LocalDate cuttoffDate15 = new LocalDate(now.getYear(), now.getMonthOfYear(), 15);
        LocalDate cuttoffDate = new LocalDate(now.getYear(), now.getMonthOfYear(), dateOfMonthToCutoff - 2);

        if (now.getDayOfMonth() > freeswitchConfiguration.getACHConfigurationDayCutoffForIVR()) {
            cuttoffDate = cuttoffDate.plusMonths(1);
            cuttoffDate15 = cuttoffDate15.plusMonths(1);
        }

        String var = "";
        if (paymentChangeDate.isAfter(cuttoffDate15)) {
            var = OptionTextParser.IVR2_APPLY_LATE_FEE + Boolean.TRUE;
        } else {
            var = OptionTextParser.IVR2_APPLY_LATE_FEE + Boolean.FALSE;
        }

        log.info("CallUUID {}, CutOffDate {}, paymentChangeDate {}, isAfter {}, NowMonth {}, cuttoffDate15 {}", variable.getCall_uuid(), cuttoffDate, paymentChangeDate, paymentChangeDate.isAfter(cuttoffDate), now.getDayOfMonth(), cuttoffDate15);

        if (paymentChangeDate.isAfter(cuttoffDate)) {
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 15);
            return tmsDialplan;
        }
        if (paymentChangeDate.isBefore(now)) {
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 16);
            return tmsDialplan;
        }

        tmsDialplan.addAction(new Playback(RecordedPhrases.YOU_HAVE_ENTERED));
        tmsDialplan.addAction(new Playback(RecordedWords.getMonthAndDay(paymentChangeDate)));
        tmsDialplan.addAction(new Playback(RecordedWords.getYear(paymentChangeDate.getYear())));        

        String fileToPlay = RecordedPhrases.PRESS_1_IF_THIS_DATE_IS_CORRECT_PRESS_2_TO_REENTER_THE_DATE_PRESS_0_TO_SPEAK_TO_A_REPRESENTATIVE.getAudioPath();
        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 1, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d+", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + var + OptionTextParser.IVR2_YOU_ENTERED_CHECK_15TH + "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new TMSOrder(IVROrder2.CHECK_CHANGED_SCHEDULED_PAYMENT_DATE));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan CheckChangedScheduledPaymentDate(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());

        OptionTextParser optionTextParser = new OptionTextParser(variable.getOptionText());

        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(500l));
        tmsDialplan.setOnce(Boolean.FALSE);

        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        if (variable.getOptionSelectedId() == 2) {
            tmsDialplan.addAction(new TMSOrder(IVROrder2.CHANGE_SCHEDULED_PAYMENT));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        } else if (variable.getOptionSelectedId() == 1) {
            if (optionTextParser.getApplyLateFee()) {

                tmsDialplan.addAction(new TMSOrder(IVROrder2.VERIFIED_CHANGED_SCHEDULED_PAYMENT_DATE_WITH_LATE_FEE));
                tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

            } else {
                tmsDialplan.addAction(new TMSOrder(IVROrder2.VERIFIED_CHANGED_SCHEDULED_PAYMENT_DATE));
                tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
            }
        } else {
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 17);
            return tmsDialplan;
        }

        return tmsDialplan;
    }

    public TMSDialplan VerifiedChangedScheduledPaymentDateWithLateFee(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());

        OptionTextParser optionTextParser = new OptionTextParser(variable.getOptionText());

        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(500l));
        tmsDialplan.setOnce(Boolean.FALSE);
        tmsDialplan.addAction(new Playback(RecordedPhrases.THE_PAYMENT_DATE_YOU_HAVE_SELECTED_IS_MORE_THAN_15_DAYS_PAST_THE_DUE_DATE_THIS_PAYMENT_WILL_BE_SUBJECT_TO_A));
        playbackAmount(tmsDialplan, optionTextParser.getLateFeeAmount());
        tmsDialplan.addAction(new Playback(RecordedPhrases.LATE_FEE_IF_YOU_WISH_TO_KEEP_THIS_DATE_AS_THE_PAYMENT_DATE_AND_INCLUDE_THE));
        playbackAmount(tmsDialplan, optionTextParser.getLateFeeAmount());

        String fileToPlay = RecordedPhrases.LATE_CHARGE_WITH_THIS_PAYMENT_PRESS_1_IF_YOU_WISH_TO_WISH_TO_CHANGE_THIS_DATE_PRESS_2_OR_0_TO_SPEAK_WITH_A_REPRESENTATIVE.getAudioPath();
        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 1, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d+", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + OptionTextParser.IVR2_VERIFIED_CHANGED_PAYMENT_DATE_LATE_FEE + "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new TMSOrder(IVROrder2.VERIFIED_CHANGED_SCHEDULED_PAYMENT_DATE));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan VerifiedChangedScheduledPaymentDate(DialplanVariable variable, TMSDialplan tmsDialplan) {
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());

        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(500l));
        tmsDialplan.setOnce(Boolean.FALSE);

        OptionTextParser optionTextParser = new OptionTextParser(variable.getOptionText());
        if (optionTextParser.getApplyLateFee()) {
            if (variable.getOptionSelectedId() == 0) {
                sendToCustomerServiceFifo(variable, tmsDialplan, true, 18);
                return tmsDialplan;
            }
            if (variable.getOptionSelectedId() == 2) {
                tmsDialplan.addAction(new TMSOrder(IVROrder2.CHANGE_SCHEDULED_PAYMENT));
                tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
                return tmsDialplan;
            }
        }

        SvBankData bankData = null;
        try {
            bankData = tmsIWS.getBankDataForLoan(variable.getLoanId());
        } catch (Exception ex) {
            log.error("SVC Exception {} - {}", variable.getCall_uuid(), ex);
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 18);
            return tmsDialplan;
        }

        String bankAccount = "77777";
        if (bankData != null) {
            bankAccount = bankData.getBankDetails().getAccountNumber();
        }

        if (bankAccount.length() > 5) {
            bankAccount = bankAccount.substring(bankAccount.length() - 5);
        }

        tmsDialplan.addAction(new Playback(RecordedPhrases.TO_USE_THE_CHECKING_ACCOUNT_ON_FILE_ENDING_IN));

        for (int i = 0; i < bankAccount.length(); i++) {
            String get = bankAccount.charAt(i) + "";
            try {
                Integer number = Integer.parseInt(get);
                tmsDialplan.addAction(new Playback(freeswitchConfiguration.getRecordingFile(number)));
            } catch (NumberFormatException ex) {
                log.error("Number count not be parsed {} - {}", get, ex);
            }

        }

        String fileToPlay = RecordedPhrases.PLEASE_PRESS_1_NOW_PRESS_0_TO_SPEAK_TO_A_REPRESENTATIVE.getAudioPath();
        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 1, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d+", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + OptionTextParser.IVR2_VERIFIED_CHANGED_PAYMENT_DATE + "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new TMSOrder(IVROrder2.CHECK_CHANGED_SCHEDULED_PAYMENT_CHECKING_ACCOUNT));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan CheckChangedScheduledPaymentCheckingAccount(DialplanVariable variable, TMSDialplan tmsDialplan) {

        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(500l));
        tmsDialplan.setOnce(Boolean.FALSE);

        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());

        if (variable.getOptionSelectedId() == 1) {
            tmsDialplan.addAction(new TMSOrder(IVROrder2.VERIFIED_CHANGED_SCHEDULED_PAYMENT_CHECKING_ACCOUNT));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        } else {
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 18);
            return tmsDialplan;
        }

        return tmsDialplan;
    }

    public TMSDialplan VerifiedChangedScheduledPaymentCheckingAccount(DialplanVariable variable, TMSDialplan tmsDialplan) {

        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(500l));
        tmsDialplan.setOnce(Boolean.FALSE);

        if (tmsDialplan.getCounter() > 2) {
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 19);
            return tmsDialplan;
        }

        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        OptionTextParser optionTextParser = new OptionTextParser(variable.getOptionText());

        Double paymentAmount = optionTextParser.getPaymentAmount();
        if (paymentAmount != null && optionTextParser.getApplyLateFee() && optionTextParser.getLateFeeAmount() != null) {
            paymentAmount = paymentAmount + optionTextParser.getLateFeeAmount();
        }
        LocalDate nextDue = optionTextParser.getNextDueDate();
        LocalDate achDateChange = optionTextParser.getDateChangePayment();

        SvBankData bankData = null;
        try {
            bankData = tmsIWS.getBankDataForLoan(variable.getLoanId());
        } catch (Exception ex) {
            log.error("SVC Exception {} - {}", variable.getCall_uuid(), ex);
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 20);
            return tmsDialplan;
        }

        if (paymentAmount == null) {
            log.warn("payment amount was null {}", variable.getCall_uuid());
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 21);
            return tmsDialplan;
        }

        if (nextDue == null) {
            log.warn("next due was null {}", variable.getCall_uuid());
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 22);
            return tmsDialplan;
        }
        if (achDateChange == null) {
            log.warn("ach date change was null {}", variable.getCall_uuid());
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 23);
            return tmsDialplan;
        }

        tmsDialplan.addAction(new Playback(RecordedPhrases.OK_YOUR));
        tmsDialplan.addAction(new Playback(RecordedWords.getMonth(nextDue.getMonthOfYear())));
        tmsDialplan.addAction(new Playback(RecordedPhrases.FIRST_PAYMENT_IN_THE_AMOUNT_OF));
        playbackAmount(tmsDialplan, paymentAmount);
        tmsDialplan.addAction(new Playback(RecordedPhrases.WILL_BE_PRESENTED_TO_BANK_ACCOUNT_ENDING_IN));

        String bankAccount = "77777";
        if (bankData != null) {
            bankAccount = bankData.getBankDetails().getAccountNumber();
        }

        if (bankAccount.length() > 5) {
            bankAccount = bankAccount.substring(bankAccount.length() - 5);
        }

        for (int i = 0; i < bankAccount.length(); i++) {
            String get = bankAccount.charAt(i) + "";
            try {
                Integer number = Integer.parseInt(get);
                tmsDialplan.addAction(new Playback(freeswitchConfiguration.getRecordingFile(number)));
            } catch (NumberFormatException ex) {
                log.error("Number count not be parsed {} - {}", get, ex);
            }

        }
        tmsDialplan.addAction(new Playback(RecordedWords.ON));

        tmsDialplan.addAction(new Playback(RecordedWords.getMonthAndDay(achDateChange)));
        tmsDialplan.addAction(new Playback(RecordedWords.getYear(achDateChange.getYear())));

        tmsDialplan.addAction(new Sleep(25l));

        String fileToPlay = RecordedPhrases.PLEASE_PRESS_1_TO_CONFIRM_PLEASE_PRESS_2_TO_TRY_AGAIN_OR_PLEASE_PRESS_0_TO_SPEAK_TO_REPRESENTATIVE.getAudioPath();
        String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
        tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 2, 5000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d+", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + OptionTextParser.IVR2_VERIFIED_CHANGE_PAYAMENT_CHECKING_ACCOUNT + "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new TMSOrder(IVROrder2.CONFIRM_PAYMENT_CHANGE));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        return tmsDialplan;
    }

    public TMSDialplan ConfirmPaymentChange(DialplanVariable variable, TMSDialplan tmsDialplan) {

        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(500l));
        tmsDialplan.setOnce(Boolean.FALSE);

        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        callDetailRecordService.updateOptionText(variable.getCall_uuid(), variable.getOptionText());
        OptionTextParser optionTextParser = new OptionTextParser(variable.getOptionText());

        if (variable.getOptionSelectedId() == 1) {
            try {
                Double paymentAmount = optionTextParser.getPaymentAmount();
                if (paymentAmount != null && optionTextParser.getApplyLateFee() && optionTextParser.getLateFeeAmount() != null) {
                    paymentAmount = paymentAmount + optionTextParser.getLateFeeAmount();
                }
                tmsIWS.createAchRequestForTMS(optionTextParser.getDateChangePayment(), new BigDecimal(paymentAmount), variable.getLoanId(), variable.getCall_uuid());
            } catch (Exception ex) {
                log.error("SVC Exception {} - {}", variable.getCall_uuid(), ex);
                sendToCustomerServiceFifo(variable, tmsDialplan, true, 24);
                return tmsDialplan;
            }
        } else if (variable.getOptionSelectedId() == 2) {
            tmsDialplan.addAction(new TMSOrder(IVROrder2.CHANGE_SCHEDULED_PAYMENT));
            tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
            return tmsDialplan;
        } else {
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 25);
            return tmsDialplan;
        }
        tmsDialplan.addAction(new TMSOrder(IVROrder2.END_PAYMENT_CHANGE));
        String fileToPlay = RecordedPhrases.THANK_YOU_FOR_YOUR_PAYMENT_IF_YOU_NEED_ADDITIONAL_ASSISTANCE_PLEASE_PRESS_0_NOW.getAudioPath();
        String invalidToPlay = "silence_stream://250";
        tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 1, 10000, "#*", fileToPlay, invalidToPlay, "OPTION_SELECTED", "\\d+", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${OPTION_SELECTED}"));
        tmsDialplan.addAction(new Set(FreeswitchVariables.option_text, variable.getOptionText() + OptionTextParser.IVR2_END_PAYMENT_CHANGE + "${OPTION_SELECTED}"));
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));

        return tmsDialplan;
    }

    public TMSDialplan EndPaymentChange(DialplanVariable variable, TMSDialplan tmsDialplan) {
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(500l));
        tmsDialplan.setOnce(Boolean.FALSE);

        if (variable.getOptionSelectedId() == 0) {
            sendToCustomerServiceFifo(variable, tmsDialplan, true, 26);
            return tmsDialplan;
        }
        tmsDialplan.addAction(new Playback(RecordedPhrases.GOODEBYE));
        tmsDialplan.addAction(new Sleep(1000l));
        tmsDialplan.addBridge(new Hangup("NORMAL_CLEARING"));
        return tmsDialplan;
    }

    private void sendToCustomerServiceFifo(DialplanVariable variable, TMSDialplan tmsDialplan, boolean isBorrowerKnown, Integer lastTransferIvr) {
        tmsDialplan.addAction(new Sleep(500l));
        CallDetailRecord mcdr = callDetailRecordService.getCDR(tmsDialplan.getCall_uuid());

        callDetailRecordService.updateLastTransferStep(tmsDialplan.getCall_uuid(), lastTransferIvr);

        tmsDialplan.addAction(new Playback((RecordedPhrases.PLEASE_WAIT_FOR_NEXT_AGENT)));

        Long qPk = 1l;
        try {
            InboundDialerQueueRecord record = tmsIWS.getDefaultInboundQueueRecord();
            dialerQueueRecordRepository.storeInboundDialerQueueRecord(record);
            qPk = record.getDqPk();
        } catch (SvcException ex) {
            log.error("This is error in calling defaul inbound queue: {} - {}", variable.getCall_uuid(), ex);
        }

        AgentIncomingDistributionOrder aido = inboundCallService.inboundCallOrder(qPk, tmsDialplan.getCallerLong(), tmsDialplan.getCall_uuid());
        if (aido.getAgents().isEmpty()) {
            //tmsDialplan.addAction(new Playback(RecordedPhrases.PLEASE_WAIT_FOR_NEXT_AGENT));
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
