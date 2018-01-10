/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapstore;

import com.hazelcast.core.MapStore;
import com.hazelcast.core.PostProcessingMapStore;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import static com.amp.tms.hazelcast.Configs.TMS_DIALPLAN_MAP_STORE_BEAN_NAME;
import com.amp.tms.hazelcast.keys.TMSDialplanKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * 
 */
@Repository(TMS_DIALPLAN_MAP_STORE_BEAN_NAME)
public class TMSDialplanMapStore implements MapStore<TMSDialplanKey, TMSDialplan>, PostProcessingMapStore {

    private static final Logger log = LoggerFactory.getLogger(TMSDialplanMapStore.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void store(TMSDialplanKey key, TMSDialplan value) {
//        if (value.isDebugOn()) {
//            log.info("--------------------");
//            log.info("Storing TMSDialplan: " + value.getKey().getTms_uuid() + " - " + value.getKey().getContext());
//            //log.info(tmsDialplan.toJson());
//            log.info("--------------------");
//        }
//        if (value.getCreateDateTime() == null) {
//            entityManager.persist(value);
//            if (value.isDebugOn()) {
//                log.info("--------------------");
//                log.info("Persist TMSDialplan: " + value.getKey().getTms_uuid() + " - " + value.getKey().getContext());
//                //log.info(tmsDialplan.toJson());
//                log.info("--------------------");
//            }
//        } else {
            entityManager.merge(value);
//            if (value.isDebugOn()) {
//                log.info("--------------------");
//                log.info("Merge TMSDialplan: " + value.getKey().getTms_uuid() + " - " + value.getKey().getContext());
//                //log.info(tmsDialplan.toJson());
//                log.info("--------------------");
//            }
//        }

//        if (value.isDebugOn()) {
//            log.info("--------------------");
//            log.info("Stored TMSDialplan: " + value.getKey().getTms_uuid() + " - " + value.getKey().getContext());
//            //log.info(tmsDialplan.toJson());
//            log.info("--------------------");
//        }
    }

    @Override
    @Transactional
    public void storeAll(Map<TMSDialplanKey, TMSDialplan> map) {
        for (Map.Entry<TMSDialplanKey, TMSDialplan> entrySet : map.entrySet()) {
            TMSDialplanKey key = entrySet.getKey();
            TMSDialplan value = entrySet.getValue();
            store(key, value);
        }
    }

    @Override
    public void delete(TMSDialplanKey key) {
        //TODO
    }

    @Override
    public void deleteAll(Collection<TMSDialplanKey> keys) {
        //TODO
    }

    @Override
    @Transactional
    public TMSDialplan load(TMSDialplanKey key) {
        try {
            return entityManager.createQuery(
                    "select tms from TMSDialplan tms where "
                    + " tms.key = :key", TMSDialplan.class)
                    .setParameter("key", key)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    @Transactional
    public Map<TMSDialplanKey, TMSDialplan> loadAll(Collection<TMSDialplanKey> keys) {
        List<TMSDialplan> tmss = entityManager.createQuery(
                "select tms from TMSDialplan tms where tms.key in (:keys)", TMSDialplan.class)
                .setParameter("keys", keys)
                .getResultList();

        Map<TMSDialplanKey, TMSDialplan> resultMap = new HashMap<>();
        for (TMSDialplan tms : tmss) {
            resultMap.put(tms.getKey(), tms);
        }
        return resultMap;
    }

    @Override
    public Set<TMSDialplanKey> loadAllKeys() {
//        List<TMSDialplanKey> extensions = entityManager.createQuery(
//                "select tms.key from TMSDialplan tms", TMSDialplanKey.class)
//                .getResultList();
//        return new HashSet<>(extensions);
        return null;
    }

}
