/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author David
 */
@NamedQueries({
        @NamedQuery(
            name = "DialerGroup.LocateByPk",
            query = "SELECT s FROM DialerGroup s WHERE s.pk = :pk"
        ),
        @NamedQuery(
            name = "DialerGroup.locateByDialerGroupName",
            query = "SELECT s FROM DialerGroup s WHERE s.groupName = :groupName"
        ),
        @NamedQuery(
            name = "DialerGroup.LocateAll",
            query = "SELECT s FROM DialerGroup s ORDER BY s.pk ASC"
        )      
})
@Entity
@Table(schema = "sti")
@XmlAccessorType(XmlAccessType.FIELD)
public class DialerGroup extends SuperEntity {
    @Column(nullable = false,unique = true)
    private String groupName;
    private String leaderName;  
    private String lastChangedBy;
    private Boolean isActive;
    private String comment;
    
    @XmlTransient
    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "dialerGroup" , orphanRemoval=true)
    Set<AgentDialerGroup> dialerGroupAgents = new HashSet<>();
    
    /*@XmlTransient
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "dialerGroup" , orphanRemoval=true)
    Set<DialerGroupToGroups> dialerGroupToGroups = new HashSet<>();
    
    @XmlTransient
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "svDailerGroups")
    Set<DialerGroupToGroups> dialerGroupsToGroup = new HashSet<>();
    */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="dialergroup_dialergroups",
     joinColumns=@JoinColumn(name="subGroupPk"),
     inverseJoinColumns=@JoinColumn(name="superGroupPk")
    )
    private Set<DialerGroup> subDialerGroups = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="dialergroup_dialergroups",
     joinColumns=@JoinColumn(name="superGroupPk"),
     inverseJoinColumns=@JoinColumn(name="subGroupPk")
    )
    private Set<DialerGroup> superDialerGroups = new HashSet<>();
    
    
    public DialerGroup(){
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Set<AgentDialerGroup> getDialerGroupAgents() {
        return dialerGroupAgents;
    }

    public void setDialerGroupAgents(Set<AgentDialerGroup> dialerGroupAgents) {
        this.dialerGroupAgents = dialerGroupAgents;
    }

    /*public Set<DialerGroupToGroups> getDialerGroupToGroups() {
    return dialerGroupToGroups;
    }
    public void setDialerGroupToGroups(Set<DialerGroupToGroups> dialerGroupToGroups) {
    this.dialerGroupToGroups = dialerGroupToGroups;
    }
    public Set<DialerGroupToGroups> getDialerGroupsToGroup() {
    return dialerGroupsToGroup;
    }
    public void setDialerGroupsToGroup(Set<DialerGroupToGroups> dialerGroupsToGroup) {
    this.dialerGroupsToGroup = dialerGroupsToGroup;
    }
     */
    public Set<DialerGroup> getSubDialerGroups() {
        return subDialerGroups;
    }

    public void setSubDialerGroups(Set<DialerGroup> subDialerGroups) {
        this.subDialerGroups = subDialerGroups;
    }

    public Set<DialerGroup> getSuperDialerGroups() {
        return superDialerGroups;
    }

    public void setSuperDialerGroups(Set<DialerGroup> superDialerGroups) {
        this.superDialerGroups = superDialerGroups;
    }
    
    
    
    
}