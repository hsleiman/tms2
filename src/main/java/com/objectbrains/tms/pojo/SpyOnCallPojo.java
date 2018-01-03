/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.pojo;

import com.google.gson.annotations.Expose;

/**
 *
 * @author hsleiman
 */
public class SpyOnCallPojo extends StatusPojo{
    @Expose
    private int calleeExt;
    @Expose
    private String uuid;
    @Expose
    private String arg;

    public int getCalleeExt() {
        return calleeExt;
    }

    public void setCalleeExt(int calleeExt) {
        this.calleeExt = calleeExt;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }
    
    
    
    
}
