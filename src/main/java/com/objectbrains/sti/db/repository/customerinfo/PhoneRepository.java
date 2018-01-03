/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.repository.customerinfo;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.sti.db.entity.base.customer.Customer;
import com.objectbrains.sti.db.entity.base.customer.Phone;
import com.objectbrains.sti.db.repository.customer.CustomerRepository;
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
public class PhoneRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PhoneRepository.class);

    @PersistenceContext
    private EntityManager entityManager;

    public void persistPhone(Phone p) {
        entityManager.persist(p);
    }

    public void mergePhone(Phone p) {
        entityManager.merge(p);
    }

    public Phone findPhoneByPk(long pk) {
        return entityManager.find(Phone.class, pk);
    }

    public List<Phone> getAllPhoneByCustomerPk(long customerPk) {
        TypedQuery<Phone> query = entityManager.createNamedQuery("Phone.getAllPhoneDataByCustomerPk", Phone.class);
        query.setParameter("customerPk", customerPk);
        try {
            return query.getResultList();
        } catch (NoResultException ex) {
            return null;
        }
    }
}
