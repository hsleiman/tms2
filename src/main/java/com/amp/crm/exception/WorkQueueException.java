/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.exception;

import javax.xml.ws.WebFault;

@WebFault(targetNamespace = "http://exception.crm.com")
public class WorkQueueException extends CrmRuntimeException {

    public WorkQueueException() {
    }

    public WorkQueueException(String message) {
        super(message);
    }

    public WorkQueueException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkQueueException(Throwable cause) {
        super(cause);
    }
    
}