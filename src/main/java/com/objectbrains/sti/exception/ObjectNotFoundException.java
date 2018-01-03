/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.exception;

import javax.xml.ws.WebFault;

/**
 *
 * @author David
 */
@WebFault(targetNamespace = "http://exception.svc.objectbrains.com")
public class ObjectNotFoundException extends StiRuntimeException {


    public ObjectNotFoundException(long loanPk) {
        super("Loan with pk [" + loanPk + "] could not be found");
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


