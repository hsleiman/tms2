/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.enumerated.refrence;

/**
 *
 * @author hsleiman
 */
public enum IVROrder3 {

    
    checkSecurityForLoan
    ;

    private final String method;

    private IVROrder3() {
        this.method = this.name();
    }

    public String getMethodName() {
        return method;
    }
}
