/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.dialer;

import com.objectbrains.tms.hazelcast.entity.AgentWeightedPriority;
import com.objectbrains.tms.hazelcast.entity.DialerStats;
import com.objectbrains.tms.service.AgentQueueAssociationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
@DisallowConcurrentExecution
public class ProgressiveCallBatchJob extends DialerQuartzJob {

    private static final Logger LOG = LoggerFactory.getLogger(ProgressiveCallBatchJob.class);

    public static final String NAME = "progressive-call-batch";

    public static JobDetail buildJobDetail(long dialerPk) {
        return JobBuilder.newJob(ProgressiveCallBatchJob.class)
                .setJobData(buildDataMap(dialerPk))
                .withIdentity(NAME + "-" + dialerPk, GROUP)
                .build();
    }

    @Autowired
    @Qualifier("tms-dialer-call-batch")
    private AsyncTaskExecutor executor;

    @Autowired
    private AgentQueueAssociationService associationService;

    @Override
    protected void executeInternal(JobExecutionContext context, Dialer dialer) throws JobExecutionException {
        final ProgressiveDialer pdialer = (ProgressiveDialer) dialer;

        double nCallsToMake = calculateNumberOfCallsToMake(pdialer, associationService.getParticipatingAgents(pdialer.getQueuePk()));
        LOG.info("***********************");
        LOG.info("Attempting to make {} new calls for dialer {}", nCallsToMake, dialer.getQueuePk());
        LOG.info("***********************");
        if (nCallsToMake < 1) {
            return;
        }
        try {
            Callable<Boolean> callable = new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    return pdialer.makeNextCall(null);
                }

            };
            List<Future<Boolean>> futures = new ArrayList<>((int) nCallsToMake);
            while (nCallsToMake > 0) {
                futures.add(executor.submit(callable));
                nCallsToMake--;
            }
            int count = 0;
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    count++;
                }
            }
            LOG.info("Made {} new calls for dialer {}", count, dialer.getQueuePk());
        } catch (InterruptedException ex) {
            throw new JobExecutionException(ex, false);
        } catch (ExecutionException ex) {
            throw new JobExecutionException(ex.getCause(), false);
        }
    }

    protected double calculateNumberOfCallsToMake(Dialer dialer, Map<Integer, AgentWeightedPriority> participatingAgents) {
        DialerStats stats = dialer.getDialerStats();
        int nCallsInProgress = stats.getInProgressCallCount() + stats.getPendingCallCount();
        double nCallsExpected = primaryAgentCount(participatingAgents) * dialer.getRecord().getSvDialerQueueSettings().getProgressiveCallsPerAgent();
        return nCallsExpected - nCallsInProgress;
    }

    private int primaryAgentCount(Map<Integer, AgentWeightedPriority> participatingAgents) {
        int count = 0;
        for (AgentWeightedPriority value : participatingAgents.values()) {
            if (value.getPrimaryGroup() != null && value.getPrimaryGroup()) {
                count++;
            }
        }
        return count;
    }

}
