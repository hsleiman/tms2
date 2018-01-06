/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.utility;

import com.amp.crm.db.entity.utility.RestfullCallLog;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author hsleiman
 */
@Repository
@Transactional
public class RestfullCallRepository {

    @PersistenceContext
    private EntityManager entityManager;
    
    private static final Logger log = LoggerFactory.getLogger(RestfullCallRepository.class);
    
    @Async
    public void createLog(RestfullCallLog restfullCallLog) {
        restfullCallLog.setCreateTimestamp(LocalDateTime.now());
        entityManager.persist(restfullCallLog);
    }
    
}
