/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.amp.crm.pojo.DialerQueueAccountDetails;
import java.io.Serializable;

/**
 *
 * @author connorpetty
 */
public class CompletedLoan implements Serializable {

    private DialerQueueAccountDetails details;
    private boolean succeded;

    public CompletedLoan(DialerQueueAccountDetails details, boolean succeded) {
        this.details = details;
        this.succeded = succeded;
    }

    public DialerQueueAccountDetails getDetails() {
        return details;
    }

    public void setDetails(DialerQueueAccountDetails details) {
        this.details = details;
    }

    public boolean isSucceded() {
        return succeded;
    }

    public void setSucceded(boolean succeded) {
        this.succeded = succeded;
    }

}
