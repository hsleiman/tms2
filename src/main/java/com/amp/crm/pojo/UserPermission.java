/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jaimel
 */
public class UserPermission {
    
    private String agentUsername;
    
    private List<Integer> roleIds = new ArrayList<>();
    
    private List<Integer> includePermissionIds = new ArrayList<>();
    
    private List<Integer> excludePermissionIds = new ArrayList<>();

    public String getAgentUsername() {
        return agentUsername;
    }

    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
    }

    public List<Integer> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Integer> roleIds) {
        this.roleIds = roleIds;
    }

    public List<Integer> getIncludePermissionIds() {
        return includePermissionIds;
    }

    public void setIncludePermissionIds(List<Integer> includePermissionIds) {
        this.includePermissionIds = includePermissionIds;
    }

    public List<Integer> getExcludePermissionIds() {
        return excludePermissionIds;
    }

    public void setExcludePermissionIds(List<Integer> excludePermissionIds) {
        this.excludePermissionIds = excludePermissionIds;
    }

}
