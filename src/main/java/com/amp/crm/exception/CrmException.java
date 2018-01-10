/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.exception;

import javax.xml.ws.WebFault;

@WebFault(targetNamespace = "http://exception.crm.com")
public class CrmException  extends Exception{
    protected WebFaultBean faultInfo;

    public CrmException() {
        this.faultInfo = new WebFaultBean();
    }

    public CrmException(String message, int faultCode) {
        super(message);
        this.faultInfo = new WebFaultBean(faultCode, message);
    }

    public CrmException(String message, WebFaultBean faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public CrmException(String message, Throwable cause, WebFaultBean faultInfo) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    public CrmException(Throwable cause, WebFaultBean faultInfo) {
        super(cause);
        this.faultInfo = faultInfo;
    }

    public CrmException(Throwable cause) {
        super(cause);
        this.faultInfo = new WebFaultBean();
        this.faultInfo.setFaultString(cause.getMessage());
    }

    public CrmException(String message, Throwable cause) {
        super(message, cause);
        this.faultInfo = new WebFaultBean();
        this.faultInfo.setFaultString(message);
    }

    public CrmException(String message) {
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
