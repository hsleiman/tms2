/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.exception;

import javax.xml.bind.annotation.XmlRootElement;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */
@XmlRootElement
public class WebFaultBean {
    
    private int faultCode;
    private String faultString;
    private String stackTrace;
    private long loanPk;
    private LocalDateTime dateTime;
    
    public WebFaultBean() {
        this.dateTime = LocalDateTime.now();
    }
    
    public WebFaultBean(int faultCode,String faultString){
        this();
        this.faultCode = faultCode;
        this.faultString = faultString;
    }

    public int getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(int faultCode) {
        this.faultCode = faultCode;
    }

    public String getFaultString() {
        return faultString;
    }

    public void setFaultString(String faultString) {
        this.faultString = faultString;
    } 

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String StackTrace) {
        this.stackTrace = StackTrace;
    }

    public long getLoanPk() {
        return loanPk;
    }

    public void setLoanPk(long loanPk) {
        this.loanPk = loanPk;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
