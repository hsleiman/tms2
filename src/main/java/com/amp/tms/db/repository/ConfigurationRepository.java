/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.repository;

import com.amp.tms.db.entity.freeswitch.StaticConfiguration;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author hsleiman
 */
@Repository
@Transactional
public class ConfigurationRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public StaticConfiguration getStaticConfiguration(String key){
       return entityManager.createQuery(
                    "select sd from StaticConfiguration sd where "
                    + "sd.keyValue = :keyValue" , StaticConfiguration.class)
                    .setParameter("keyValue", key)
                    .getSingleResult();
    }
}
