/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.mapstore;

import com.amp.tms.db.entity.DialerLoanEntity;
import com.amp.tms.db.entity.DialerStatsEntity;
import com.amp.tms.hazelcast.entity.DialerLoan;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * 
 */
@Repository
@Transactional
public class DialerLoanRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private void create(Long dialerPk, Long loanPk, DialerLoan value) {
        DialerLoanEntity loan = new DialerLoanEntity();
        loan.copyFrom(value);

        DialerStatsEntity stats = entityManager.find(DialerStatsEntity.class, dialerPk);
        loan.setPk(new DialerLoanEntity.Pk(stats.getPk(), loanPk));
        loan.setDialerStats(stats);
        loan.setDialerQueue(stats.getDialerQueue());
        entityManager.persist(loan);
        value.setStatsPk(stats.getPk());
    }

    private DialerLoanEntity findLoan(Long dialerPk, Long loanPk) {
        return entityManager.find(DialerLoanEntity.class, new DialerLoanEntity.Pk(dialerPk, loanPk));
    }

    private List<DialerLoanEntity> findLoans(Long dialerPk, Collection<Long> loanPks) {
        return entityManager.createQuery("select loan "
                + "from DialerLoan loan "
                + "where loan.pk.statsPk = :statsPk "
                + "and loan.pk.loanPk in (:loanPks)",
                DialerLoanEntity.class)
                .setParameter("statsPk", dialerPk)
                .setParameter("loanPks", loanPks)
                .getResultList();
    }

    public void saveDialerLoan(Long dialerPk, Long loanPk, DialerLoan value) {
        Long statsPk = value.getStatsPk();

        DialerLoanEntity loan;
        if (statsPk != null) {
            loan = entityManager.find(DialerLoanEntity.class, new DialerLoanEntity.Pk(statsPk, value.getLoanPk()));
        } else {
            loan = findLoan(dialerPk, loanPk);
        }

        if (loan == null) {
            create(dialerPk, loanPk, value);
        } else {
            loan.copyFrom(value);
        }
    }

    public void saveDialerLoans(Long dialerPk, Map<Long, DialerLoan> map) {
        Map<Long, DialerLoan> loanMap = new HashMap<>(map);
        List<DialerLoanEntity> loans = findLoans(dialerPk, map.keySet());
        for (DialerLoanEntity loan : loans) {
            DialerLoan value = loanMap.remove(loan.getLoanPk());
            loan.copyFrom(value);
        }
        for (Map.Entry<Long, DialerLoan> entrySet : loanMap.entrySet()) {
            Long loanPk = entrySet.getKey();
            DialerLoan value = entrySet.getValue();
            create(dialerPk, loanPk, value);
        }
    }

    public DialerLoan getDialerLoan(Long dialerPk, Long loanPk) {
        DialerLoanEntity loan = findLoan(dialerPk, loanPk);
        return (loan == null) ? null : new DialerLoan(loan);
    }

    public Map<Long, DialerLoan> getDialerLoans(Long queuePk, Collection<Long> loanPks) {
        List<DialerLoanEntity> loans = findLoans(queuePk, loanPks);
        Map<Long, DialerLoan> retMap = new HashMap<>();
        for (DialerLoanEntity loan : loans) {
            retMap.put(loan.getLoanPk(), new DialerLoan(loan));
        }
        return retMap;
    }

}
