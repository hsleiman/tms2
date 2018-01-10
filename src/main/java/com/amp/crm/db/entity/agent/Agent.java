/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.amp.crm.db.entity.base.WorkQueue;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.superentity.SuperEntity;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.envers.AuditTable;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 * 
 */
@NamedQueries({
        @NamedQuery(
                name = "Agent.LocateByPk",
                query = "SELECT s FROM Agent s WHERE s.pk = :pk"
        ),
        @NamedQuery(
                name = "Agent.locateByAgentUserName",
                query = "SELECT s FROM Agent s WHERE LOWER(s.userName) = LOWER(:userName)"
        ),
        @NamedQuery(
                name = "Agent.getAllAgents",
                query = "SELECT s FROM Agent s"
        ),
        @NamedQuery(
                name = "Agent.getAllAgentsForManager",
                query = "SELECT s FROM Agent s WHERE s.manager1ToTeam.manager1 = :manager1"
        ),
        @NamedQuery(
                name = "Agent.locateByAgentPhoneExtension",
                query = "SELECT s FROM Agent s WHERE s.extension = :phoneExtension"
        )
})
@Entity
@AuditTable(value = "agent_history", schema = "crm")
//@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(name = "agent", schema = "crm", uniqueConstraints = {
        @UniqueConstraint(name = "userName", columnNames = {"userName"})
})

@XmlAccessorType(XmlAccessType.FIELD)
public class Agent extends SuperEntity {

    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "secondaryAgent")
    Set<AgentQueue> AgentQueues = new HashSet<>();
    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "dialerAgent")
    Set<AgentDialerGroup> AgentDialerGroups = new HashSet<>();
    @Column(nullable = false, unique = true)
    private String userName;
    private String firstName;
    private String lastName;
    private String lastChangedBy;
    private String emailAddress;
    private Long phoneNumber;
    @Column(unique = true)
    private Long extension;
    private Boolean isActive;
    private Boolean isCurrent;
    private Boolean isCancelled;
    private String primaryQueueName;
    private Integer type;
    private LocalDate effectiveDate;
    private LocalDate pointsAdjustmentDate;
    private String comment;
    private Integer lastReturnedSortNumberForLoan = 0;


//    @XmlTransient
//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "AgentAssignedToLoan")
//    private Set<SvLoan> svLoans = new HashSet<>(0);
    private LocalDateTime lastAccessTime;

    /* Address of an Agent is not stored anywhere currently. May be this can be implemented in phase 2 if required? 
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "AgentAddress")
    private Set<SvAddress> svAddresses = new HashSet<>(0);
    */
    private String effectiveCallerId;
    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "agentAssignedToAccount")
    private Set<Account> account = new HashSet<>(0);
    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "teamLeader")
    private Team leaderToTeam;
    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "teamManager1")
    private Team manager1ToTeam;
    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "teamManager2")
    private Team manager2ToTeam;
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_name", referencedColumnName = "teamName")
    @ForeignKey(name = "fk_agent_team")
    private Team team;
    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "queueSupervisor")
    private Set<WorkQueue> supervisorQueues = new HashSet<>(0);

    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "primaryAgent")
    private Set<WorkQueue> primaryQueues = new HashSet<>(0);

    public Agent() {
        this.setIsCurrent(true);
    }

    public String getEffectiveCallerId() {
        return effectiveCallerId;
    }

    public void setEffectiveCallerId(String effectiveCallerId) {
        this.effectiveCallerId = effectiveCallerId;
    }

    public String getUserName() {
        return userName;
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLastChangedBy() {
        return lastChangedBy;
    }

    public void setLastChangedBy(String lastChangedBy) {
        this.lastChangedBy = lastChangedBy;
    }

    public Boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean isIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public Boolean isIsCancelled() {
        return isCancelled;
    }

    public void setIsCancelled(Boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getPointsAdjustmentDate() {
        return pointsAdjustmentDate;
    }

    public void setPointsAdjustmentDate(LocalDate pointsAdjustmentDate) {
        this.pointsAdjustmentDate = pointsAdjustmentDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPrimaryQueueName() {
        return primaryQueueName;
    }

    public void setPrimaryQueueName(String primaryQueueName) {
        this.primaryQueueName = primaryQueueName;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public Set<Account> getAccount() {
        return account;
    }

    public void setAccount(Set<Account> account) {
        this.account = account;
    }


    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Team getLeaderToTeam() {
        return leaderToTeam;
    }

    public void setLeaderToTeam(Team leaderToTeam) {
        this.leaderToTeam = leaderToTeam;
    }

    public Team getManager1ToTeam() {
        return manager1ToTeam;
    }

    public void setManager1ToTeam(Team manager1ToTeam) {
        this.manager1ToTeam = manager1ToTeam;
    }

    public Team getManager2ToTeam() {
        return manager2ToTeam;
    }

    public void setManager2ToTeam(Team manager2ToTeam) {
        this.manager2ToTeam = manager2ToTeam;
    }

    public Set<WorkQueue> getSupervisorQueues() {
        return supervisorQueues;
    }

    public void setSupervisorQueues(Set<WorkQueue> supervisorQueues) {
        this.supervisorQueues = supervisorQueues;
    }

    public Set<WorkQueue> getPrimaryQueues() {
        return primaryQueues;
    }

    public void setPrimaryQueues(Set<WorkQueue> primaryQueues) {
        this.primaryQueues = primaryQueues;
    }

    public Set<AgentQueue> getAgentQueues() {
        return AgentQueues;
    }

    public void setAgentQueues(Set<AgentQueue> AgentQueues) {
        this.AgentQueues = AgentQueues;
    }

    public Set<AgentDialerGroup> getAgentDialerGroups() {
        return AgentDialerGroups;
    }

    public void setAgentDialerGroups(Set<AgentDialerGroup> AgentDialerGroups) {
        this.AgentDialerGroups = AgentDialerGroups;
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

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getPhoneExtension() {
        return extension;
    }

    public void setPhoneExtension(Long phoneExtension) {
        this.extension = phoneExtension;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getLastReturnedSortNumberForLoan() {
        return lastReturnedSortNumberForLoan;
    }

    public void setLastReturnedSortNumberForLoan(Integer lastReturnedSortNumberForLoan) {
        this.lastReturnedSortNumberForLoan = lastReturnedSortNumberForLoan;
    }
    
    /*public Set<SvLoan> getLoansInAgentPrimaryQueue(){
    if(svPrimaryAgentQueue == null){
    return null;
    }
    return 
    }*/
//    public Set<CollectionQueue> getSvPrimaryQueues() {
//        return svPrimaryQueues;
//    }
//
//    public void setSvPrimaryQueues(Set<CollectionQueue> svPrimaryQueues) {
//        this.svPrimaryQueues = svPrimaryQueues;
//    }
}

