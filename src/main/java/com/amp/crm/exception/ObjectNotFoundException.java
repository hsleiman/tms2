/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.exception;

import javax.xml.ws.WebFault;

@WebFault(targetNamespace = "http://exception.crm.com")
public class ObjectNotFoundException extends CrmRuntimeException {


    public ObjectNotFoundException(long loanPk) {
        super("Account with pk [" + loanPk + "] could not be found");
        faultInfo = new WebFaultBean();
        faultInfo.setLoanPk(loanPk);
    }
    
    public ObjectNotFoundException(String msg) {
        super(msg);
    }
    
    public ObjectNotFoundException(long pk, Class<?> clazz) {
        super(clazz.getSimpleName() + " with pk [" + pk + "] could not be found");
    }
}


