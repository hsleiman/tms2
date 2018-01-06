/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.constants.WorkPortfolioType;
import com.amp.crm.db.entity.base.dialer.OutboundDialerQueue;
import com.amp.crm.db.entity.base.dialer.InboundDialerQueue;
import com.amp.crm.db.entity.agent.Agent;
import com.amp.crm.db.entity.agent.AgentQueue;
import com.amp.crm.db.entity.superentity.SuperEntity;
import com.amp.crm.embeddable.WorkQueueData;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * @author David
 */
@NamedQueries({
    @NamedQuery(
            name = "WorkQueue.LocateByPk",
            query = "SELECT c FROM WorkQueue c WHERE c.pk = :pk"
    ),
    @NamedQuery(
            name = "WorkQueue.LocateByQueueName",
            query = "SELECT c FROM WorkQueue c WHERE lower(c.workQueueData.queueName) = lower(:queueName)"
    ),
    @NamedQuery(
            name = "WorkQueue.LocateAll",
            query = "SELECT c FROM WorkQueue c "
    ),
    @NamedQuery(
            name = "WorkQueue.LocateActiveAutoQueues",
            query = "SELECT c FROM WorkQueue c WHERE c.workQueueData.portfolioType IN (100, 200, 210, 220, 230) AND c.workQueueData.active = true AND coalesce(c.workQueueData.manual,false) = false"
    ),
    @NamedQuery(
            name = "WorkQueue.LocateAllActiveQueues",
            query = "SELECT c FROM WorkQueue c WHERE c.workQueueData.active = true"
    ),
    @NamedQuery(
            name = "WorkQueue.LocateDefaultPortfolioQueues",
            query = "SELECT q " +
                    "FROM WorkQueue q " +
                    "WHERE q.workQueueData.portfolioType = :portfolio " +
                        "AND q.workQueueData.defaultPortfolioQueue = true " +
                        "AND q.workQueueData.manual = true "
    )
})
@Entity
@Table(schema = "sti")
public class WorkQueue extends SuperEntity {

    @Embedded
    private WorkQueueData workQueueData;

    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "secondaryQueue" , orphanRemoval=true)
    Set<AgentQueue> agentQueues = new HashSet<>();
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agent_supervisor_pk", referencedColumnName = "pk")
    @ForeignKey(name="fk_supervisor_agent_queue")
    private Agent queueSupervisor;
    
    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "workQueue")
    private InboundDialerQueue inboundDialerQueue;
    
    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "workQueue")
    private OutboundDialerQueue outboundDialerQueue;
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agent_primary_pk", referencedColumnName = "pk")
    @ForeignKey(name="fk_primary_queue_agent")
    private Agent primaryAgent;
    
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "workQueues")
    private Set<Account> accounts = new HashSet<>();
        
    public WorkQueue() {
        
    }
    
    public WorkQueueData getWorkQueueData() {
        return workQueueData;
    }

    public void setWorkQueueData(WorkQueueData workQueueData) {
        this.workQueueData = workQueueData;
    }
    
    /*public Set<SvLoan> getSvLoans() {
        return svLoans;
    }

    public void setSvLoans(Set<SvLoan> svLoans) {
        this.getWorkQueueData().setQueueCount(svLoans.size());
        this.svLoans = svLoans;
    }*/

    public Set<AgentQueue> getAgentQueues() {
        return agentQueues;
    }

    public void setAgentQueues(Set<AgentQueue> agentQueues) {
        this.agentQueues = agentQueues;
    }

    public Agent getQueueSupervisor() {
        return queueSupervisor;
    }

    public void setQueueSupervisor(Agent queueSupervisor) {
        this.queueSupervisor = queueSupervisor;
    }

    public InboundDialerQueue getInboundDialerQueue() {
        return inboundDialerQueue;
    }

    public void setInboundDialerQueue(InboundDialerQueue inboundDialerQueue) {
        this.inboundDialerQueue = inboundDialerQueue;
    }

    public OutboundDialerQueue getOutboundDialerQueue() {
        return outboundDialerQueue;
    }

    public void setOutboundDialerQueue(OutboundDialerQueue outboundDialerQueue) {
        this.outboundDialerQueue = outboundDialerQueue;
    }

    public Agent getPrimaryAgent() {
        return primaryAgent;
    }

    public void setPrimaryAgent(Agent primaryAgent) {
        this.primaryAgent = primaryAgent;
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }    

    public boolean isBackendQueue(){
        return (getWorkQueueData().getPortfolioType() == WorkPortfolioType.WORK_PORTFOLIO_BACK_END ||
                getWorkQueueData().getPortfolioType() == WorkPortfolioType.WORK_PORTFOLIO_BACK_END_2 ||
                getWorkQueueData().getPortfolioType() == WorkPortfolioType.WORK_PORTFOLIO_BACK_END_3 ||
                getWorkQueueData().getPortfolioType() == WorkPortfolioType.WORK_PORTFOLIO_BACK_END_4 );
    }


//    public SvAgent getSvPrimaryAgent() {
//        return svPrimaryAgent;
//    }
//
//    public void setSvPrimaryAgent(SvAgent svPrimaryAgent) {
//        this.svPrimaryAgent = svPrimaryAgent;
//    }

    public void associatePrimaryAgent(Agent agent){
        setPrimaryAgent(agent);
        agent.getPrimaryQueues().add(this);
    }
    
    public void disassociatePrimaryAgent(){
        Agent primary = this.getPrimaryAgent();
        if(primary != null){
            primary.getPrimaryQueues().remove(this);
        }
        setPrimaryAgent(null);       
    }
    
    
    public void associateSupervisor(Agent agent){
        setQueueSupervisor(agent);
        agent.getSupervisorQueues().add(this);
    }
    
    public void disassociateSupervisor(){
        Agent supervisor = this.getQueueSupervisor();
        if(supervisor != null){
            supervisor.getSupervisorQueues().remove(this);
        }
        setQueueSupervisor(null);
    }
    
    
}
