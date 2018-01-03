/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.incoming.service;

import com.objectbrains.tms.enumerated.PaymentType;
import java.math.BigDecimal;

/**
 *
 * @author hsleiman
 */
public class OptionTextDialplanPayment {

    private String str;

    private final String paymentType = "PT";
    private final String creditCardNumber = "CCN";
    private final String creditCardExp = "CCE";
    private final String creditCardZip = "CCZ";
    private final String creditCardCCV = "CCC";
    private final String selectPaymentAmount = "SPE";
    private final String selectedPaymentAmount = "SDPE";
    private final String reviewPaymentEntry = "RPE";
    private final String paymentOnType = "POT";
    private final String paymentAmountOnFile = "POF";

    public OptionTextDialplanPayment(String text) {
        str = text;
    }

    public PaymentType getPaymentType() {
        if (getInfo(paymentType).equalsIgnoreCase("1")) {
            return PaymentType.CREDIT_CARD;
        } else if (getInfo(paymentType).equalsIgnoreCase("2")) {
            return PaymentType.ACH;
        }
        return PaymentType.UNKNOWN;
    }

    public String getCreditCardNumber() {
        return getInfo(creditCardNumber);
    }

    public String getCreditCardExp() {
        return getInfo(creditCardExp);
    }

    public String getCreditCardZip() {
        return getInfo(creditCardZip);
    }

    public String getCreditCardCCV() {
        return getInfo(creditCardCCV);
    }

    public boolean isSystemAmount() {
//        if (getPaymentType() == PaymentType.CREDIT_CARD) {
            if (getInfo(selectPaymentAmount).equalsIgnoreCase("1")) {
                return true;
            }
//        }
        return false;
    }

    public boolean isMonthlyPayment() {
        if (getInfo(paymentOnType).equalsIgnoreCase("MONTHLY")) {
            return true;
        }
        return false;
    }

    public boolean paymentAmountVerified() {
        if (getInfo(reviewPaymentEntry).equalsIgnoreCase("1")) {
            return true;
        }
        return false;
    }

    public BigDecimal getSelectedAmount() {
       // if (getPaymentType() == PaymentType.CREDIT_CARD) {
            if (getInfo(selectPaymentAmount).equalsIgnoreCase("2")) {
                try {
                    String am = getInfo(selectedPaymentAmount);
                    am = am.substring(0, am.length() - 2) + "." + am.substring(am.length() - 2);
                    BigDecimal amount = new BigDecimal(am);
                    return amount;
                } catch (NumberFormatException ex) {
                    return new BigDecimal(0);
                }
            } else {
                String am = getInfo(paymentAmountOnFile);
                BigDecimal amount = new BigDecimal(am);
                return amount;
            }
        //}
        //return new BigDecimal(0);
    }

    private String getInfo(String key) {
        String line = str;
        line = line.substring(line.lastIndexOf(key) + key.length() + 1);
        if (line.contains("-")) {
            line = line.substring(0, line.indexOf("-"));
        }

        return line;

    }
}
