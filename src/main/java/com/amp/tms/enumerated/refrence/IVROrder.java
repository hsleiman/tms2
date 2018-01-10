/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.enumerated.refrence;

/**
 *
 * 
 */
public enum IVROrder {
    ASK_FOR_SSN("AskForSSN"),
    ASK_FOR_SSN_OR_LOAN("AskForSSNOrLoan"),
    VERIFY_SSN("VerifySSN"),
    ASK_FOR_ZIP("AskForZip"),
    VERIFY_ZIP("VerifyZip"),
    FOUND_LOAN_ID("FoundLoanId"),
    VERIFY_LOAN_ID("VerifyLoanId"),
    AGENT_FIFO("AgentFifo"),
    SEND_TO_FIFO("SendToFifo"),
    CONNECT_TO_AGENT("ConnectToAgent"),
    INBOUND_LEAVE_VOICE_MAIL("InboundLeaveVoicemail"),
    MAIN_MENU("MainMenu"),
    VERIFY_MAIN("VerifyMain"),
    SELECT_PAYMENT_TYPE("SelectPaymentType"),
    SELECT_PAYMENT_ENTRY("SelectPaymentEntry"),
    SELECTED_PAYMENT_ENTRY("SelectedPaymentEntry"),
    REVIEW_PAYMENT_ENTRY("ReviewPaymentEntry"),
    ENTER_PAYMENT_AMOUNT("EnterPaymentAmount"),
    APPLY_PAYMENT_ENTRY("ApplyPaymentEntry"),
    HANGUP_CALL("HangupCall");
    
    
    private final String method;

    private IVROrder(String method) {
        this.method = method;
    }

    public String getMethodName() {
        return method;
    }
}
