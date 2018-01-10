/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.repository;

import com.amp.tms.db.entity.AgentRecord;
import com.amp.tms.db.entity.AgentCallDetailRecordAssociation;
import com.amp.tms.db.entity.cdr.CallDetailRecordTMS;
import com.amp.tms.db.entity.cdr.PTP;
import com.amp.tms.db.entity.cdr.PaymentTms;
import com.amp.tms.db.entity.cdr.SpeechToTextTms;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.websocket.message.inbound.Recieve;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.Session;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * 
 */
@Repository
@Transactional
public class CallDetailRecordRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public void addAgentToCDR(CallDetailRecordTMS cdr, AgentRecord agent) {
        AgentCallDetailRecordAssociation acdra = new AgentCallDetailRecordAssociation();
        acdra.setAgent(agent);
        acdra.setCallDetailRecord(cdr);
        acdra.setStartTimestamp(LocalDateTime.now());
        entityManager.persist(acdra);
    }

    public void endAgentCallFromCDR(CallDetailRecordTMS cdr, AgentTMS agent) {
        AgentCallDetailRecordAssociation acdra = entityManager.createQuery(
                "select tms from AgentCallDetailRecordAssociation tms where "
                + " AgentCallDetailRecordAssociation.Agent = :Agent and AgentCallDetailRecordAssociation.CallDetailRecord = :CallDetailRecord", AgentCallDetailRecordAssociation.class)
                .setParameter("Agent", agent)
                .setParameter("CallDetailRecord", cdr)
                .getSingleResult();
        if (acdra != null) {
            acdra.setEndTimestamp(LocalDateTime.now());
            entityManager.persist(acdra);
        }
    }

    public void persist(SpeechToTextTms toText) {
        entityManager.persist(toText);
    }

    public void addPTP(Integer ext, Recieve recieve) {
        PTP ptp = new PTP();
        ptp.setAmount(recieve.getPtp().getAmount());
        ptp.setDate(recieve.getPtp().getDate());
        ptp.setCall_uuid(recieve.getCall_uuid());
        entityManager.persist(ptp);
    }

    public void addPayment(Integer ext, Recieve recieve) {
        PaymentTms payment = new PaymentTms();
        payment.setAmount(recieve.getPayment().getAmount());
        payment.setDate(recieve.getPayment().getDate());
        payment.setCall_uuid(recieve.getCall_uuid());
        entityManager.persist(payment);
    }
}
