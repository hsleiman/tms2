/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.utility;

import com.amp.crm.db.entity.utility.DynamicCode;
import com.amp.crm.exception.ObjectNotFoundException;
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
public class DynamicCodeRepository {
    
    @PersistenceContext
    private EntityManager em;
    
    public DynamicCode locateDynamicCodeByPk(long dynamicCodePk) {
        DynamicCode sdc = em.find(DynamicCode.class, dynamicCodePk);
        if (sdc == null) {
            throw new ObjectNotFoundException(dynamicCodePk, DynamicCode.class);
        }
        return sdc;
    }
    
    public DynamicCode locateDynamicCodeByName(String name) {
        TypedQuery<DynamicCode> q = em.createNamedQuery("DynamicCode.LocateByCodeName", DynamicCode.class);
        q.setParameter("name", name);
        List<DynamicCode> list = q.getResultList();
        if (list.isEmpty()) {
            throw new ObjectNotFoundException("DynamicCode with name [" + name + "] could not be found.");
        } 
        return list.get(0);
    }
    
   public Number getCount(){
        TypedQuery<Number> q = em.createQuery("SELECT count(*) FROM DynamicCode s", Number.class);
        return q.getSingleResult();

   }
    
    public DynamicCode mergeDynamicCode(DynamicCode sdc) {
        return em.merge(sdc);
    }
            
    public void deleteDynamicCode(DynamicCode sdc) {
        em.remove(sdc);
    }
    
    public DynamicCode locateDynamicCodeByInterface(String interfaceStr) {
        TypedQuery<DynamicCode> q = em.createNamedQuery("DynamicCode.LocateByInterface", DynamicCode.class);
        q.setParameter("interface", interfaceStr);
        List<DynamicCode> list = q.getResultList();
        if (list.isEmpty()) {
            throw new ObjectNotFoundException("DynamicCode with interface [" + interfaceStr + "] could not be found.");
        } 
        return list.get(0);
    }
}
