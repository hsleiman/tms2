/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer.predict;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class CallCalculation implements Callable<Integer>, DataSerializable {

    private static final Logger LOG = LoggerFactory.getLogger(CallCalculation.class);
    double serviceRate;
    double regensRate;
    double responseRate;
    double responseProb;
    int nServers;
    int nCallsInSystem;
    double targetAbandonPercent;

    private static void checkGTZero(double value, String valueName) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(valueName + " cannot be NaN or Inf");
        }
        if (value <= 0) {
            throw new IllegalArgumentException(valueName + " cannot be <= 0");
        }
    }

    private static void checkPercent(double value, String valueName) {
        checkGTZero(value, valueName);
        if (value > 1) {
            throw new IllegalArgumentException(valueName + " cannot be > 1");
        }
    }

    private CallCalculation() {
    }

    public CallCalculation(double serviceRate, double regensRate,
            double responseRate, double responseProb,
            int nServers, int nCallsInSystem,
            double targetAbandonPercent) {
        checkGTZero(serviceRate, "serviceRate");
        checkGTZero(regensRate, "regensRate");
        checkGTZero(responseRate, "responseRate");
        checkPercent(responseProb, "responseProb");
        checkGTZero(nServers, "nServers");
        if (nCallsInSystem < 0) {
            throw new IllegalArgumentException("nCallsInSystem cannot be < 0");
        }
        checkPercent(targetAbandonPercent, "targetAbandonPercent");
        this.serviceRate = serviceRate;
        this.regensRate = regensRate;
        this.responseRate = responseRate;
        this.responseProb = responseProb;
        this.nServers = nServers;
        this.nCallsInSystem = nCallsInSystem;
        this.targetAbandonPercent = targetAbandonPercent;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeDouble(serviceRate);
        out.writeDouble(regensRate);
        out.writeDouble(responseRate);
        out.writeDouble(responseProb);
        out.writeInt(nServers);
        out.writeInt(nCallsInSystem);
        out.writeDouble(targetAbandonPercent);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        serviceRate = in.readDouble();
        regensRate = in.readDouble();
        responseRate = in.readDouble();
        responseProb = in.readDouble();
        nServers = in.readInt();
        nCallsInSystem = in.readInt();
        targetAbandonPercent = in.readDouble();
    }

    private static double divDiff(Point p0, Point p1) {
        return (p1.y - p0.y) / (p1.x - p0.x);
    }

    private static double divDiff(Point p0, Point p1, Point p2) {
        return (divDiff(p1, p2) - divDiff(p0, p1)) / (p2.x - p0.x);
    }

    //returns whether the points make a positive curve (curving upward)
    private static boolean isPositive(Point p0, Point p1, Point p2) {
        Point[] points = new Point[]{p0, p1, p2};
        Arrays.sort(points);
        return divDiff(points[0], points[1]) < divDiff(points[0], points[2]);
    }

    @Override
    public Integer call() throws Exception {

        LOG.info("starting calculation [\n"
                + "\tserviceRate: {}\n"
                + "\tregensRate: {}\n"
                + "\tresponseRate: {}\n"
                + "\tresponseProb: {}\n"
                + "\tnServers: {}\n"
                + "\tnCallsInSystem: {}\n"
                + "\ttargetAbandonPercent: {}\n"
                + "]",
                serviceRate,
                regensRate,
                responseRate,
                responseProb,
                nServers,
                nCallsInSystem,
                targetAbandonPercent);
        long startTime = System.currentTimeMillis();
        Point zeroPoint = createZeroPoint();
        //check that the zero point does not already exceed our threshold;
        if (zeroPoint.y > 0) {
            return 0;
        }
        Point balancePoint = createBalancePoint();

        Point extraPoint;
        if (balancePoint.y > 0) {
            int x = balancePoint.x / 2;
            if (x == zeroPoint.x) {
                extraPoint = new Point(balancePoint.x + 1);
            } else {
                extraPoint = new Point(x);
            }
        } else {
            extraPoint = new Point(2 * balancePoint.x - zeroPoint.x);
        }

        Point p1 = zeroPoint;
        Point p2 = balancePoint;
        Point p3 = extraPoint;

        int maxIter = 7;
        int i = 0;

        double xRaw = 0.0;
        /*
         performs Muller's method to find the largest callCount that does
         not exceed the targetAbandonRate
         */
        do {
            if (i >= maxIter) {
                throw new Exception("took too long");
            }
            i++;

            double w = divDiff(p3, p2) + divDiff(p3, p1) - divDiff(p2, p1);

            double temp = Math.sqrt(w * w - 4 * p3.y * divDiff(p3, p2, p1));

            double xp = (p3.x - 2 * p3.y / (w + temp));
            double xn = (p3.x - 2 * p3.y / (w - temp));

            xRaw = isPositive(p1, p2, p3) ? Math.max(xp, xn) : Math.min(xp, xn);
            int x = (int) xRaw;
            Point p = (x == p3.x) ? p3 : new Point(x);

            p1 = p2;
            p2 = p3;
            p3 = p;
        } while (Math.abs(p3.x - p2.x) > 1);
        int result = (int) (xRaw / responseProb);

        long time = System.currentTimeMillis() - startTime;
        LOG.info("result [{}] took [{}] ms", result, time);
        return result;
    }

    private Point createZeroPoint() {
        if (nCallsInSystem > nServers) {
            return new Point(0);
        }
        return new Point(nServers - nCallsInSystem, -targetAbandonPercent);
    }

    private Point createBalancePoint() {
        double n = nServers * serviceRate / responseRate;

        int zeroPoint = Math.max(0, nServers - nCallsInSystem);

        if (zeroPoint > n + nServers) {
            //the zero point is beyond our guess so just take the next point
            return new Point(zeroPoint + 1);
        }
        int point = (int) Math.ceil(n);
        return new Point(2 * point + nServers - nCallsInSystem);
    }

    private class Point implements Comparable<Point> {

        public int x;
        public double y;

        public Point(int x) {
            this.x = x;
            ExactAbandonPercentStrategy strat = new ExactAbandonPercentStrategy(x);
            strat.run();
            this.y = strat.mean() - targetAbandonPercent;
//            this(x, calcAbandonPercent(x) - targetAbandonPercent);
        }

        public Point(int x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "x=[" + x + "] y=[" + y + "]";
        }

        @Override
        public int compareTo(Point o) {
            return x - o.x;
        }

    }

    abstract class AbandonPercentStrategy implements Runnable {

        protected int callCount;

        public AbandonPercentStrategy(int callCount) {
            this.callCount = callCount;
        }

        abstract double stdDev();

        abstract double mean();

        abstract long maxMemoryCost();

        abstract long maxExecutionCost();

//        abstract long execute();
    }

    class ExactAbandonPercentStrategy extends AbandonPercentStrategy {

        private double mean = 0.0;

        public ExactAbandonPercentStrategy(int callCount) {
            super(callCount);
        }

        @Override
        double mean() {
            return mean;
        }

        @Override
        double stdDev() {
            return 0;
        }

        @Override
        long maxExecutionCost() {
            int totalCallCount = nCallsInSystem + callCount;
            if (totalCallCount <= nServers) {
                return 0;
            }
            long cost = 0;
            for (int i = 0; i <= callCount; i++) {
                final int jMax = totalCallCount - i;
                final int kMax = Math.max(0, jMax - nServers);

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

        @Override
        public long maxMemoryCost() {
            int totalCallCount = nCallsInSystem + callCount;

            if (totalCallCount <= nServers) {
                return 0;
            }

            int nsm = totalCallCount + 1;
            //check to make sure that calculation will not cause the system
            //to run out of memory
            long doubleCount = nsm - nServers + nsm * 3;
            for (int i = 0; i < nsm - nServers; i++) {
                doubleCount += nsm - i;
            }
//        if (doubleCount * 8 > Runtime.getRuntime().freeMemory()) {
//            throw new RuntimeException("Not enough memory to perform calculation");
//        }
            return doubleCount * 8;
        }

        private double d(int i, int j) {
            if (j <= nServers) {
                return 0.0;
            }
            double rRate = i * responseRate;
            double sRate = nServers * serviceRate;
            double aRate = (j - nServers) * regensRate;
            return aRate / (rRate + sRate + aRate);
        }

        private double s(int i, int j) {
            double rRate = i * responseRate;
            double sRate;
            double aRate;
            if (j > nServers) {
                sRate = nServers * serviceRate;
                aRate = (j - nServers) * regensRate;
            } else {
                sRate = j * serviceRate;
                aRate = 0.0;
            }
            return sRate / (sRate + rRate + aRate);
        }

        private double a(int i, int j) {
            if (j < 0) {
                return 0.0;
            }
            double rRate = i * responseRate;
            double sRate;
            double aRate;
            if (j > nServers) {
                sRate = nServers * serviceRate;
                aRate = (j - nServers) * regensRate;
            } else {
                sRate = j * serviceRate;
                aRate = 0.0;
            }
            return rRate / (rRate + sRate + aRate);
        }

        @Override
        public void run() {
            int totalCallCount = nCallsInSystem + callCount;

            if (totalCallCount <= nServers) {
                return;
            }

            //this is just a placeholder value
            int nsm = totalCallCount + 1;

            double[][] resultMatrix = new double[nsm - nServers][];
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
                final int kMax = Math.max(0, jMax - nServers);
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
            double meanAbandonCount = 0.0;
            for (int k = 0; k < resultMatrix.length; k++) {
                //get the last result of each matrix column
                double result = resultMatrix[k][totalCallCount - k];
                meanAbandonCount += k * result;
            }
            mean = meanAbandonCount / totalCallCount;
        }

    }

    class EstimateAbandonPercentStrategy extends AbandonPercentStrategy {

        private final RandomGenerator random;
        private final ExponentialDistribution servicingDist;
        private final ExponentialDistribution responseDist;
        private final ExponentialDistribution regensDist;

        private final PriorityQueue<Event> eventQueue;
        private CallDropEvent headDropEvent = null;
        private CallDropEvent tailDropEvent = null;

        final Mean mean = new Mean();
        private final StandardDeviation stdDev = new StandardDeviation();

//        private int nextEventId;
        private int nCallsBeingServiced;
        private int dropCount;
        private double currentTime;

        public EstimateAbandonPercentStrategy(int callCount) {
            super(callCount);
            this.random = new Well19937c();
            this.servicingDist = new ExponentialDistribution(random, 1.0 / serviceRate);
            this.responseDist = new ExponentialDistribution(random, 1.0 / responseRate);
            this.regensDist = new ExponentialDistribution(random, 1.0 / regensRate);
            this.eventQueue = new PriorityQueue<>(nCallsInSystem + callCount);
        }

        @Override
        long maxMemoryCost() {
            int eventSize = 8 + 8;
            int dropEventSize = eventSize + 16;
            int totalCallCount = nCallsInSystem + callCount;
            int maxCallDropCount = Math.max(0, totalCallCount - nServers);
            int normalEventCount = totalCallCount - maxCallDropCount;
            return normalEventCount * eventSize + maxCallDropCount * dropEventSize;
        }

        @Override
        long maxExecutionCost() {
            int maxEventCount = nCallsInSystem + callCount;
//            int maxCallDropCount = maxEventCount - nServers;
            //this a calculated constant based off comparing run times of the exact calc vs this estimated one.
            return (long) (8.5 * maxEventCount * Math.log(maxEventCount) / Math.log(2));
        }

        @Override
        double stdDev() {
            return stdDev.getResult();
        }

        @Override
        double mean() {
            return mean.getResult();
        }

        @Override
        public void run() {
            eventQueue.clear();
            headDropEvent = null;
            tailDropEvent = null;

            nCallsBeingServiced = 0;
            dropCount = 0;
            currentTime = 0.0;
            for (int i = 0; i < nCallsInSystem; i++) {
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
            double result = dropCount / (double) (nCallsInSystem + callCount);
            mean.increment(result);
            stdDev.increment(result);
        }

        private void handleCallResponse() {
            if (nCallsBeingServiced < nServers) {
                //call is getting serviced
                nCallsBeingServiced++;
                eventQueue.add(new CallServicedEvent());
            } else {
                eventQueue.add(new CallDropEvent());
            }
        }

        private void handleCallServiced() {
            if (headDropEvent == null) {
                //mark server as waiting
                nCallsBeingServiced--;
            } else {
                headDropEvent.remove();
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

        private class CallDropEvent extends Event {

            private CallDropEvent prev = null;
            private CallDropEvent next = null;

            public CallDropEvent() {
                super(regensDist);
                //call must wait in the drop queue
                add();
            }

            private void add() {
                if (headDropEvent == null) {
                    headDropEvent = this;
                }
                if (tailDropEvent != null) {
                    tailDropEvent.next = this;
                }
                prev = tailDropEvent;
                tailDropEvent = this;
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
                if (tailDropEvent == this) {
                    tailDropEvent = prev;
                    removed = true;
                }
                if (headDropEvent == this) {
                    headDropEvent = next;
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
                    dropCount++;
                }
            }

        }
    }

}
