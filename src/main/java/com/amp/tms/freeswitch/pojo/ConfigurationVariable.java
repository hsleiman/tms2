/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.pojo;

import com.google.gson.annotations.Expose;
import com.amp.tms.freeswitch.FreeswitchVariables;
import javax.ws.rs.FormParam;

/**
 *
 * 
 */
public class ConfigurationVariable {

    @Expose
    @FormParam(FreeswitchVariables.key_value)
    private String key_value;

    public String getKey_value() {
        return key_value;
    }

    public void setKey_value(String key_value) {
        this.key_value = key_value;
    }

}
