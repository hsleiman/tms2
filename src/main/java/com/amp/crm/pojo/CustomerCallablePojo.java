/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import com.amp.crm.constants.DoNotCallCodes;
import org.joda.time.LocalDateTime;

/**
 *
 * @author David
 */
public class CustomerCallablePojo {
    private LocalDateTime rightNow;
    private LocalDateTime tooEarly;
    private LocalDateTime tooLate;
    private int doNotCallCode;

    public LocalDateTime getRightNow() {
        return rightNow;
    }

    public void setRightNow(LocalDateTime rightNow) {
        this.rightNow = rightNow;
    }

    public LocalDateTime getTooEarly() {
        return tooEarly;
    }

    public void setTooEarly(LocalDateTime tooEarly) {
        this.tooEarly = tooEarly;
    }

    public LocalDateTime getTooLate() {
        return tooLate;
    }

    public void setTooLate(LocalDateTime tooLate) {
        this.tooLate = tooLate;
    }

    public int getDoNotCallCode() {
        return doNotCallCode;
    }
    
    public String getDoNotCallCodeDesc(){
        return DoNotCallCodes.getCodeDesciption(doNotCallCode);
    }

    public void setDoNotCallCode(int doNotCallCode) {
        this.doNotCallCode = doNotCallCode;
    }
}