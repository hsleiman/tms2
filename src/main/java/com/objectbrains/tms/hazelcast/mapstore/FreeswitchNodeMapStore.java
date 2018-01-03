/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast.mapstore;

import com.hazelcast.core.MapStore;
import com.hazelcast.core.PostProcessingMapStore;
import com.objectbrains.tms.db.entity.freeswitch.FreeswitchNode;
import static com.objectbrains.tms.hazelcast.Configs.FREESWITCH_NODE_MAP_STORE_BEAN_NAME;
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
@Repository(FREESWITCH_NODE_MAP_STORE_BEAN_NAME)
public class FreeswitchNodeMapStore implements MapStore<String, FreeswitchNode>,PostProcessingMapStore {

    private static final Logger log = LoggerFactory.getLogger(FreeswitchNodeMapStore.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void store(String key, FreeswitchNode value) {
//        log.info("Storing Freeswitch node {} ", key);
//        value.setPk(key);
        if(value.getPk() == null || value.getPk() == 0){
            entityManager.persist(value);
        }else{
            entityManager.merge(value);
        }
    }

    @Override
    @Transactional
    public void storeAll(Map<String, FreeswitchNode> map) {
        for (Map.Entry<String, FreeswitchNode> entrySet : map.entrySet()) {
            String key = entrySet.getKey();
            FreeswitchNode value = entrySet.getValue();
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

    @Override
    @Transactional
    public FreeswitchNode load(String key) {
        try {
            return entityManager.createQuery(
                    "select tms from FreeswitchNode tms where "
                    + " tms.hostname = :key", FreeswitchNode.class)
                    .setParameter("key", key)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    @Transactional
    public Map<String, FreeswitchNode> loadAll(Collection<String> keys) {
        List<FreeswitchNode> tmss = entityManager.createQuery(
                "select tms from FreeswitchNode tms where tms.hostname in (:keys)", FreeswitchNode.class)
                .setParameter("keys", keys)
                .getResultList();

        Map<String, FreeswitchNode> resultMap = new HashMap<>();
        for (FreeswitchNode tms : tmss) {
            resultMap.put(tms.getHostname(), tms);
        }
        return resultMap;
    }

    @Override
    @Transactional
    public Set<String> loadAllKeys() {
        List<String> key = entityManager.createQuery(
                "select tms.hostname from FreeswitchNode tms", String.class)
                .getResultList();
        return new HashSet<>(key);
    }

}
