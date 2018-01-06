/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.JobCompletableFuture;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.scheduler.annotation.QuartzJob;
import com.amp.tms.db.repository.DialerQueueStatsRepository;
import com.amp.tms.hazelcast.Configs;
import com.amp.tms.hazelcast.entity.AgentWeightedPriority;
import com.amp.tms.hazelcast.entity.DialerStats;
import com.amp.tms.hazelcast.entity.WeightedPriority;
import com.amp.tms.hazelcast.keys.AgentQueueKey;
import com.amp.tms.service.AgentQueueAssociationService;
import com.amp.tms.service.dialer.predict.AbandonCallDistribution;
import com.amp.tms.service.dialer.predict.AgentQueueKeyPredicate;
import com.amp.tms.service.dialer.predict.QRateCollator;
import com.amp.tms.service.dialer.predict.QRateCombinerFactory;
import com.amp.tms.service.dialer.predict.QRateMapper;
import com.amp.tms.service.dialer.predict.QRateReducerFactory;
import com.amp.tms.service.dialer.predict.QueueAverages;
import com.amp.tms.service.dialer.predict.QueueRates;
import com.amp.tms.service.dialer.predict.QueueVariables;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.util.FastMath;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author connorpetty
 */
@QuartzJob(name = CallBatchJob.NAME, group = CallBatchJob.GROUP)
public class CallBatchJob extends DialerQuartzJob implements InterruptableJob {

    public static final String NAME = "call-batch";

    private static final Logger LOG = LoggerFactory.getLogger(CallBatchJob.class);

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private AgentQueueAssociationService associationService;
    
    @Autowired
    private DialerQueueStatsRepository queueStatsRepository;

    private IMap<AgentQueueKey, AgentWeightedPriority> agentQueueMap;
    private IExecutorService executor;

    private JobTracker jobTracker;

    private JobCompletableFuture<Double> serviceRateFuture = null;
    private Future<Integer> callCalculationFuture = null;

    @PostConstruct
    private void init() {
        agentQueueMap = hazelcastService.getMap(Configs.QUEUE_WEIGHTED_PRIORITY_MAP);
        jobTracker = hazelcastService.getJobTracker("default");
        executor = hazelcastService.getExecutorService("call-batch-executor");
    }

    @Override
    protected void executeInternal(JobExecutionContext context, Dialer dialer) throws JobExecutionException {
        if (!(dialer instanceof PredictiveDialer)) {
            JobExecutionException jex = new JobExecutionException("dialer is not a PredictiveDialer instance");
            jex.setUnscheduleFiringTrigger(true);
            throw jex;
        }
        PredictiveDialer pdialer = (PredictiveDialer) dialer;

        Map<Integer, AgentWeightedPriority> agentWeightedPriorities = associationService.getParticipatingAgents(pdialer.getQueuePk());
        DialerStats stats = dialer.getDialerStats();
        QueueRates queueRates = stats.getQueueRates(5000, 30000);

        double serviceRate;
        Map<Long, QueueAverages> queueAverages = queueStatsRepository.getAllQueueAverages(300000, 1000);
        if (queueAverages.isEmpty()) {
            LOG.warn("queueAverages was empty, defaulting service rate to 5 minutes");
            serviceRate = 1.0 / 300000;//5 minutes per call
        } else {
            serviceRateFuture = jobTracker.newJob(KeyValueSource.fromMap(agentQueueMap))
                    .keyPredicate(new AgentQueueKeyPredicate(agentWeightedPriorities.keySet()))
                    .mapper(new QRateMapper(queueAverages, agentWeightedPriorities))
                    .combiner(new QRateCombinerFactory())
                    .reducer(new QRateReducerFactory())
                    .submit(new QRateCollator());
            try {
                serviceRate = serviceRateFuture.get();
            } catch (InterruptedException ex) {
                throw new JobExecutionException(ex);
            } catch (ExecutionException ex) {
                throw new JobExecutionException(ex.getCause());
            }
        }

        int nServers = agentWeightedPriorities.size();
        int nCallsInSystem = stats.getInProgressCallCount();

        double targetAbandonPercent = 0.03;//TODO
        QueueVariables variables = new QueueVariables(serviceRate,
                1.0 / queueRates.getAverageCustomerDropTime(),
                1.0 / queueRates.getAverageCustomerResponseTime(),
                nServers);
//        AbandonCallSolver solver = new AbandonCallSolver(variables, nCallsInSystem, targetAbandonPercent);
//        int nCallsToMake = (int) (solver.solve() / queueRates.getCallResponseProbability());

//        AbandonCallSolver solver = new AbandonCallSolver(variables, nCallsInSystem, targetAbandonPercent);
        int nCallsToMake = (int) (nCallsToMake(variables, nCallsInSystem, targetAbandonPercent) / queueRates.getCallResponseProbability());

//        CallCalculation calc = new CallCalculation(serviceRate,
//                1.0 / queueRates.getAverageCustomerDropTime(),
//                1.0 / queueRates.getAverageCustomerResponseTime(),
//                queueRates.getCallResponseProbability(),
//                nServers, nCallsInSystem, targetAbandonPercent);
//
//        callCalculationFuture = executor.submit(calc);//TODO select member
//        int nCallsToMake;
//        try {
//            nCallsToMake = callCalculationFuture.get();
//        } catch (InterruptedException ex) {
//            throw new JobExecutionException(ex);
//        } catch (ExecutionException ex) {
//            throw new JobExecutionException(ex.getCause());
//        }
        LOG.info("making {} new calls", nCallsToMake);
        while (nCallsToMake > 0) {
            try {
                pdialer.makeNextCall(null);
            } catch (DialerException ex) {
                throw new JobExecutionException(ex);
            }
            nCallsToMake--;
        }
    }

    private static double nCallsToMake(final QueueVariables variables,
            final int nCallsInSystem, final double targetAbandonPercent) {
        UnivariateFunction function = new UnivariateFunction() {
            @Override
            public double value(double x) {
                int callCount = (int) x;
                double mean = new AbandonCallDistribution(variables, nCallsInSystem, callCount).getNumericalMean();
                return mean - targetAbandonPercent * (callCount + nCallsInSystem);
//targetLine.value(callCount);
            }

        };

        if (function.value(0.0) > 0) {
            return 0.0;
        }

        int min = variables.nServers - nCallsInSystem;
        int max = min+1;
        int offset = 1;

        while (max < 1000 && function.value(max) < 0) {
            offset <<= 1;
            max += offset;
        }
        
        min = max - offset;
        if (max >= 1000) {
            return min;
        }

        BrentSolver solver = new BrentSolver(0.5);

        double value = solver.solve(50, function, min, max);

        double[] x = new double[3];
        double[] y = new double[3];

//        int nPoints = 2;
        x[0] = FastMath.floor(value);
        y[0] = function.value(x[0]);

        x[1] = FastMath.ceil(value);
        y[1] = function.value(x[1]);

//            if (x[0] == x[1]) {
        if (y[0] > 0) {
            x[1] = x[0];
            y[1] = y[0];

            x[0]--;
            y[0] = function.value(x[0]);
        } else if (y[1] < 0) {
            x[0] = x[1];
            y[0] = y[1];

            x[1]++;
            y[1] = function.value(x[1]);
        }
//            }

//            if (y[0] > 0) {
//                x[2] = x[0] - 1;
//                y[2] = function.value(x[2]);
//                nPoints = 3;
//            }
//            if (y[1] < 0) {
//                x[2] = x[0] + 1;
//                y[2] = function.value(x[2]);
//                nPoints = 3;
//            }
//            return solve(x, y, nPoints);
//            System.out.println("x[0]="+x[0]+", y[0]="+y[0]+", x[1]="+x[1]+", y[1]="+y[1]);
        double a = (y[1] - y[0]) / (x[1] - x[0]);
        double b = y[0] - a * x[0];
        return -b / a;
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        if (serviceRateFuture != null) {
            serviceRateFuture.cancel(true);
        }
//        if (callCalculationFuture != null) {
//            callCalculationFuture.cancel(true);
//        }
    }

}
