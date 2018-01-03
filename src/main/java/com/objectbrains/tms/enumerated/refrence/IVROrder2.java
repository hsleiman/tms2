/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.enumerated.refrence;

/**
 *
 * @author hsleiman
 */
public enum IVROrder2 {

    ASK_FOR_SSN_OR_LOAN("AskForSSNOrLoan"),
    CHECK_FOR_SSN_OR_LOAN("CheckForSSNOrLoan"),
    ASK_FOR_LOAN("AskForLoan"),
    ASK_FOR_SSN("AskForSSN"),
    ASK_FOR_DOB("AskForDOB"),
    CHECK_SECURITY_FOR_DOB("CheckSecurityForDOB"),
    VERIFIED_SECURITY_FOR_DOB("VerifiedSecurityForDOB"),
    CHECK_SECURITY_FOR_LOAN("CheckSecurityForLoan"),
    ASK_ARE_YOU_CALLING_FOR_LOAN("ASKAreYouCallingForLoan"),
    VERIFIED_CORRECT_LOAN_ID("VerifiedCorrectLoanId"),
    VERIFIED_SECURITY_FOR_LOAN("VerifiedSecurityForLoan"),
    CHECK_PORTFOLIO_CONDITION("CheckProtfolioCondition"),
    CHECK_ACH_CONDITION("CheckACHCondition"),
    ASK_TO_CHANGE_SCHEDULED_PAYMENT("AskToChangeScheduledPayment"),
    CHANGE_SCHEDULED_PAYMENT("ChangeScheduledPayment"),
    CHECK_IF_DAY_IS_LARGER_THEN_15("CheckIfDayIsLargerThen15"),
    CHECK_CHANGED_SCHEDULED_PAYMENT_DATE("CheckChangedScheduledPaymentDate"),
    VERIFIED_CHANGED_SCHEDULED_PAYMENT_DATE("VerifiedChangedScheduledPaymentDate"),
    VERIFIED_CHANGED_SCHEDULED_PAYMENT_DATE_WITH_LATE_FEE("VerifiedChangedScheduledPaymentDateWithLateFee"),
    CHECK_CHANGED_SCHEDULED_PAYMENT_CHECKING_ACCOUNT("CheckChangedScheduledPaymentCheckingAccount"),
    VERIFIED_CHANGED_SCHEDULED_PAYMENT_CHECKING_ACCOUNT("VerifiedChangedScheduledPaymentCheckingAccount"),
    CONFIRM_PAYMENT_CHANGE("ConfirmPaymentChange"),
    END_PAYMENT_CHANGE("EndPaymentChange"),
    INBOUND_LEAVE_VOICE_MAIL_AFTER_HOUR_CLOSED("InboundLeaveVoicemailAfterHourClosed"),
    
    HANGUP_CALL("HangupCall");

    private final String method;

    private IVROrder2(String method) {
        this.method = method;
    }

    public String getMethodName() {
        return method;
    }
}
