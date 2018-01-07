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
public class TooManyObjectFoundException extends StiRuntimeException {
    
    public TooManyObjectFoundException(String msg){
        super(msg);
    }

}