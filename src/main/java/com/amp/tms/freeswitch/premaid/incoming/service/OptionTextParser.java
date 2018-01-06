/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.incoming.service;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hsleiman
 */
public class OptionTextParser {

    protected final static Logger log = LoggerFactory.getLogger(OptionTextParser.class);

    public static String IVR2_SSN_OR_LOANID = "-IVR2_SSN_OR_LOANID:";
    public static String IVR2_LOANID = "-IVR2_LOANID:";
    public static String IVR2_SSN = "-IVR2_SSN:";
    public static String IVR2_DOB = "-IVR2_DOB:";
    public static String IVR2_ASKED_LOAN_CORRECT = "-IVR2_ASKED_LOAN_CORRECT:";

    public static String IVR2_CLOSED_PROMPT_SELECTION = "-IVR2_CLOSED_PROMPT_SELECTION:";

    public static String IVR2_ASKED_TO_CHANGE_PAYMENT = "-IVR2_ASKED_TO_CHANGE_PAYMENT:";
    public static String IVR2_DATE_CHANGED_PAYMENT = "-IVR2_DATE_CHANGED_PAYMENT:";         // ACh payment changed date.

    public static String IVR2_YOU_ENTERED_CHECK_15TH = "-IVR2_YOU_ENTERED_CHECK_15TH:";
    public static String IVR2_VERIFIED_CHANGED_PAYMENT_DATE = "-IVR2_VERIFIED_CHANGED_PAYMENT_DATE:";
    public static String IVR2_VERIFIED_CHANGED_PAYMENT_DATE_LATE_FEE = "-IVR2_VERIFIED_CHANGED_PAYMENT_DATE_LATE_FEE:";

    public static String IVR2_VERIFIED_CHANGE_PAYAMENT_CHECKING_ACCOUNT = "-IVR2_VERIFIED_CHANGE_PAYAMENT_CHECKING_ACCOUNT:";

    public static String IVR2_ASKED_TO_CHANGE_PAYMENT_AMOUNT = "-IVR2_ASKED_TO_CHANGE_PAYMENT_AMOUNT:"; // Ach payment amount.

    public static String IVR2_LATE_FEE_AMOUNT = "-IVR2_LATE_FEE_AMOUNT:"; // Ach payment amount.
    public static String IVR2_APPLY_LATE_FEE = "-IVR2_APPLY_LATE_FEE:";

    public static String IVR2_END_PAYMENT_CHANGE = "-IVR2_END_PAYMENT_CHANGE:";

    public static String SAVED_NEXT_DUE_DATE = "-SAVED_NEXT_DUE_DATE:";

    public static String SAVED_PRINCIPAL_BALANCE = "-SAVED_PRINCIPAL_BALANCE:";

    private String text = "null-IVR2_SSN_OR_LOANID:1-IVR2_LOANID:1270-IVR2_DOB:102219";

    public OptionTextParser(String text) {
        this.text = text;
    }

    public Integer getSSNOrLoan() {
        String value = parser(IVR2_SSN_OR_LOANID);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return 0;
    }

    public Long getLoanId() {
        String value = parser(IVR2_LOANID);
        if (value != null) {
            return Long.parseLong(value);
        }
        return 0l;
    }

    public String getSSN() {
        return parser(IVR2_SSN);
    }

    public LocalDate getDOB() {
        String tmp = parser(IVR2_DOB);
        if (tmp == null) {
            return null;
        }

        Integer monthOfYear = Integer.parseInt(tmp.substring(0, 2));
        Integer dayOfMonth = Integer.parseInt(tmp.substring(2, 4));

        String str = tmp.substring(4);
        Integer dd = Integer.parseInt(str);
        if (dd > 30) {
            str = "19" + str;
        } else {
            str = "20" + str;
        }

        Integer year = Integer.parseInt(str);
        try {
            LocalDate localDate = new LocalDate(year, monthOfYear, dayOfMonth);
            return localDate;
        } catch (Exception ex) {
            return null;
        }

    }

    public LocalDate getDateChangePayment() {
        String tmp = parser(IVR2_DATE_CHANGED_PAYMENT);
        if (tmp == null) {
            return null;
        }

        Integer monthOfYear = Integer.parseInt(tmp.substring(0, 2));
        Integer dayOfMonth = Integer.parseInt(tmp.substring(2, 4));

        String str = tmp.substring(4);
        str = "20" + str;

        Integer year = Integer.parseInt(str);
        try {
            LocalDate localDate = new LocalDate(year, monthOfYear, dayOfMonth);
            return localDate;
        } catch (Exception ex) {
            return null;
        }

    }

    public LocalDate getNextDueDate() {
        String tmp = parser(SAVED_NEXT_DUE_DATE);
        if (tmp == null) {
            return null;
        }
        try {
            Integer dayOfMonth = Integer.parseInt(tmp.substring(0, 2));
            Integer monthOfYear = Integer.parseInt(tmp.substring(2, 4));
            Integer year = Integer.parseInt(tmp.substring(4));

            LocalDate localDate = new LocalDate(year, monthOfYear, dayOfMonth);
            return localDate;
        } catch (Exception ex) {
            return null;
        }

    }

    public Double getPrincipalBalance() {
        String tmp = parser(SAVED_PRINCIPAL_BALANCE);
        if (tmp == null) {
            return null;
        }
        return Double.parseDouble(tmp);
    }

    public Double getPaymentAmount() {
        String tmp = parser(IVR2_ASKED_TO_CHANGE_PAYMENT_AMOUNT);
        if (tmp == null) {
            return null;
        }
        return Double.parseDouble(tmp);
    }

    public Double getLateFeeAmount() {
        String tmp = parser(IVR2_LATE_FEE_AMOUNT);
        if (tmp == null) {
            return null;
        }
        return Double.parseDouble(tmp);
    }

    public boolean getApplyLateFee() {
        String value = parser(IVR2_APPLY_LATE_FEE);
        if (value != null) {
            return Boolean.valueOf(value);
        }
        return false;
    }

    private String parser(String key) {
        log.info("Parsing {} for {}", text, key);
        try {
            String temp = text.substring(text.lastIndexOf(key) + key.length());
            if (temp.contains("-")) {
                temp = temp.substring(0, temp.indexOf("-"));
            }
            log.info("Parsed {} for {} found: {}", text, key, temp);
            return temp;
        } catch (Exception ex) {
            return null;
        }
    }

}
