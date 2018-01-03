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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;
import org.joda.time.LocalDate;

/**
 *
 * @author David
 */
@NamedQueries({
        @NamedQuery(
            name = "Team.LocateByPk",
            query = "SELECT s FROM Team s WHERE s.pk = :pk"
        ),
        @NamedQuery(
            name = "Team.locateByTeamName",
            query = "SELECT s FROM Team s WHERE s.teamName = :teamName"
        )
})
@Entity
@Table(schema = "sti", uniqueConstraints = {           
            @UniqueConstraint(name = "teamName", columnNames = {"teamName"}),
            @UniqueConstraint(name = "leader_pk", columnNames = {"leader_pk"}),
            @UniqueConstraint(name = "manager1_pk", columnNames = {"manager1_pk"}),
            @UniqueConstraint(name = "manager2_pk", columnNames = {"manager2_pk"})
           // @UniqueConstraint(name = "leader_user_name", columnNames = {"leader_user_name"}),
           // @UniqueConstraint(name = "manager1_user_name", columnNames = {"manager1_user_name"}),
           // @UniqueConstraint(name = "manager2_user_name", columnNames = {"manager2_user_name"})
        })

@XmlAccessorType(XmlAccessType.FIELD)
public class Team extends SuperEntity implements java.io.Serializable{
    @Column(nullable = false,unique = true)
    private String teamName;
    private String manager1;
    private String manager2;
    private String leaderName;  
    private String lastChangedBy;
    private Boolean isActive;
    private Boolean isCurrent; 
    private LocalDate manager1EffectiveDate;
    private LocalDate manager2EffectiveDate;
    private String comment;
    
    
    @JsonIgnore
    @XmlTransient
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "team")
    private Set<Agent> Agents = new HashSet<>(0);
    
    @JsonIgnore
    @XmlTransient
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_pk", referencedColumnName = "pk" , unique = true)
    @ForeignKey(name="fk_team_leader")
    private Agent teamLeader;
    
    @JsonIgnore
    @XmlTransient
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager1_pk", referencedColumnName = "pk" , unique = true)
    @ForeignKey(name="fk_team_manager1")
    private Agent teamManager1;
    
    @JsonIgnore
    @XmlTransient
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager2_pk", referencedColumnName = "pk" , unique = true)
    @ForeignKey(name="fk_team_manager2")
    private Agent teamManager2;

    public Team(){
        this.setIsCurrent(true);
        
    }
    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getManager1() {
        return manager1;
    }

    public void setManager1(String manager1) {
        this.manager1 = manager1;
    }

    public String getManager2() {
        return manager2;
    }

    public void setManager2(String manager2) {
        this.manager2 = manager2;
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

    public Boolean isIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getManager1EffectiveDate() {
        return manager1EffectiveDate;
    }

    public void setManager1EffectiveDate(LocalDate manager1EffectiveDate) {
        this.manager1EffectiveDate = manager1EffectiveDate;
    }

    public LocalDate getManager2EffectiveDate() {
        return manager2EffectiveDate;
    }

    public void setManager2EffectiveDate(LocalDate manager2EffectiveDate) {
        this.manager2EffectiveDate = manager2EffectiveDate;
    }

    
    
    

    public Set<Agent> getAgents() {
        return Agents;
    }

    public void setAgents(Set<Agent> Agents) {
        this.Agents = Agents;
    }

    public Agent getteamLeader() {
        return teamLeader;
    }

    public void setteamLeader(Agent teamLeader) {
        this.teamLeader = teamLeader;
    }
    
    public Agent getteamManager1() {
        return teamManager1;
    }

    public void setteamManager1(Agent teamManager1) {
        this.teamManager1 = teamManager1;
    }

    public Agent getteamManager2() {
        return teamManager2;
    }

    public void setteamManager2(Agent teamManager2) {
        this.teamManager2 = teamManager2;
    }
    
  
}
