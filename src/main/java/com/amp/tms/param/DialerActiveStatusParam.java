/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.param;

import com.amp.tms.enumerated.DialerActiveStatus;

/**
 *
 * 
 */
public class DialerActiveStatusParam extends AbstractParam<DialerActiveStatus>{

    public DialerActiveStatusParam(String i){
        super(i);
    }
    
    @Override
    protected DialerActiveStatus parse(String param) throws Throwable {
        return DialerActiveStatus.valueOf(param);
    }
}
