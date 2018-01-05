/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.entity;

import com.objectbrains.tms.hazelcast.entity.AgentCall;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.LocalDateTime;

/**
 *
 * @author connorpetty
 */
@NamedNativeQueries({
    @NamedNativeQuery(name = "AgentCallRecord.AgentCallReport",
            query = "with calls as ("
            + " select distinct"
            + "     call.call_direction as direction, "
            + "     call.call_uuid as call_uuid, "
            + "     call.auto_dialed as auto_dialed"
            + " from tms.tms_agent_call_history call inner join tms.tms_revinfo info on call.revision = info.rev"
            + " where to_timestamp(info.revtstmp/1000) between :startTime and :endTime"
            + " and call.revision_type <> 2"
            + " and call.agent_extension = :extension"
            + " and call.queue_pk = :queuePk"
            + ")"
            + "select "
            + " count(nullif(direction = 'INBOUND', false)) as inboundCallCount,"
            + " count(nullif(direction = 'OUTBOUND' and auto_dialed = true, false)) as dialerCallCount,"
            + " count(nullif(direction = 'OUTBOUND' and auto_dialed = false, false)) as manualCallCount"
            + "from calls"),
    @NamedNativeQuery(name = "AgentCallRecord.AgentCallReports",
            query = "with calls as ("
            + " select distint call.agent_extension as extension, "
            + "     call.call_direction as direction, "
            + "     call.call_uuid as call_uuid, "
            + "     call.auto_dialed as auto_dialed "
            + " from tms.tms_agent_call_history call inner join tms.tms_revinfo info on call.revision = info.rev"
            + " where to_timestamp(info.revtstmp/1000) between :startTime and :endTime"
            + " and call.revision_type <> 2"
            + " and call.agent_extension in (:extensions)"
            + " and call.queuePk = :queuePk"
            + ")"
            + "select extension, "
            + " count(nullif(direction = 'INBOUND', false)) as inboundCallCount,"
            + " count(nullif(direction = 'OUTBOUND' and auto_dialed = true, false)) as dialerCallCount,"
            + " count(nullif(direction = 'OUTBOUND' and auto_dialed = false, false)) as manualCallCount "
            + "from calls "
            + "group by extension"),
    @NamedNativeQuery(name = "AgentCallRecord.AllCallReports",
            query = "with calls as ("
            + " select distinct call.agent_extension as extension, "
            + "     call.call_direction as direction, "
            + "     call.call_uuid as call_uuid, "
            + "     call.auto_dialed as auto_dialed "
            + " from tms.tms_agent_call_history call inner join tms.tms_revinfo info on call.revision = info.rev"
            + " where to_timestamp(info.revtstmp/1000) between :startTime and :endTime"
            + " and call.revision_type <> 2"
            + ")"
            + "select extension, "
            + " count(nullif(direction = 'INBOUND', false)) as inboundCallCount,"
            + " count(nullif(direction = 'OUTBOUND' and auto_dialed = true, false)) as dialerCallCount,"
            + " count(nullif(direction = 'OUTBOUND' and auto_dialed = false, false)) as manualCallCount"
            + "from calls "
            + "group by extension")
}
)

@Entity(name = "AgentCall")
@Table(schema = "sti")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
//@AuditTable(value = "tms_agent_call_history", schema = "tms")
public class AgentCallRecord extends AgentCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @ManyToOne
    @JoinColumn(name = "agent_extension")
    private AgentRecord agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stats_pk", referencedColumnName = "pk")
    private AgentStatsRecord agentStats;

    private LocalDateTime createDate;

    @PrePersist
    protected void onCreate() {
        createDate = new LocalDateTime();
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public AgentRecord getAgent() {
        return agent;
    }

    public void setAgent(AgentRecord agent) {
        this.agent = agent;
    }

    public AgentStatsRecord getAgentStats() {
        return agentStats;
    }

    public void setAgentStats(AgentStatsRecord agentStats) {
        this.agentStats = agentStats;
    }

}
