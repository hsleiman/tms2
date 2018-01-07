/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapstore;

import com.hazelcast.core.MapStore;
import com.hazelcast.core.PostProcessingMapStore;
import com.amp.tms.db.entity.DialerStatsEntity;
import static com.amp.tms.hazelcast.Configs.DIALER_STATS_MAP_STORE_BEAN_NAME;
import com.amp.tms.hazelcast.entity.DialerStats;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Repository(DIALER_STATS_MAP_STORE_BEAN_NAME)
public class DialerStatsMapStore implements MapStore<Long, DialerStats>, PostProcessingMapStore {

    @PersistenceContext
    private EntityManager entityManager;

//    @Autowired
//    private DialerQueueRepository queueRepository;
//
//    private void create(Long queuePk, DialerStats value) {
//        LocalDateTime now = LocalDateTime.now();
//        DialerStatsEntity stats = new DialerStatsEntity();
//        value.setPk(null);
//        stats.copyFrom(value);
//
//        DialerQueue queue = queueRepository.getDialerQueue(queuePk);
//        DialerStatsEntity currentStats = queue.getCurrentQueueStats();
//        if (currentStats != null && !currentStats.hasEnded()) {
//            currentStats.stop();
//        }
//        stats.setDialerQueue(queue);
//        queue.setCurrentQueueStats(stats);
//        entityManager.persist(stats);
//        entityManager.flush();
//        value.setPk(stats.getPk());
//    }
    @Override
    @Transactional
    public void store(Long dialerPk, DialerStats value) {
        Long pk = value.getPk();
        DialerStatsEntity stats = null;
        if (pk != null) {
            stats = entityManager.find(DialerStatsEntity.class, pk);
        }
        if (stats != null) {
            stats.copyFrom(value);
        } else {
//            create(queuePk, value);
            //TODO throw something?
        }
    }

    @Override
    @Transactional
    public void storeAll(Map<Long, DialerStats> map) {
        for (Map.Entry<Long, DialerStats> entrySet : map.entrySet()) {
            Long key = entrySet.getKey();
            DialerStats value = entrySet.getValue();
            store(key, value);
        }
    }

    @Override
    public void delete(Long dialerPk) {
    }

    @Override
    public void deleteAll(Collection<Long> keys) {
    }

    @Override
    @Transactional
    public DialerStats load(Long dialerPk) {
        DialerStatsEntity stats = entityManager.find(DialerStatsEntity.class, dialerPk);
        if (stats != null) {
            return new DialerStats(stats);
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public Map<Long, DialerStats> loadAll(Collection<Long> keys) {
        List<DialerStatsEntity> results;
        if (keys != null && !keys.isEmpty()) {
            results = entityManager.createQuery(
                    "select stats "
                    + "from DialerStats stats "
                    + "where stats.pk in (:statsPks)",
                    DialerStatsEntity.class)
                    .setParameter("statsPks", keys)
                    .getResultList();
        } else {
            results = Collections.emptyList();
        }
        Map<Long, DialerStats> retMap = new HashMap<>();
        for (DialerStatsEntity result : results) {
            retMap.put(result.getQueuePk(), new DialerStats(result));
        }
        return retMap;
    }

    @Override
    public Iterable<Long> loadAllKeys() {
        return null;
    }

}
