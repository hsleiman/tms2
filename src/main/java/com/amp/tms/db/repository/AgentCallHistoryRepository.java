/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.repository;

import com.amp.tms.pojo.report.AgentCallHistorySummary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * 
 */
@Repository
@Transactional
public class AgentCallHistoryRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public AgentCallHistorySummary getCallHistorySummary(long queuePk, int extension, LocalDateTime from, LocalDateTime to) {
        Object[] result = (Object[])entityManager.createNativeQuery("with calls as ("
                + " select distinct"
                + "     call.call_direction as direction, "
                + "     call.call_uuid as call_uuid, "
                + "     call.auto_dialed as auto_dialed"
                + " from tms.tms_agent_call_history call inner join tms.tms_revinfo info on call.revision = info.rev"
                + " where to_timestamp(info.revtstmp / 1000) between :startTime and :endTime"
                + " and call.revision_type <> 2"
                + " and call.agent_extension = :extension"
                + " and call.queue_pk = :queuePk"
                + ")"
                + "select "
                + " count(nullif(direction = 'INBOUND', false)) as inboundCallCount, "
                + " count(nullif(direction = 'OUTBOUND' and auto_dialed = true, false)) as dialerCallCount, "
                + " count(nullif(direction = 'OUTBOUND' and auto_dialed = false, false)) as manualCallCount "
                + "from calls")
                .setParameter("extension", extension)
                .setParameter("queuePk", queuePk)
                .setParameter("startTime", from.toDate())
                .setParameter("endTime", to.toDate())
                .getSingleResult();
        AgentCallHistorySummary summary = new AgentCallHistorySummary();
        summary.setExtension(extension);
        summary.setInboundCallCount(((Number) result[0]).intValue());
        summary.setDialerCallCount(((Number) result[1]).intValue());
        summary.setManualCallCount(((Number) result[2]).intValue());
//        summary.setContactCount(((Number) result[3]).intValue());
//        summary.setPtpCount(((Number) result[4]).intValue());
        return summary;
    }
    
    public Map<Integer, AgentCallHistorySummary> getCallHistorySummaries(Set<Integer> extensions, LocalDateTime from, LocalDateTime to) {
        List<Object[]> results = entityManager.createNamedQuery("AgentCallRecord.AgentCallsReport", Object[].class)
                .setParameter("extensions", extensions)
                .setParameter("startTime", from)
                .setParameter("endTime", to)
                .getResultList();
        Map<Integer, AgentCallHistorySummary> summaryMap = new HashMap<>();
        for (Object[] result : results) {
            AgentCallHistorySummary summary = new AgentCallHistorySummary();
            summary.setExtension(((Number) result[0]).intValue());
            summary.setInboundCallCount(((Number) result[1]).intValue());
            summary.setDialerCallCount(((Number) result[2]).intValue());
            summary.setManualCallCount(((Number) result[3]).intValue());
            summary.setContactCount(((Number) result[4]).intValue());
            summary.setPtpCount(((Number) result[5]).intValue());
            summaryMap.put(summary.getExtension(), summary);
        }
        return summaryMap;
    }
}
