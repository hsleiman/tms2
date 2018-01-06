/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer.predict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.math3.analysis.FunctionUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.MullerSolver;
import org.apache.commons.math3.analysis.solvers.MullerSolver2;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.SimpleCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.util.FastMath;
import org.springframework.util.StopWatch;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 * // * @param <VariableContext> a variable context
 */
public class AbandonCallSolver {

    private static final int ACCURATE_SAMPLE_SIZE = 1000;

    private static class AbandonCountPoint extends WeightedObservedPoint {

        private final boolean isExact;
        private final double variance;

        public AbandonCountPoint(int callCount, double mean, double variance, int sampleSize, boolean isExact) {
            super(sampleSize, callCount, mean);
            this.variance = variance;
            this.isExact = isExact;
        }

        public boolean isExact() {
            return isExact;
        }

        public int getCallCount() {
            return (int) getX();
        }

        public double getMean() {
            return getY();
        }

        public double getVariance() {
            return variance;
        }

        public int getSampleSize() {
            return (int) getWeight();
        }

        @Override
        public String toString() {
            return "AbandonCallPoint[x=" + getX() + ", y=" + getY() + ", var=" + variance + ", sampleSize=" + getSampleSize() + ", isExact=" + isExact;
        }

    }
//    private static final ACEstimatorFunction FUNCTION = new ACEstimatorFunction();
    private final QueueVariables variables;
    private final int nCallsInSystem;
    private final double[] targetCoef;
    private final PolynomialFunction targetLine;
    private final TreeMap<Integer, AbandonCountPoint> points;
//    private SortedSet<Point> points;
    private final double targetPercent;
    private double[] estimatorParams;

    public AbandonCallSolver(QueueVariables variables, int nCallsInSystem, double targetPercent) {
        this.variables = variables;
        this.nCallsInSystem = nCallsInSystem;
        this.targetPercent = targetPercent;
        this.targetCoef = new double[]{targetPercent * nCallsInSystem, targetPercent};
        this.targetLine = new PolynomialFunction(targetCoef).negate();
        points = new TreeMap<>();
//        estimatorParams = ACEstimatorFunction.PARAMETRIC.createStart(0);
    }

    private int leftBound() {
        return Math.max(0, variables.nServers - nCallsInSystem);
    }

    /**
     * this is the callCount value at which:<br>
     * abandonCount(callCount) = 0 && abandonCount(callCount+1) > 0
     *
     * @return
     */
    private int zeroPoint() {
        return variables.nServers - nCallsInSystem;
    }

    /**
     * this is the number of responding calls before it exceeds the rate at
     * which the servers can service them, i.e. when the abandon count starts to
     * become significant since we only want to know the number of additional
     * calls that can be reasonably handled we subtract the calls already in the
     * system.
     *
     * @return
     */
    private double balancePoint() {
        return variables.nServers * variables.serviceRate / variables.responseRate - nCallsInSystem;
    }

//    abstract int lowerBounds();
//    public double evaluate(int x, double error) {
//
//    }
    private AbandonCountPoint generatePoint(int callCount, int targetSampleSize) {
//        System.out.println("generatePoint: " + callCount + ", " + targetSampleSize);
        AbandonCountPoint point = points.get(callCount);
        int existingSampleSize = 0;
        double mean = 0;
        double variance = 0;
        if (point != null) {
            existingSampleSize = point.getSampleSize();
            mean = point.getMean();
            variance = point.getVariance();
            if (targetSampleSize <= existingSampleSize) {
                return point;
            } else if (point.isExact()) {
                point = new AbandonCountPoint(callCount, mean, variance, targetSampleSize, true);
                points.put(callCount, point);
                return point;
            }
        }
        AbandonCallDistribution dist = new AbandonCallDistribution(variables, nCallsInSystem, callCount);

        int remainingWeight = targetSampleSize - existingSampleSize;

        long sampleExecCost = dist.sampleExecutionCost() * remainingWeight;
        long distributionMemoryCost = dist.distributionMemoryCost();
        long distributionExecCost = dist.distributionExecutionCost();

        boolean generateFromSamples = distributionMemoryCost > Runtime.getRuntime().freeMemory()
                || sampleExecCost < distributionExecCost;

        if (generateFromSamples) {
            int n = existingSampleSize;
            int[] samples = dist.sample(remainingWeight);
            for (int sample : samples) {
                int n0 = n;
                n++;
                double n1 = n;
                double dev = sample - mean;
                double nDev = dev / n1;
                mean += nDev;
                variance += n0 * dev * nDev;
            }
        } else {
            if (targetSampleSize < ACCURATE_SAMPLE_SIZE) {
                targetSampleSize = ACCURATE_SAMPLE_SIZE;
            }
            mean = dist.getNumericalMean();
            variance = dist.getNumericalVariance();
        }
        point = new AbandonCountPoint(callCount, mean, variance, targetSampleSize, !generateFromSamples);
        points.put(callCount, point);
        return point;
    }

    private int createZeroPoints() {
        AbandonCountPoint point;
        int callCount = 0;
        do {
            point = generatePoint(callCount, 1);
            callCount++;
        } while (point.isExact() && point.getMean() == 0);
//        for (int i = 0; i <= zeroPoint(); i++) {
//            generatePoint(i, 1);
//        }
        return callCount;
    }

    private double diff(AbandonCountPoint point) {
        return point.getY() + targetLine.value(point.getX());
    }

    public int solve() {

        StopWatch watch = new StopWatch("solve");
        try {
            watch.start("zeroPoints");
            AbandonCountPoint point;
            //first we create all the zero points of the function
            int index = 0;
            do {
                point = generatePoint(index, ACCURATE_SAMPLE_SIZE);
                index++;
            } while (point.getMean() == 0);
            if (diff(point) > 0) {
                //we just found the solution
                return index - 1;
            }
//            int index = Math.max(0, variables.nServers - nCallsInSystem);
//            point = generatePoint(index, ACCURATE_SAMPLE_SIZE);
//            if (diff(point) > 0) {
//                //we just found the solution
//                return index;
//            }
            watch.stop();
//            index++;

            int leftBounds = index;
            int rightBounds;
            watch.start("explore");
            //now we explore the graph until we find a point that exceeds the targetLine
            {
                int offset = 1;
                int sampleSize = 2;
                do {
                    point = generatePoint(index, sampleSize);
                    if (diff(point) > 0) {
                        if (point.getSampleSize() == ACCURATE_SAMPLE_SIZE) {
                            //we found our right end bounds
                            rightBounds = index;
                            break;
                        }
                        //we want to regenerate the point to make sure it is in fact
                        //larger than the target line
                        sampleSize = ACCURATE_SAMPLE_SIZE;
                        offset = 0;
                    } else {
                        if (point.getSampleSize() == ACCURATE_SAMPLE_SIZE) {
                            leftBounds = index;
                        }
                        sampleSize = 2;
                        if (point.getMean() == 0) {
                            offset = 1 + (int) (1.25 * offset);
                        } else {
                            offset = 1;
                        }
                    }
                    index += offset;
                } while (true);
            }
            watch.stop();
            watch.start("pinpoint");
            int next = rightBounds - 1;
            AbandonCountPoint point2;
            for (int i = 0; i < 20; i++) {
                point2 = point;
                point = generatePoint(next, ACCURATE_SAMPLE_SIZE);
                if (diff(point) < 0) {
                    System.out.println("found in " + i);
                    //we found our solution!
                    return next;
                }

                double x1 = point.getX();
                double y1 = point.getY();
                double x2 = point2.getX();
                double y2 = point2.getY();
                double c1 = (y1 - y2) / (x1 - x2);
                double c0 = y1 - c1 * x1;
                next = (int) ((targetCoef[0] - c0) / (c1 - targetCoef[1]));
            }
        } finally {
            watch.stop();
            System.out.println(watch.prettyPrint());
        }
        throw new TooManyIterationsException(20);
    }
    
    public static void main(String[] args) {
        QueueVariables variables = new QueueVariables(1.0 / 3, 1 / 3.3, 1 / 4.0, 250);
        AbandonCallSolver solver = new AbandonCallSolver(variables, 200, 0.03);
        System.out.println(solver.solve());
        for (Map.Entry<Integer, AbandonCountPoint> entrySet : solver.points.entrySet()) {
            Integer key = entrySet.getKey();
            AbandonCountPoint value = entrySet.getValue();
            System.out.println(value);
        }
//        solver.solve();
    }

}
