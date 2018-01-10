/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.customerinfo;

import com.amp.crm.db.entity.base.customer.WebUser;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class WebUserRepository {
    
    private static final Logger LOG = LoggerFactory.getLogger(WebUserRepository.class);

    @PersistenceContext
    private EntityManager entityManager;
    
    public void persistWebUser(WebUser c) {
        entityManager.persist(c);
    }
    
    public void mergeWebUser(WebUser c) {
        entityManager.merge(c);
    }

    public WebUser findWebUserByPk(long pk) {
        return entityManager.find(WebUser.class, pk);
    }
}
