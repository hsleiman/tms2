/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.qaform;

import com.amp.crm.db.entity.base.dialer.CallDetailRecord;
import com.amp.crm.db.entity.base.dialer.CallQualityManagementEvaluation;
import com.amp.crm.db.repository.dialer.CrmCallDetailRecordRepository;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CallQualityManagementRepository {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private CrmCallDetailRecordRepository cdrRepo;

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
