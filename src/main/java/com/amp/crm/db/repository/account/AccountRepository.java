/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.account;

import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.base.customer.Customer;
import com.amp.crm.db.repository.customer.CustomerRepository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;


@Repository
public class AccountRepository {
    private static final Logger LOG = LoggerFactory.getLogger(AccountRepository.class);
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public void persistAccount(Account c) {
        entityManager.persist(c);
    }
    
    public void mergeAccount(Account c) {
        entityManager.merge(c);
    }

    public Account findAccountByPk(long pk) {
        return entityManager.find(Account.class, pk);
    }
}
