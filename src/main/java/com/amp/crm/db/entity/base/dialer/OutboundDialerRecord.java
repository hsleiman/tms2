/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.db.entity.base.dialer;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.superentity.SuperEntity;
import com.amp.crm.embeddable.AgentWeightPriority;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import org.joda.time.LocalDateTime;


@Entity
@Table(schema = "crm")
public class OutboundDialerRecord extends SuperEntity{
    private String UUID;
    private long dqPk;
    private boolean isComplete = false;
    private LocalDateTime completeTime;
    
    @ElementCollection(targetClass = AgentWeightPriority.class, fetch = FetchType.EAGER)
    @CollectionTable(schema = "crm", name = "agent_weight_priority", joinColumns = @JoinColumn(name = "outbound_dialer_record_pk"))
    @OrderColumn(name = "order_index")
    private List<AgentWeightPriority> agentWeightPriority = new ArrayList<>();
    
    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "outboundDialerRecord")
    private Set<OutboundAccountDetails> accountDetails = new HashSet<>(0);

    public OutboundDialerRecord(long dqPk){
        setDqPk(dqPk);
        setUUID("OBDialerRec_ "+dqPk+"_"+(new LocalDateTime())+"_"+java.util.UUID.randomUUID());
    }
    
    public OutboundDialerRecord(){
        
    }
    public long getDqPk() {
        return dqPk;
    }

    public void setDqPk(long dqPk) {
        this.dqPk = dqPk;
    }

      
    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public boolean isIsComplete() {
        return isComplete;
    }

    public void setIsComplete(boolean isComplete) {
        this.isComplete = isComplete;
        if (isComplete){
            this.completeTime = new LocalDateTime();
        }
    }

    public List<AgentWeightPriority> getAgentWeightPriority() {
        return agentWeightPriority;
    }

    public void setAgentWeightPriority(List<AgentWeightPriority> agentWeightPriority) {
        this.agentWeightPriority = agentWeightPriority;
    }

    public Set<OutboundAccountDetails> getAccountDetails() {
        return accountDetails;
    }

    public void setAccountDetails(Set<OutboundAccountDetails> accountDetails) {
        this.accountDetails = accountDetails;
    }
    
}
