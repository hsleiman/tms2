/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer.predict;

import java.util.PriorityQueue;
import org.apache.commons.math3.distribution.AbstractIntegerDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.FastMath;

/**
 *
 * @author HS
 */
public class AbandonCallDistribution extends AbstractIntegerDistribution {

    private static final double[] ZERO_DISTRIBUTION = new double[]{1.0};

    private final QueueVariables variables;
    private final int callCount;
    private final int totalCallCount;
    private double[] abandonDistribution = null;
    private double mean = -1;

    public AbandonCallDistribution(QueueVariables variables, int nCallsInSystem, int callCount) {
        super(new Well19937c(System.currentTimeMillis()));
        this.variables = variables;
        this.callCount = callCount;
        this.totalCallCount = nCallsInSystem + callCount;
        if (getSupportUpperBound() == 0) {
            abandonDistribution = ZERO_DISTRIBUTION;
            mean = 0;
        }
    }

    private double d(int i, int j) {
        if (j <= variables.nServers) {
            return 0.0;
        }
        double rRate = i * variables.responseRate;
        double sRate = variables.nServers * variables.serviceRate;
        double aRate = (j - variables.nServers) * variables.regensRate;
        return aRate / (rRate + sRate + aRate);
    }

    private double s(int i, int j) {
        double rRate = i * variables.responseRate;
        double sRate;
        double aRate;
        if (j > variables.nServers) {
            sRate = variables.nServers * variables.serviceRate;
            aRate = (j - variables.nServers) * variables.regensRate;
        } else {
            sRate = j * variables.serviceRate;
            aRate = 0.0;
        }
        return sRate / (sRate + rRate + aRate);
    }

    private double a(int i, int j) {
        if (j < 0) {
            return 0.0;
        }
        double rRate = i * variables.responseRate;
        double sRate;
        double aRate;
        if (j > variables.nServers) {
            sRate = variables.nServers * variables.serviceRate;
            aRate = (j - variables.nServers) * variables.regensRate;
        } else {
            sRate = j * variables.serviceRate;
            aRate = 0.0;
        }
        return rRate / (rRate + sRate + aRate);
    }

    private double[] getDist() {
        if (abandonDistribution == null) {
            //this is just a placeholder value
            int nsm = totalCallCount + 1;

            double[][] resultMatrix = new double[nsm - variables.nServers][];
            for (int i = 0; i < resultMatrix.length; i++) {
                resultMatrix[i] = new double[nsm - i];
            }
            resultMatrix[0][0] = 1.0;

            double[] aCache = new double[nsm];
            double[] sCache = new double[nsm];
            double[] dCache = new double[nsm];

            for (int i = callCount; i >= 0; i--) {

                final int jMax = totalCallCount - i;
                for (int j = 0; j <= jMax; j++) {
                    aCache[j] = a(i + 1, j - 1);
                    sCache[j] = s(i, j + 1);
                    dCache[j] = d(i, j + 1);
                }
                final int kMax = Math.max(0, jMax - variables.nServers);
                for (int k = 0; k <= kMax; k++) {
                    for (int j = jMax - k, jIndex = 0; j >= 0; j--, jIndex++) {
                        double result = resultMatrix[k][jIndex];
                        if (i < callCount) {
                            result *= aCache[j];
                        }
                        if (k > 0) {
                            result += resultMatrix[k - 1][jIndex] * dCache[j];
                        }
                        if (jIndex > 0) {
                            result += resultMatrix[k][jIndex - 1] * sCache[j];
                        }
                        resultMatrix[k][jIndex] = result;
                    }
                }
            }
            abandonDistribution = new double[resultMatrix.length];
            mean = 0;
            for (int k = 0; k < resultMatrix.length; k++) {
                //get the last result of each matrix column
                abandonDistribution[k] = resultMatrix[k][totalCallCount - k];
                mean += k * abandonDistribution[k];
            }
        }
        return abandonDistribution;
    }

    @Override
    public double getNumericalMean() {
        if (mean == -1) {
            int nsm = totalCallCount + 1;

            double[] results = new double[nsm];

            results[totalCallCount - callCount] = 1.0;

            mean = 0.0;
            for (int i = callCount; i >= 0; i--) {
                final int jMax = totalCallCount - i;
                for (int j = (i == callCount) ? jMax - 1 : jMax; j >= 0; j--) {
                    double result = 0.0;
                    if (j < jMax) {
                        double rRate = i * variables.responseRate;
                        double sRate;
                        double tmp = results[j + 1];
                        //service rate
                        if (j + 1 > variables.nServers) {
                            sRate = variables.nServers * variables.serviceRate;
                            double aRate = (j + 1 - variables.nServers) * variables.regensRate;
                            tmp /= (rRate + sRate + aRate);
                            result += aRate * tmp;
                            mean += result;
                        } else {
                            sRate = (j + 1) * variables.serviceRate;
                            tmp /= (rRate + sRate);
                        }
                        result += sRate * tmp;
                    }
                    if (j > 0) {
                        //response rate
//                        result += results[j-1] * a(i+1, j-1);

                        double rRate = (i + 1) * variables.responseRate;
                        double sRate;
                        double aRate;
                        if (j - 1 > variables.nServers) {
                            sRate = variables.nServers * variables.serviceRate;
                            aRate = (j - 1 - variables.nServers) * variables.regensRate;
                        } else {
                            sRate = (j - 1) * variables.serviceRate;
                            aRate = 0.0;
                        }
                        result += results[j - 1] * rRate / (rRate + sRate + aRate);
                    }
                    results[j] = result;
                }
            }
        }
        return mean;
//        double mean = 0;
//        double[] dist = getDist();
//        for (int i = 0; i < dist.length; i++) {
//            mean += i * dist[i];
//        }
//        return mean;
    }

    @Override
    public double getNumericalVariance() {
        double mean = 0;
        double meanOfSquares = 0;
        double[] dist = getDist();
        for (int i = 0; i < dist.length; i++) {
            double prob = i * dist[i];
            mean += prob;
            meanOfSquares += i * prob;
        }
        return meanOfSquares - mean * mean;
    }
    
    
    public int getNumericalMedian(){
        double sum = 0;
        double[] dist = getDist();
        for (int i = 0; i < dist.length; i++) {
            sum += dist[i];
            if(sum >= 0.5){
                return i;
            }
        }
        return -1;
    }
    
    public double getNumericalSkew(){
        double mean = 0;
        double meanOfSquares = 0;
        double meanOfCubes = 0;
        double[] dist = getDist();
        for (int i = 0; i < dist.length; i++) {
            double prob = i * dist[i];
            mean += prob;
            prob *= i;
            meanOfSquares += prob;
            prob *= i;
            meanOfCubes += prob;
        }
        double meanSq = mean * mean;
        double variance = meanOfSquares - meanSq;
        return (meanOfCubes - 3 * mean * variance - mean *meanSq)/FastMath.pow(variance, 1.5);
    }

//    public static void main(String[] args) {
//        //64.354520 29.636468 99.650731 99.0 468.0 0.0 154.483280
//        QueueVariables vars = new QueueVariables(64.354520, 29.636468, 99.650731, 1);
//
//        AbandonCallDistribution dist = new AbandonCallDistribution(vars, 1, 4);
////        AbandonCallDistribution dist = new AbandonCallDistribution(vars, 100, 0);
//
////        dist.getDist2();
//
//        System.out.println("mean: " + dist.getNumericalMean());
//        System.out.println("variance: "+dist.getNumericalVariance());
//        System.out.println("skew: "+dist.getNumericalSkew());
////        dist
//        
//        double[] di = dist.getDist();
//        System.out.println("size: "+ (di.length-1));
//        System.out.println("last: "+ di[di.length -1]);
//        
//        
//        for(int i = 0; i < di.length; i++){
//            System.out.println("i="+i+", pdf="+di[i]);
//        }
//        System.out.println("");
//        for(int i = 0; i < di.length; i++){
//            System.out.println("i="+i+", cdf="+dist.cumulativeProbability(i));
//        }
//        
//    }
    public long distributionExecutionCost() {
        if (getSupportUpperBound() == 0) {
            return 0;
        }

        long cost = 0;
        for (int i = 0; i <= callCount; i++) {
            final int jMax = totalCallCount - i;
            final int kMax = Math.max(0, jMax - variables.nServers);

//                int cacheCost = 3 * jMax;
            int cacheCost = jMax;
            cost += cacheCost;
            int loopCount = ((1 + kMax) * (2 * jMax - kMax)) / 2;
//                int loopCost = loopCount * 3;
            int loopCost = loopCount;
            cost += loopCost;
        }
        return cost;
    }

    public long distributionMemoryCost() {
        if (getSupportUpperBound() == 0) {
            return 0;
        }

        int nsm = totalCallCount + 1;
        //check to make sure that calculation will not cause the system
        //to run out of memory
        long doubleCount = nsm - variables.nServers + nsm * 3;
        for (int i = 0; i < nsm - variables.nServers; i++) {
            doubleCount += nsm - i;
        }
//        if (doubleCount * 8 > Runtime.getRuntime().freeMemory()) {
//            throw new RuntimeException("Not enough memory to perform calculation");
//        }
        return doubleCount * 8;
    }

    @Override
    public double probability(int x) {
        return getDist()[x];
    }

    @Override
    public double cumulativeProbability(int x) {
        double sum = 0;
        double[] dist = getDist();
        for (int i = 0; i <= x; i++) {
            sum += dist[i];
        }
        return sum;
    }

    @Override
    public int getSupportLowerBound() {
        return 0;
    }

    @Override
    public int getSupportUpperBound() {
        return Math.max(0, totalCallCount - variables.nServers);
    }

    @Override
    public boolean isSupportConnected() {
        return true;
    }

    @Override
    public int sample() {
        if (abandonDistribution != null) {
            return super.sample();
        }
        return new SampleGenerator().generateSample();
    }

    @Override
    public int[] sample(int sampleSize) {
        if (sampleSize <= 0) {
            throw new NotStrictlyPositiveException(
                    LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
        }
        int[] out = new int[sampleSize];
        if (abandonDistribution != null) {
            for (int i = 0; i < sampleSize; i++) {
                out[i] = super.sample();
            }
        } else {
            SampleGenerator generator = new SampleGenerator();
            for (int i = 0; i < sampleSize; i++) {
                out[i] = generator.generateSample();
            }
        }
        return out;
    }

    public long sampleMemoryCost() {
        int eventSize = 8 + 8;
        int dropEventSize = eventSize + 16;
        int maxCallDropCount = Math.max(0, totalCallCount - variables.nServers);
        int normalEventCount = totalCallCount - maxCallDropCount;
        return normalEventCount * eventSize + maxCallDropCount * dropEventSize;
    }

    public long sampleExecutionCost() {
        int maxEventCount = totalCallCount;
//            int maxCallDropCount = maxEventCount - nServers;
        //this a calculated constant based off comparing run times of the exact calc vs this estimated one.
        return (long) (8.5 * maxEventCount * Math.log(maxEventCount) / Math.log(2));
    }

    private class SampleGenerator {

        private final ExponentialDistribution servicingDist;
        private final ExponentialDistribution responseDist;
        private final ExponentialDistribution regensDist;

        private final PriorityQueue<Event> eventQueue;
        private CallAbandonEvent headAbandonEvent = null;
        private CallAbandonEvent tailAbandonEvent = null;

        private int nCallsBeingServiced;
        private int abandonCount;
        private double currentTime;

        public SampleGenerator() {
            this.servicingDist = new ExponentialDistribution(random, 1.0 / variables.serviceRate);
            this.responseDist = new ExponentialDistribution(random, 1.0 / variables.responseRate);
            this.regensDist = new ExponentialDistribution(random, 1.0 / variables.regensRate);
            this.eventQueue = new PriorityQueue<>(totalCallCount);
        }

        public int generateSample() {
            eventQueue.clear();
            headAbandonEvent = null;
            tailAbandonEvent = null;

            nCallsBeingServiced = 0;
            abandonCount = 0;
            currentTime = 0.0;
            for (int i = callCount; i < totalCallCount; i++) {
                handleCallResponse();
            }
            for (int i = 0; i < callCount; i++) {
                eventQueue.add(new CallResponseEvent());
            }
            while (!eventQueue.isEmpty()) {
                Event event = eventQueue.poll();

                currentTime = event.eventTime;
                event.process();
            }
            return abandonCount;
//            double result = dropCount / (double) (nCallsInSystem + callCount);
        }

        private void handleCallResponse() {
            if (nCallsBeingServiced < variables.nServers) {
                //call is getting serviced
                nCallsBeingServiced++;
                eventQueue.add(new CallServicedEvent());
            } else {
                eventQueue.add(new CallAbandonEvent());
            }
        }

        private void handleCallServiced() {
            if (headAbandonEvent == null) {
                //mark server as waiting
                nCallsBeingServiced--;
            } else {
                headAbandonEvent.remove();
                eventQueue.add(new CallServicedEvent());
            }
        }

        private abstract class Event implements Comparable<Event> {

            final double eventTime;

            public Event(ExponentialDistribution dist) {
                this.eventTime = dist.sample() + currentTime;
            }

            public abstract void process();

            @Override
            public final int compareTo(Event o) {
                return Double.compare(this.eventTime, o.eventTime);
            }

        }

        private class CallServicedEvent extends Event {

            public CallServicedEvent() {
                super(servicingDist);
            }

            @Override
            public void process() {
                handleCallServiced();
            }

        }

        private class CallResponseEvent extends Event {

            public CallResponseEvent() {
                super(responseDist);
            }

            @Override
            public void process() {
                handleCallResponse();
            }

        }

        private class CallAbandonEvent extends Event {

            private CallAbandonEvent prev = null;
            private CallAbandonEvent next = null;

            public CallAbandonEvent() {
                super(regensDist);
                //call must wait in the drop queue
                add();
            }

            private void add() {
                if (headAbandonEvent == null) {
                    headAbandonEvent = this;
                }
                if (tailAbandonEvent != null) {
                    tailAbandonEvent.next = this;
                }
                prev = tailAbandonEvent;
                tailAbandonEvent = this;
            }

            private boolean remove() {
                boolean removed = false;
                if (prev != null) {
                    prev.next = next;
                    removed = true;
                }
                if (next != null) {
                    next.prev = prev;
                    removed = true;
                }
                if (tailAbandonEvent == this) {
                    tailAbandonEvent = prev;
                    removed = true;
                }
                if (headAbandonEvent == this) {
                    headAbandonEvent = next;
                    removed = true;
                }
                next = null;
                prev = null;
                return removed;
            }

            @Override
            public void process() {
                if (remove()) {
                    //if this event has not been picked up by a servicing event
                    //then we mark the call as dropped
                    abandonCount++;
                }
            }

        }
    }
}
