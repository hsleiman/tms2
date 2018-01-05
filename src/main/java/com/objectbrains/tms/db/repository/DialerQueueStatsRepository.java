/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.repository;

import com.objectbrains.tms.db.entity.DialerQueueTms;
import com.objectbrains.tms.db.entity.DialerStatsEntity;
import com.objectbrains.tms.service.dialer.predict.QueueAverages;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author connorpetty
 */
@Repository
@Transactional
public class DialerQueueStatsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TmsDialerQueueRepository queueRepository;

    public DialerStatsEntity getCurrentStats(long queuePk) {
        try {
            return entityManager.createQuery("select queue.currentQueueStats "
                    + "from DialerQueue queue where queue.pk = :queuePk", DialerStatsEntity.class)
                    .setParameter("queuePk", queuePk)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public long createStats(long queuePk) {
//        LocalDateTime now = LocalDateTime.now();
        DialerStatsEntity stats = new DialerStatsEntity();
        stats.setQueuePk(queuePk);
        stats.init();
        
        DialerQueueTms queue = queueRepository.getDialerQueue(queuePk);
        DialerStatsEntity currentStats = queue.getCurrentQueueStats();
        if (currentStats != null && !currentStats.hasEnded()) {
            currentStats.stop(LocalDateTime.now());
        }
        stats.setDialerQueue(queue);
        queue.setCurrentQueueStats(stats);
        entityManager.persist(stats);
        entityManager.flush();
        return stats.getPk();
    }

    /**
     *
     * @param defaultACL defaultAverageCallLength in milliseconds
     * @param defaultATBCA defaultAverageTimeBetweenCallArrivals in milliseconds
     * @return
     */
    public Map<Long, QueueAverages> getAllQueueAverages(long defaultACL, long defaultATBCA) {
        List<Object[]> results = entityManager.createQuery(
                "select queue.pk, coalesce(avg("
                + "  case when cdr.answermsec > 0 and cdr.createTimestamp > stats.startTime "
                + "   then cast(cdr.answermsec as double)"
                + "  else null end),"
                + " :defaultACL) as averageCallLength, "
                + "extract(epoch from (coalesce(stats.endTime, now()) - stats.startTime)) * 1000 as statTime, "
                + "stats.callResponseCount as callResponseCount "
                + "from CDR cdr "
                + " right join cdr.dialerQueue as queue "
                + " join queue.currentQueueStats as stats "
                + "group by queue.pk, stats.startTime, stats.endTime, stats.callResponseCount", Object[].class)
                .setParameter("defaultACL", defaultACL)
                .getResultList();

        Map<Long, QueueAverages> queueAverages = new HashMap<>();
        for (Object[] result : results) {
            Long queuePk = (Long) result[0];
            double averageCallLength = ((Number) result[1]).doubleValue();
            double statTime = ((Number) result[2]).doubleValue();
            double respondedCallCount = ((Number) result[3]).doubleValue();

            Double averageTimeBetweenCallArrivals;
            if (respondedCallCount <= 1 || statTime == 0.0) {
                averageTimeBetweenCallArrivals = (double) defaultATBCA;
            } else {
                averageTimeBetweenCallArrivals = statTime / respondedCallCount;
            }
            queueAverages.put(queuePk, new QueueAverages(averageTimeBetweenCallArrivals, averageCallLength));
        }
        return queueAverages;
    }

//    /**
//     *
//     * @param queuePk queuePk
//     * @param defaultACL defaultAverageCallLength in milliseconds
//     * @param defaultATBCA defaultAverageTimeBetweenCallArrivals in milliseconds
//     * @param defaultACDT defaultAverageCustomerDropTime in milliseconds
//     * @param defaultACRT defualtAverageCustomerResponseTime in milliseconds
//     * @return
//     */
//    public QueueRates getQueueRates(long queuePk, long defaultACL, long defaultATBCA, long defaultACDT, long defaultACRT) {
//        DialerQueueStats stats = getCurrentStats(queuePk);
//
//        double averageCustomerDropTime;
//        double averageCustomerResponseTime;
//        double callResponseProbability;
//        double droppedCount = stats.getDroppedCallCount();
//        double responseCount = stats.getRespondedCallCount();
//        double failedCount = stats.getFailedLoanCount();
//        if (droppedCount != 0.0) {
//            averageCustomerDropTime = stats.getTotalWaitTimeMillis() / droppedCount;
//        } else {
//            averageCustomerDropTime = defaultACDT;
//        }
//
//        if (responseCount != 0.0) {
//            averageCustomerResponseTime = stats.getTotalRespondTimeMillis() / responseCount;
//        } else {
//            averageCustomerResponseTime = defaultACRT;
//        }
//
//        double totalCount = failedCount + responseCount;
//        if (totalCount != 0.0) {
//            callResponseProbability = responseCount / totalCount;
//        } else {
//            callResponseProbability = 1.0;
//        }
//
//        return new QueueRates(getAllQueueAverages(defaultACL, defaultATBCA), averageCustomerDropTime, averageCustomerResponseTime, callResponseProbability);
//    }
}
