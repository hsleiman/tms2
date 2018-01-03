/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.objectbrains.sti.pojo;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author raine.cabal
 */
public class UserData {
    
    private String userName;
    private String originatingCompanyId;
    private String orgName;
    private String source;
    private Boolean bypassSecurity;
    
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOriginatingCompanyId() {
        return originatingCompanyId;
    }

    public void setOriginatingCompanyId(String originatingCompanyId) {
        this.originatingCompanyId = originatingCompanyId;
    }

    public Boolean isBypassSecurity() {
        return bypassSecurity == null ? true : bypassSecurity;
    }

    public void setBypassSecurity(Boolean bypassSecurity) {
        this.bypassSecurity = bypassSecurity;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    public UserData toSvcUserData(){
        UserData userData = new UserData();
        userData.setUserName(getUserName());
        return userData;       
    }

}
