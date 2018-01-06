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
@WebFault(targetNamespace = "http://exception.sti.objectbrains.com")
public class DynamicClassExecutionException extends RuntimeException {

    public String faultInfo;

    public DynamicClassExecutionException(String message) {
        super(message);
        faultInfo = message;
    }

    public DynamicClassExecutionException(Throwable cause) {
        super(cause);
        faultInfo = cause.getMessage();
    }

    public DynamicClassExecutionException(String message, Throwable cause) {
        super(message, cause);
        faultInfo = message;
    }

    public String getFaultInfo() {
        return faultInfo;
    }
    
}
