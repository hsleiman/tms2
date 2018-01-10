/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.pojo;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.utility.GsonUtility;
import javax.ws.rs.FormParam;

/**
 *
 * 
 */
public class DirectoryVariable {

    @Expose
    @FormParam(FreeswitchVariables.user)
    private String user;
    @Expose
    @FormParam(FreeswitchVariables.domain)
    private String domain;
    @Expose
    @FormParam(FreeswitchVariables.FreeSWITCH_IPv4)
    private String FreeSWITCH_IPv4;
    
    @Expose
    @FormParam(FreeswitchVariables.ip)
    private String userIP;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getFreeSWITCH_IPv4() {
        return FreeSWITCH_IPv4;
    }

    public void setFreeSWITCH_IPv4(String FreeSWITCH_IPv4) {
        this.FreeSWITCH_IPv4 = FreeSWITCH_IPv4;
    }

    public String getUserIP() {
        return userIP;
    }

    public void setUserIP(String userIP) {
        this.userIP = userIP;
    }
    

    public String toJson() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }

}
