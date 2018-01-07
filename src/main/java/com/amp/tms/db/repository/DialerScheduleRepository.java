/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.repository;

import com.amp.tms.db.entity.DialerQueueTms;
import com.amp.tms.db.entity.DialerScheduleEntity;
import com.amp.tms.pojo.DialerSchedule;
import com.amp.tms.service.dialer.DialerException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author HS
 */
@Repository
@Transactional
public class DialerScheduleRepository {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private TmsDialerQueueRepository dialerRepository;

//    @Autowired
//    private Scheduler scheduler;
    
    
    public List<DialerScheduleEntity> getDialerSchedule(long queuePk){
        return entityManager.createQuery("select schedule "
                + "from DialerSchedule schedule "
                + "where schedule.dialerQueue.pk = :queuePk", DialerScheduleEntity.class)
                .setParameter("queuePk", queuePk)
                .getResultList();
    }

    public List<DialerScheduleEntity> setDialerSchedule(long queuePk, List<DialerSchedule> schedules) throws DialerException {

//        List<DialerScheduleEntity> oldSchedules
        

        entityManager.createQuery("delete DialerSchedule schedule "
                + "where schedule.dialerQueue.pk = :queuePk")
                .setParameter("queuePk", queuePk)
                .executeUpdate();
        
        
        DialerQueueTms queue = dialerRepository.getDialerQueue(queuePk);
        
        List<DialerScheduleEntity> newSchedules = new ArrayList<>();
        for (DialerSchedule schedule : schedules) {
            DialerScheduleEntity entity = new DialerScheduleEntity();
            entity.copyFrom(schedule);
            entity.setDialerQueue(queue);
            entityManager.persist(entity);
            newSchedules.add(entity);
            
//            String name = entity.getPk().toString();
//            TriggerKey key = new TriggerKey(name, scheduleTriggerGroupName(queuePk));
//            keys.add(key);
        }
        
        return newSchedules;
        
    }
//
//    private List<TriggerKey> toTriggerKeys(long queuePk, List<DialerScheduleEntity> schedules) {
//        List<TriggerKey> keys = new ArrayList<>();
//        for (DialerScheduleEntity schedule : schedules) {
////            StringBuilder id = new StringBuilder();
////            id.append("schedule-dialer-").append(queuePk).append("-").append(schedule.getPk());
////            schedule.
//            String name = schedule.getPk().toString();
//            TriggerKey key = new TriggerKey(name, scheduleTriggerGroupName(queuePk));
//            keys.add(key);
//        }
//        return keys;
//    }
//
//    private String scheduleTriggerGroupName(long queuePk) {
//        return "schedule-dialer-" + queuePk;
//    }
}
