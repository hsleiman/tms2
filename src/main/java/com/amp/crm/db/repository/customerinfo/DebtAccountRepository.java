/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.customerinfo;

import com.amp.crm.db.entity.base.account.DebtAccount;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class DebtAccountRepository {
    
    private static final Logger LOG = LoggerFactory.getLogger(DebtAccountRepository.class);

    @PersistenceContext
    private EntityManager entityManager;
    
    public void persistDebtAccount(DebtAccount c) {
        entityManager.persist(c);
    }
    
    public void mergeDebtAccount(DebtAccount c) {
        entityManager.merge(c);
    }

    public DebtAccount findDebtAccountByPk(long pk) {
        return entityManager.find(DebtAccount.class, pk);
    }
}
