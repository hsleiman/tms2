/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.dialer;

import com.objectbrains.sti.constants.CallTimeCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.LocalDateTime;

/**
 *
 * @author David
 */
public class PhoneNumberCallable {
    
    private CallTimeCode callTimeCode;
    private LocalDateTime earliestTimeToCall;

    public PhoneNumberCallable() {
    }

    public PhoneNumberCallable(CallTimeCode callTimeCode, LocalDateTime earliestTimeToCall) {
        this.callTimeCode = callTimeCode;
        this.earliestTimeToCall = earliestTimeToCall;
    }
    
    public CallTimeCode getCallTimeCode() {
        return callTimeCode;
    }

    public void setCallTimeCode(CallTimeCode callTimeCode) {
        this.callTimeCode = callTimeCode;
    }

    public LocalDateTime getEarliestTimeToCall() {
        return earliestTimeToCall;
    }

    public void setEarliestTimeToCall(LocalDateTime earliestTimeToCall) {
        this.earliestTimeToCall = earliestTimeToCall;
    }
    
    @Override
    public String toString(){
        return ToStringBuilder.reflectionToString(this);
    }
}

