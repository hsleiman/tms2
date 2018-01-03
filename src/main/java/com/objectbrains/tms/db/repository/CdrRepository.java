/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.repository;

import com.objectbrains.tms.db.entity.freeswitch.CDR;
import com.objectbrains.tms.pojo.CallHistory;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author connorpetty
 */
@Repository
@Transactional
public class CdrRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void persistCDR(CDR cdr) {
        entityManager.persist(cdr);
    }

    public List<CallHistory> getAgentCallHistory(int ext) {
        String sql = "select new " + CallHistory.class.getName()
                + "(cdr.pk, cdr.createTimestamp, cdr.caller_id_number, cdr.destination_number, "
                + " concat(case when cdr.answermsec = 0 then 'Missed ' else '' end, "
                + " case when cdr.caller_id_number = :ext then 'Outbound' else 'Inbound' end)"
                + ") "
                + "from CDR cdr "
                + "where cdr.context = 'agent_dp' "
                + "and cdr.destination_number <> 'voicemail' "
                + "and (cdr.caller_id_number = :ext or cdr.destination_number = :ext) "
                + "order by cdr.createTimestamp desc";

        return entityManager.createQuery(sql, CallHistory.class)
                .setParameter("ext", Integer.toString(ext))
                .getResultList();
    }

    public List<CallHistory> getAgentCallHistory(int ext, int pageIndex, int pageSize) {
        String sql = "select new " + CallHistory.class.getName()
                + "(cdr.pk, cdr.createTimestamp, cdr.caller_id_number, cdr.destination_number, "
                + " concat(case when cdr.answermsec = 0 then 'Missed ' else '' end, "
                + " case when cdr.caller_id_number = :ext then 'Outbound' else 'Inbound' end),"
                + " concat(cdr.borrowerFirstName, ' ', cdr.borrowerLastName)"
                + ") "
                + "from CDR cdr "
                + "where cdr.context = 'agent_dp' "
                + "and cdr.destination_number <> 'voicemail' "
                + "and (cdr.caller_id_number = :ext or cdr.destination_number = :ext) "
                + "order by cdr.createTimestamp desc";

        return entityManager.createQuery(sql, CallHistory.class)
                .setParameter("ext", Integer.toString(ext))
                .setFirstResult(pageSize * pageIndex)
                .setMaxResults(pageSize)
                .getResultList();
    }
}
