/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapstore;

import com.hazelcast.core.MapStore;
import com.amp.tms.db.entity.DialerCallEntity;
import com.amp.tms.db.entity.DialerLoanEntity;
import static com.amp.tms.hazelcast.Configs.DIALER_CALL_MAP_STORE_BEAN_NAME;
import com.amp.tms.hazelcast.entity.DialerCall;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author connorpetty
 */
@Repository(DIALER_CALL_MAP_STORE_BEAN_NAME)
public class DialerCallMapStore implements MapStore<String, DialerCall> {

    @PersistenceContext
    private EntityManager entityManager;

//    private DialerLoanEntity getCurrentDialerLoan(Long queuePk, Long loanPk) {
//        try {
//            return entityManager.createQuery(
//                    "select loan "
//                    + "from DialerLoan loan "
//                    + "where loan.dialerQueue.currentQueueStats = loan.dialerStats "
//                    + "and loan.dialerQueue.pk = :queuePk "
//                    + "and loan.pk.loanPk = :loanPk",
//                    DialerLoanEntity.class)
//                    .setParameter("queuePk", queuePk)
//                    .setParameter("loanPk", loanPk)
//                    .getSingleResult();
//        } catch (NoResultException ex) {
//            return null;
//        }
//    }

    private List<DialerCallEntity> getCalls(Collection<String> keys) {
        return entityManager.createQuery(
                "select call from DialerCall call where call.callUUID in (:callUUIDs)",
                DialerCallEntity.class)
                .setParameter("callUUIDs", keys)
                .getResultList();
    }

    private void create(DialerCall value) {
        DialerCallEntity call = new DialerCallEntity();
        call.copyFrom(value);
        
        DialerLoanEntity dialerLoan = entityManager.find(DialerLoanEntity.class, new DialerLoanEntity.Pk(value.getDialerPk(), value.getLoanPk()));
        call.setDialerQueue(dialerLoan.getDialerQueue());
        call.setDialerStats(dialerLoan.getDialerStats());
        call.setDialerLoan(dialerLoan);
        dialerLoan.setCurrentCall(call);
        entityManager.persist(call);
//        entityManager.flush();
//        value.setStatsPk(call.getStatsPk());
    }

    @Override
    @Transactional
    public void store(String callUUID, DialerCall value) {
        DialerCallEntity call = entityManager.find(DialerCallEntity.class, callUUID);
        if (call != null) {
            call.copyFrom(value);
        } else {
            create(value);
        }
    }

    @Override
    @Transactional
    public void storeAll(Map<String, DialerCall> map) {
        Map<String, DialerCall> storeCopy = new HashMap<>(map);
        List<DialerCallEntity> calls = getCalls(storeCopy.keySet());
        for (DialerCallEntity call : calls) {
            call.copyFrom(storeCopy.remove(call.getCallUUID()));
        }
        for (DialerCall call : storeCopy.values()) {
            create(call);
        }
    }

    @Override
    public void delete(String key) {
    }

    @Override
    public void deleteAll(Collection<String> keys) {
    }

    @Override
    @Transactional
    public DialerCall load(String callUUID) {
        DialerCallEntity call = entityManager.find(DialerCallEntity.class, callUUID);
        return (call == null) ? null : new DialerCall(call);
    }

    @Override
    @Transactional
    public Map<String, DialerCall> loadAll(Collection<String> keys) {
        List<DialerCallEntity> calls = getCalls(keys);
        Map<String, DialerCall> retMap = new HashMap<>();
        for (DialerCallEntity call : calls) {
            retMap.put(call.getCallUUID(), new DialerCall(call));
        }
        return retMap;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        return null;
    }

}
