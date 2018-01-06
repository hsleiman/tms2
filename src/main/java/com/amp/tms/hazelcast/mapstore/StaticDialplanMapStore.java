/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapstore;

import com.hazelcast.core.MapStore;
import com.hazelcast.core.PostProcessingMapStore;
import com.amp.tms.db.entity.freeswitch.StaticDialplan;
import static com.amp.tms.hazelcast.Configs.STATIC_DIALPLAN_MAP_STORE_BEAN_NAME;
import com.amp.tms.hazelcast.keys.StaticDialplanKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author connorpetty
 */
@Repository(STATIC_DIALPLAN_MAP_STORE_BEAN_NAME)
public class StaticDialplanMapStore implements MapStore<StaticDialplanKey, StaticDialplan>, PostProcessingMapStore {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void store(StaticDialplanKey key, StaticDialplan value) {
        if (value.getPk() == null) {
            entityManager.persist(value);
        } else {
            entityManager.merge(value);
        }
    }

    @Override
    @Transactional
    public void storeAll(Map<StaticDialplanKey, StaticDialplan> map) {
        for (Map.Entry<StaticDialplanKey, StaticDialplan> entrySet : map.entrySet()) {
            StaticDialplanKey key = entrySet.getKey();
            StaticDialplan value = entrySet.getValue();
            store(key, value);
        }
    }

    @Override
    public void delete(StaticDialplanKey key) {
        //TODO
    }

    @Override
    public void deleteAll(Collection<StaticDialplanKey> keys) {
        //TODO
    }

    @Override
    @Transactional
    public StaticDialplan load(StaticDialplanKey key) {
        try {
            return entityManager.createQuery(
                    "select sd from StaticDialplan sd where "
                    + "sd.staticDialplanKey = :staticDialplanKey", StaticDialplan.class)
                    .setParameter("staticDialplanKey", key)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    @Transactional
    public Map<StaticDialplanKey, StaticDialplan> loadAll(Collection<StaticDialplanKey> keys) {
        List<StaticDialplan> staticDialplans = entityManager.createQuery(
                "select sd from StaticDialplan sd where sd.staticDialplanKey in (:staticDialplanKeys)", StaticDialplan.class)
                .setParameter("staticDialplanKeys", keys)
                .getResultList();

        Map<StaticDialplanKey, StaticDialplan> resultMap = new HashMap<>();
        for (StaticDialplan dialplan : staticDialplans) {
            resultMap.put(dialplan.getStaticDialplanKey(), dialplan);
        }
        return resultMap;
    }

    @Override
    public Set<StaticDialplanKey> loadAllKeys() {
//        List<StaticDialplanKey> extensions = entityManager.createQuery(
//                "select sd.staticDialplanKey from StaticDialplan sd", StaticDialplanKey.class)
//                .getResultList();
//        return new HashSet<>(extensions);
        return null;
    }

}
