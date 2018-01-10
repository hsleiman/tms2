/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

public enum PayPeriod {
    MONTHLY (1),
    SEMIMONTHLY (2),
    WEEKLY (3),
    BIWEEKLY (4),
    UNKNOWN (0);
    
    private final int id;

    private PayPeriod(int id) {
        this.id = id;
    }

    public int getPayPeriodId() {
        return id;
    }

    public static PayPeriod getPayPeriodById(int id) {
        for (PayPeriod payPeriod : PayPeriod.values()) {
            if (payPeriod.getPayPeriodId() == id) {
                return payPeriod;
            }
        }
        return null;
    }
}
