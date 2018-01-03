/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast.mapstore;

import com.hazelcast.core.MapStore;
import com.hazelcast.core.PostProcessingMapStore;
import com.objectbrains.tms.db.entity.cdr.CallDetailRecord;
import static com.objectbrains.tms.hazelcast.Configs.CALL_DETAIL_RECORD_MAP_STORE_BEAN_NAME;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
 * @author connorpetty
 */
@Repository(CALL_DETAIL_RECORD_MAP_STORE_BEAN_NAME)
public class CallDetailRecordMapStore implements MapStore<String, CallDetailRecord>, PostProcessingMapStore {

    private static final Logger log = LoggerFactory.getLogger(CallDetailRecordMapStore.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void store(String key, CallDetailRecord value) {

        log.info("Saving CallDetailRecordMapStore {} -> pk is {} isAgentHangup: {} F:{}, {}", value.getCall_uuid(), value.getPk(), value.getComplete(), value.getCompleteFinal(), value.getSystemDispostionCode());

        if (value.getPk() == null) {
            entityManager.persist(value);
        } else {
            entityManager.merge(value);
        }
    }

    @Override
    @Transactional
    public void storeAll(Map<String, CallDetailRecord> map) {
        for (Map.Entry<String, CallDetailRecord> entrySet : map.entrySet()) {
            String key = entrySet.getKey();
            CallDetailRecord value = entrySet.getValue();
            store(key, value);
        }
    }

    @Override
    public void delete(String key) {
        //TODO
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        //TODO
    }

    private CallDetailRecord create(String uuid) {
        CallDetailRecord cdr = new CallDetailRecord(uuid);
        entityManager.persist(cdr);
        return cdr;
    }

    @Override
    @Transactional
    public CallDetailRecord load(String key) {
        try {
            return entityManager.createQuery(
                    "select tms from CallDetailRecord tms where "
                    + " tms.call_uuid = :key", CallDetailRecord.class)
                    .setParameter("key", key)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return create(key);
        }
    }

    @Override
    @Transactional
    public Map<String, CallDetailRecord> loadAll(Collection<String> keys) {
        List<CallDetailRecord> tmss = entityManager.createQuery(
                "select tms from CallDetailRecord tms where tms.call_uuid in (:keys)", CallDetailRecord.class)
                .setParameter("keys", keys)
                .getResultList();
        
        Set<String> missingKeys = new HashSet<>(keys);

        Map<String, CallDetailRecord> resultMap = new HashMap<>();
        for (CallDetailRecord tms : tmss) {
            resultMap.put(tms.getCall_uuid(), tms);
            missingKeys.remove(tms.getCall_uuid());
        }
        for (String missingKey : missingKeys) {
            resultMap.put(missingKey, create(missingKey));
        }
        return resultMap;
    }

    @Override
    public Set<String> loadAllKeys() {
//        List<TMSDialplanKey> extensions = entityManager.createQuery(
//                "select tms.key from TMSFunctionParam tms", TMSDialplanKey.class)
//                .getResultList();
//        return new HashSet<>(extensions);
        return null;
    }

}
