/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.exception;

import javax.xml.ws.WebFault;

/**
 *
 * @author Hoang, J, Bishistha
 */
@WebFault(targetNamespace = "http://exception.sti.objectbrains.com")
public class StiException  extends Exception{
    protected WebFaultBean faultInfo;

    public StiException() {
        this.faultInfo = new WebFaultBean();
    }

    public StiException(String message, int faultCode) {
        super(message);
        this.faultInfo = new WebFaultBean(faultCode, message);
    }

    public StiException(String message, WebFaultBean faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public StiException(String message, Throwable cause, WebFaultBean faultInfo) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    public StiException(Throwable cause, WebFaultBean faultInfo) {
        super(cause);
        this.faultInfo = faultInfo;
    }

    public StiException(Throwable cause) {
        super(cause);
        this.faultInfo = new WebFaultBean();
        this.faultInfo.setFaultString(cause.getMessage());
    }

    public StiException(String message, Throwable cause) {
        super(message, cause);
        this.faultInfo = new WebFaultBean();
        this.faultInfo.setFaultString(message);
    }

    public StiException(String message) {
        super(message);
        this.faultInfo = new WebFaultBean();
        this.faultInfo.setFaultString(message);
    }

    public WebFaultBean getFaultInfo() {
        return faultInfo;
    }
    
    public void setFaultInfo(WebFaultBean faultInfo) {
        this.faultInfo = faultInfo;
    }
}
