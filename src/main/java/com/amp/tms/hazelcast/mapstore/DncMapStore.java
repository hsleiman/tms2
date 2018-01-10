/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapstore;

import com.hazelcast.core.MapStore;
import com.hazelcast.core.PostProcessingMapStore;
import com.objectbrains.scheduler.annotation.QuartzJob;
import com.amp.tms.db.entity.DNC;
import com.amp.tms.enumerated.DncStatus;
import static com.amp.tms.hazelcast.Configs.DNC_MAP_STORE_BEAN_NAME;
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
 * 
 */
@Repository(DNC_MAP_STORE_BEAN_NAME)
public class DncMapStore implements MapStore<String, DNC>, PostProcessingMapStore {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void store(String key, DNC value) {
        if (value.getPk() == null) {
            entityManager.persist(value);
        } else {
            entityManager.merge(value);
        }
    }

    @Override
    @Transactional
    public void storeAll(Map<String, DNC> map) {
        for (Map.Entry<String, DNC> entrySet : map.entrySet()) {
            String key = entrySet.getKey();
            DNC value = entrySet.getValue();
            store(key, value);
        }
    }

    @Override
    @Transactional
    public void delete(String phoneNumber) {
        entityManager.createQuery(
                "update DNC dnc "
                + "set dnc.status = :expiredStatus "
                + "where dnc.phoneNumber = :phoneNumber")
                .setParameter("phoneNumber", phoneNumber)
                .setParameter("expiredStatus", DncStatus.EXPIRED)
                .executeUpdate();
    }

    @Override
    @Transactional
    public void deleteAll(Collection<String> keys) {
        entityManager.createQuery(
                "update DNC dnc "
                + "set dnc.status = :expiredStatus "
                + "where dnc.phoneNumber in (:phoneNumbers)")
                .setParameter("phoneNumbers", keys)
                .setParameter("expiredStatus", DncStatus.EXPIRED)
                .executeUpdate();
    }

    @Override
    @Transactional
    public DNC load(String key) {
        try {
            return entityManager.createQuery(
                    "select dnc from DNC dnc "
                    + "where dnc.status = :activeStatus "
                    + "and dnc.phoneNumber = :phoneNumber", DNC.class)
                    .setParameter("phoneNumber", key)
                    .setParameter("activeStatus", DncStatus.ACTIVE)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    @Transactional
    public Map<String, DNC> loadAll(Collection<String> keys) {
        List<DNC> dncs = entityManager.createQuery(
                "select dnc from DNC dnc "
                + "where dnc.status = :activeStatus "
                + "and dnc.phoneNumber in (:phoneNumbers)", DNC.class)
                .setParameter("phoneNumbers", keys)
                .setParameter("activeStatus", DncStatus.ACTIVE)
                .getResultList();
        Map<String, DNC> resultMap = new HashMap<>();
        for (DNC dnc : dncs) {
            resultMap.put(dnc.getPhoneNumber(), dnc);
        }
        return resultMap;
    }

    @Override
    public Set<String> loadAllKeys() {
//        List<String> extensions = entityManager.createQuery(
//                "select dnc.phoneNumber from DNC dnc where "
//                + "(dnc.expireTimestamp is null or dnc.expireTimestamp > now())", String.class)
//                .getResultList();
//        return new HashSet<>(extensions);
        return null;
    }

}
