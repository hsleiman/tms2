/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.objectbrains.sti.embeddable;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlElement;



@Embeddable
public class AgentWeightPriority {
    
    private String username;
    private String firstName;
    private String lastName;
    private WeightedPriority weightedPriority;
    @XmlElement(required = true)
    private Boolean leader = false;
    private Boolean allowAfterHours = Boolean.FALSE;
    private long groupPk;
    private Boolean isPrimaryGroup;
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public WeightedPriority getWeightedPriority() {
        return weightedPriority;
    }

    public void setWeightedPriority(WeightedPriority weightedPriority) {
        this.weightedPriority = weightedPriority;
    }

    public Boolean isLeader() {
        return leader;
    }

    public void setLeader(Boolean leader) {
        this.leader = leader;
    }

    public Boolean getAllowAfterHours() {
        return allowAfterHours;
    }

    public void setAllowAfterHours(Boolean allowAfterHours) {
        this.allowAfterHours = allowAfterHours;
    }

    public long getGroupPk() {
        return groupPk;
    }

    public void setGroupPk(long groupPk) {
        this.groupPk = groupPk;
    }

    public Boolean isIsPrimaryGroup() {
        return isPrimaryGroup;
    }

    public void setIsPrimaryGroup(Boolean isPrimaryGroup) {
        this.isPrimaryGroup = isPrimaryGroup;
    }
    
}
