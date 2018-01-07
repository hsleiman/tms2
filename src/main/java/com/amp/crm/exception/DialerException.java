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
public class DialerException extends CrmRuntimeException {

    public DialerException() {
    }

    public DialerException(String message) {
        super(message);
    }

    public DialerException(String message, Throwable cause) {
        super(message, cause);
    }

    public DialerException(Throwable cause) {
        super(cause);
    }
    
}