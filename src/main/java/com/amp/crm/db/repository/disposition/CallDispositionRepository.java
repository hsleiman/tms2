/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.disposition;

import com.amp.crm.db.entity.disposition.CallDispositionCode;
import com.amp.crm.db.entity.disposition.CallDispositionGroup;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

/**
 *
 * 
 */
@Repository
public class CallDispositionRepository {

    @PersistenceContext
    private EntityManager em;

    public CallDispositionCode locateDispositionById(long id) {
        return em.find(CallDispositionCode.class, id);
    }
    
    public CallDispositionCode locateDisposition(String disposition) {
        TypedQuery<CallDispositionCode> q = em.createNamedQuery("CallDispositionCode.LocateByDisposition", CallDispositionCode.class);
        q.setParameter("disposition", disposition);
        List<CallDispositionCode> list = q.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
    
    public CallDispositionCode locateDispositionByQCode(Integer qCode) {
        TypedQuery<CallDispositionCode> q = em.createNamedQuery("CallDispositionCode.LocateByQCode", CallDispositionCode.class);
        q.setParameter("qCode", qCode);
        List<CallDispositionCode> list = q.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public CallDispositionGroup locateDispositionGroupByPk(long pk) {
        return em.find(CallDispositionGroup.class, pk);
    }

    public List<CallDispositionGroup> locateCallDispositionGroups() {
        return em.createNamedQuery("CallDispositionGroup.GetAllCallDispositionGroups", CallDispositionGroup.class).getResultList();
    }

    public CallDispositionGroup locateDispositionGroupByName(String name) {
        List<CallDispositionGroup> list = em.createNamedQuery("CallDispositionGroup.GetCallDispositionGroupByName", CallDispositionGroup.class)
                .setParameter("name", name).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<CallDispositionCode> locateAllCallDispositionCodes() {
        return em.createNamedQuery("CallDispositionCode.LocateAll", CallDispositionCode.class).getResultList();
    }

    public List<CallDispositionGroup> locateAllDispositionGroupsForDispositionCode(long dispositionId) {
        List<CallDispositionGroup> groups = em.createNamedQuery("CallDispositionGroup.GetAllGroupsForDispositionCode", CallDispositionGroup.class)
                .setParameter("dispositionId", dispositionId)
                .getResultList();
        return groups;
    }
}
