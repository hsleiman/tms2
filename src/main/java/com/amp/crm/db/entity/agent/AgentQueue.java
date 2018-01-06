/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.base.WorkQueue;
import com.amp.crm.db.entity.superentity.SuperEntity;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;
import org.joda.time.LocalDate;

/**
 *
 * @author David
 */
@NamedQueries({
        @NamedQuery(
            name = "AgentQueue.LocateByAgentPk",
            query = "SELECT s FROM AgentQueue s WHERE s.secondaryAgent.pk = :agentPk"
        ),
    @NamedQuery(
            name = "AgentQueue.locateByQueuePk",
            query = "SELECT s FROM AgentQueue s WHERE s.secondaryQueue.pk = :queuePk"
        ),
    @NamedQuery(
            name = "AgentQueue.LocateByAgentPkAndQueuePk",
            query = "SELECT s FROM AgentQueue s WHERE s.secondaryAgent.pk = :agentPk AND s.secondaryQueue.pk = :queuePk"
        )
})
@Entity
@Table(name = "agent_queue" , schema = "sti")
public class AgentQueue extends SuperEntity{
    
    @XmlTransient
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agent_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_agent_queue_secondary_pk")
    @JsonIgnore
    private Agent secondaryAgent;
    
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "queue_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_queue_agent_secondary_pk")
    private WorkQueue secondaryQueue;
    
    private LocalDate assignedUntil;
    private String createdBy;
    private Boolean allowCalls = false;
    
    
    public AgentQueue(){
    }

    public Agent getSecondaryAgent() {
        return secondaryAgent;
    }

    public void setSecondaryAgent(Agent secondaryAgent) {
        this.secondaryAgent = secondaryAgent;
    }

    public WorkQueue getSecondaryQueue() {
        return secondaryQueue;
    }

    public void setSecondaryQueue(WorkQueue secondaryQueue) {
        this.secondaryQueue = secondaryQueue;
    }

    public LocalDate getAssignedUntil() {
        return assignedUntil;
    }

    public void setAssignedUntil(LocalDate assignedUntil) {
        this.assignedUntil = assignedUntil;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    //Added allowCalls so Secondary Agents do not need to receive inbound calls
    public Boolean getAllowCalls() {
        return allowCalls;
    }

    public void setAllowCalls(Boolean allowCalls) {
        this.allowCalls = allowCalls;
    }
    
}
