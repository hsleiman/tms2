/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.agent;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.superentity.SuperEntity;
import com.amp.crm.embeddable.WeightedPriority;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * 
 */
@NamedQueries({
        @NamedQuery(
            name = "AgentDialerGroup.LocateByAgentPk",
            query = "SELECT s FROM AgentDialerGroup s WHERE s.dialerAgent.pk = :agentPk"
        ),
    @NamedQuery(
            name = "AgentDialerGroup.locateByDialerGroupPk",
            query = "SELECT s FROM AgentDialerGroup s WHERE s.dialerGroup.pk = :dialerGroupPk"
        ),
    @NamedQuery(
            name = "AgentDialerGroup.LocateByAgentPkAndGroupPk",
            query = "SELECT s FROM AgentDialerGroup s WHERE s.dialerAgent.pk = :agentPk AND s.dialerGroup.pk = :dialerGroupPk"
        )
})
@Entity
@Table(name = "agent_dialer_group" , schema = "crm")
public class AgentDialerGroup extends SuperEntity{
      
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agent_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_agent_dialer_group_pk")
    private Agent dialerAgent;
     
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dialer_group_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_dialer_group_agent_pk")
    private DialerGroup dialerGroup;
    
    private String createdBy;
    private Boolean leader = Boolean.FALSE;
    private Boolean allowAfterHours = Boolean.FALSE;
    
    @Embedded
    private WeightedPriority weightedPriority;
    
    
    public AgentDialerGroup(){
    }

    public Agent getDialerAgent() {
        return dialerAgent;
    }

    public void setDialerAgent(Agent dialerAgent) {
        this.dialerAgent = dialerAgent;
    }

    public DialerGroup getDialerGroup() {
        return dialerGroup;
    }

    public void setDialerGroup(DialerGroup dialerGroup) {
        this.dialerGroup = dialerGroup;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public WeightedPriority getWeightedPriority() {
        if (weightedPriority == null) weightedPriority = new WeightedPriority();
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

}
