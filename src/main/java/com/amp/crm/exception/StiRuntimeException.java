/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.exception;

import javax.xml.ws.WebFault;

/**
 *
 * @author David
 */
@WebFault(targetNamespace = "http://exception.svc.objectbrains.com")
public class StiRuntimeException extends RuntimeException {

    protected WebFaultBean faultInfo;

    public StiRuntimeException() {
        this.faultInfo = new WebFaultBean();
    }

    public StiRuntimeException(String message, int faultCode) {
        super(message);
        this.faultInfo = new WebFaultBean(faultCode, message);
    }

    public StiRuntimeException(String message, WebFaultBean faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public StiRuntimeException(String message, Throwable cause, WebFaultBean faultInfo) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    public StiRuntimeException(Throwable cause, WebFaultBean faultInfo) {
        super(cause);
        this.faultInfo = faultInfo;
    }

    public StiRuntimeException(Throwable cause) {
        super(cause);
        this.faultInfo = new WebFaultBean();
        this.faultInfo.setFaultString(cause.getMessage());
    }

    public StiRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.faultInfo = new WebFaultBean();
        this.faultInfo.setFaultString(message);
    }

    public StiRuntimeException(String message) {
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
