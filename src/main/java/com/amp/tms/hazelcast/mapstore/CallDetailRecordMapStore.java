/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapstore;

import com.hazelcast.core.MapStore;
import com.hazelcast.core.PostProcessingMapStore;
import com.amp.tms.db.entity.cdr.CallDetailRecordTMS;
import static com.amp.tms.hazelcast.Configs.CALL_DETAIL_RECORD_MAP_STORE_BEAN_NAME;
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
public class CallDetailRecordMapStore implements MapStore<String, CallDetailRecordTMS>, PostProcessingMapStore {

    private static final Logger log = LoggerFactory.getLogger(CallDetailRecordMapStore.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void store(String key, CallDetailRecordTMS value) {

        log.info("Saving CallDetailRecordMapStore {} -> pk is {} isAgentHangup: {} F:{}, {}", value.getCall_uuid(), value.getPk(), value.getComplete(), value.getCompleteFinal(), value.getSystemDispostionCode());

        if (value.getPk() == null) {
            entityManager.persist(value);
        } else {
            entityManager.merge(value);
        }
    }

    @Override
    @Transactional
    public void storeAll(Map<String, CallDetailRecordTMS> map) {
        for (Map.Entry<String, CallDetailRecordTMS> entrySet : map.entrySet()) {
            String key = entrySet.getKey();
            CallDetailRecordTMS value = entrySet.getValue();
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

    private CallDetailRecordTMS create(String uuid) {
        CallDetailRecordTMS cdr = new CallDetailRecordTMS(uuid);
        entityManager.persist(cdr);
        return cdr;
    }

    @Override
    @Transactional
    public CallDetailRecordTMS load(String key) {
        try {
            return entityManager.createQuery("select tms from CallDetailRecord tms where "
                    + " tms.call_uuid = :key", CallDetailRecordTMS.class)
                    .setParameter("key", key)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return create(key);
        }
    }

    @Override
    @Transactional
    public Map<String, CallDetailRecordTMS> loadAll(Collection<String> keys) {
        List<CallDetailRecordTMS> tmss = entityManager.createQuery("select tms from CallDetailRecord tms where tms.call_uuid in (:keys)", CallDetailRecordTMS.class)
                .setParameter("keys", keys)
                .getResultList();
        
        Set<String> missingKeys = new HashSet<>(keys);

        Map<String, CallDetailRecordTMS> resultMap = new HashMap<>();
        for (CallDetailRecordTMS tms : tmss) {
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
