/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.repository.qaform;

import com.objectbrains.sti.db.entity.base.dialer.CallDetailRecord;
import com.objectbrains.sti.db.entity.base.dialer.CallQualityManagementEvaluation;
import com.objectbrains.sti.db.repository.dialer.CallDetailRecordRepository;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author David
 */
@Repository
public class CallQualityManagementRepository {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private CallDetailRecordRepository cdrRepo;

    public CallQualityManagementEvaluation getCallQualityManagementEvaluation(long qmPk) {
        return em.find(CallQualityManagementEvaluation.class, qmPk);
    }

    public CallQualityManagementEvaluation createCallQualityManagementEvaluation(String callUUID) {
        CallQualityManagementEvaluation callEvaluation = new CallQualityManagementEvaluation();
        CallDetailRecord cdr = cdrRepo.locateCallDetailRecordByCallUUID(callUUID);
        callEvaluation.setCallUUID(callUUID);
        callEvaluation.setFromPhoneNumber(cdr.getCallerIdNumber());
        callEvaluation.setToPhoneNumber(cdr.getCalleeIdNumber());
        callEvaluation.setCallDirection(cdr.getCallDirection());
        if (cdr.getAccountPk() != null) {
            callEvaluation.setAccountPk(cdr.getAccountPk());
        }
        return callEvaluation;
    }

    public List<CallQualityManagementEvaluation> getAllCallQualityManagementEvaluation(String callUUID) {
        TypedQuery<CallQualityManagementEvaluation> q = em
                .createNamedQuery("CallQualityManagementEvaluation.LocateByCallUUID", CallQualityManagementEvaluation.class)
                .setParameter("callUUID", callUUID);
        return q.getResultList();
    }

}
