/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.exception;

import javax.xml.ws.WebFault;

@WebFault(targetNamespace = "http://exception.crm.com")
public class AccountNotInQueueException extends CrmException {
    
    public AccountNotInQueueException(long accountPk, long queuePk){
        super("Account with pk [" + accountPk + "] is not in queue with pk [" + queuePk + "]");
        faultInfo = new WebFaultBean();
        faultInfo.setAccountPk(accountPk);
    }
}

