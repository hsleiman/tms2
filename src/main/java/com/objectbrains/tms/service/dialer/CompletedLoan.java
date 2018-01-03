/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.dialer;

import com.objectbrains.svc.iws.DialerQueueLoanDetails;
import java.io.Serializable;

/**
 *
 * @author connorpetty
 */
public class CompletedLoan implements Serializable {

    private DialerQueueLoanDetails details;
    private boolean succeded;

    public CompletedLoan(DialerQueueLoanDetails details, boolean succeded) {
        this.details = details;
        this.succeded = succeded;
    }

    public DialerQueueLoanDetails getDetails() {
        return details;
    }

    public void setDetails(DialerQueueLoanDetails details) {
        this.details = details;
    }

    public boolean isSucceded() {
        return succeded;
    }

    public void setSucceded(boolean succeded) {
        this.succeded = succeded;
    }

}
