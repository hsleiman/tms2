/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.exception;

import javax.xml.ws.WebFault;

@WebFault(targetNamespace = "http://exception.crm.com")
public class TooManyObjectFoundException extends CrmRuntimeException {
    
    public TooManyObjectFoundException(String msg){
        super(msg);
    }

}