/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.repository.customerinfo;

import com.objectbrains.sti.db.entity.base.customer.Email;
import com.objectbrains.sti.db.entity.base.customer.Phone;
import com.objectbrains.sti.embeddable.EmailData;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Bishistha
 */
@Repository
public class EmailRepository {
    
    private static final Logger LOG = LoggerFactory.getLogger(EmailRepository.class);

    @PersistenceContext
    private EntityManager entityManager;
    
    public void persistEmail(Email c) {
        entityManager.persist(c);
    }

    public Email findEmailByPk(long pk) {
        return entityManager.find(Email.class, pk);
    }
    
    public List<EmailData> getAllEmailsByCustomerPk(long customerPk) {
        TypedQuery<EmailData> query = entityManager.createNamedQuery("Email.LocateAllByCustomerPk", EmailData.class);
        query.setParameter("customerPk", customerPk);
        try {
            return query.getResultList();
        } catch (NoResultException ex) {
            return null;
        }
    }
}
