/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.pojo;

import com.objectbrains.sti.embeddable.WeightedPriority;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Zachary Soohoo
 */

public class QueueAgentWeightPriority {
 
    private WeightedPriority weightedPriority;
    @XmlElement(required = true)
    private Boolean leader = false;
    private Boolean allowAfterHours = Boolean.FALSE;
    private long queuePk;
    private long groupPk;
    private Boolean isPrimary;

    public QueueAgentWeightPriority(){
    }

    public QueueAgentWeightPriority(WeightedPriority weightedPriority, long queuePk, Boolean leader, Boolean allowAfterHours, long groupPk, Boolean isPrimary) {
        this.weightedPriority = weightedPriority;
        this.queuePk = queuePk;
        this.leader = leader;
        this.allowAfterHours = allowAfterHours;
        this.groupPk = groupPk;
        this.isPrimary = isPrimary;
    }
    
  
    public WeightedPriority getWeightedPriority() {
        return weightedPriority;
    }

    public void setWeightedPriority(WeightedPriority weightedPriority) {
        this.weightedPriority = weightedPriority;
    }

    public Boolean getLeader() {
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
    
    public long getQueuePk() {
        return queuePk;
    }

    public void setQueuePk(long queuePk) {
        this.queuePk = queuePk;
    }

    public long getGroupPk() {
        return groupPk;
    }

    public void setGroupPk(long groupPk) {
        this.groupPk = groupPk;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
    
}
