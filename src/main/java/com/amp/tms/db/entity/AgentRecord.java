/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.entity;

import com.objectbrains.ams.iws.User;
import com.amp.tms.hazelcast.entity.AgentTMS;
import java.io.Serializable;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.hibernate.annotations.NaturalId;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.LocalDateTime;

/**
 *
 * @author connorpetty
 */
@Entity(name = "AgentRecord")
@Table(schema = "sti")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
//@AuditTable(value = "tms_agent_history", schema = "sti")
public class AgentRecord implements Serializable {

    @Id
    private Integer extension;

    @NaturalId(mutable = true)
    private String userName;

    @Embedded
    private AgentTMS info;

    @OneToOne
    @JoinColumn(name = "agent_stats_pk", referencedColumnName = "pk")
    private AgentStatsRecord currentAgentStats;

    @OneToMany(mappedBy = "agent", cascade = CascadeType.REMOVE)
    private Set<AgentCallRecord> callRecords;

    private LocalDateTime createdDate;

    public AgentRecord() {
    }

    public AgentRecord(User user) {
        this.extension = user.getExtension();
        this.userName = user.getUserName();
        this.info = new AgentTMS(user);
    }

    public Integer getExtension() {
        return extension;
    }

    public void setExtension(Integer extension) {
        this.extension = extension;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public AgentTMS getInfo() {
        return info;
    }

    public void setInfo(AgentTMS info) {
        this.info = info;
    }

    public AgentStatsRecord getCurrentAgentStats() {
        return currentAgentStats;
    }

    public void setCurrentAgentStats(AgentStatsRecord currentAgentStats) {
        this.currentAgentStats = currentAgentStats;
    }

    public Set<AgentCallRecord> getCallRecords() {
        return callRecords;
    }

    public void setCallRecords(Set<AgentCallRecord> callRecords) {
        this.callRecords = callRecords;
    }

    @PrePersist
    protected void onCreate() {
        createdDate = new LocalDateTime();
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + java.util.Objects.hashCode(this.extension);
        hash = 17 * hash + java.util.Objects.hashCode(this.userName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AgentRecord other = (AgentRecord) obj;
        if (!java.util.Objects.equals(this.extension, other.extension)) {
            return false;
        }
        if (!java.util.Objects.equals(this.userName, other.userName)) {
            return false;
        }
        return true;
    }

}
