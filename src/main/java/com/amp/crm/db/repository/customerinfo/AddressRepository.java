/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.customerinfo;

import com.amp.crm.db.entity.base.customer.Address;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Bishistha
 */
@Repository
public class AddressRepository {
    
    private static final Logger LOG = LoggerFactory.getLogger(AddressRepository.class);

    @PersistenceContext
    private EntityManager entityManager;
    
    public void persistAddress(Address c) {
        entityManager.persist(c);
    }
    
    public void mergeAddress(Address c) {
        entityManager.merge(c);
    }

    public Address findAddressByPk(long pk) {
        return entityManager.find(Address.class, pk);
    }
}
