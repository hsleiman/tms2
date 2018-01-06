/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.param;

import com.amp.tms.enumerated.AgentState;

/**
 *
 * @author hsleiman
 */
public class PhoneActiveStatusParam extends AbstractParam<AgentState>{

    public PhoneActiveStatusParam(String i){
        super(i);
    }
    
    @Override
    protected AgentState parse(String param) throws Throwable {
        return AgentState.valueOf(param);
    }
}
