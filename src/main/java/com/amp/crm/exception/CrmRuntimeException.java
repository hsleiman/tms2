/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.exception;

import javax.xml.ws.WebFault;

/**
 *
 * 
 */
@WebFault(targetNamespace = "http://exception.svc.objectbrains.com")
public class CrmRuntimeException extends RuntimeException {

    protected WebFaultBean faultInfo;

    public CrmRuntimeException() {
        this.faultInfo = new WebFaultBean();
    }

    public CrmRuntimeException(String message, int faultCode) {
        super(message);
        this.faultInfo = new WebFaultBean(faultCode, message);
    }

    public CrmRuntimeException(String message, WebFaultBean faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public CrmRuntimeException(String message, Throwable cause, WebFaultBean faultInfo) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    public CrmRuntimeException(Throwable cause, WebFaultBean faultInfo) {
        super(cause);
        this.faultInfo = faultInfo;
    }

    public CrmRuntimeException(Throwable cause) {
        super(cause);
        this.faultInfo = new WebFaultBean();
        this.faultInfo.setFaultString(cause.getMessage());
    }

    public CrmRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.faultInfo = new WebFaultBean();
        this.faultInfo.setFaultString(message);
    }

    public CrmRuntimeException(String message) {
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
