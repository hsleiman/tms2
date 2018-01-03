/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.report;

import com.objectbrains.sti.db.entity.agent.Agent;
import com.objectbrains.sti.embeddable.AgentStats;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author connorpetty
 */
@Entity(name = "AgentStats")
@Table(schema = "sti")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@AuditTable(value = "agent_stats_history", schema = "sti")
public class AgentStatsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Embedded
    private AgentStats info;

    @ManyToOne
    @JoinColumn(name = "phoneExtension")
    private Agent agent;

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public AgentStats getInfo() {
        return info;
    }

    public void setInfo(AgentStats info) {
        this.info = info;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

}
