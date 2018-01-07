/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer.predict;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.util.MathArrays;

/**
 *
 * @author HS
 */
public class Utils {

//    public static IntegerDistribution createDroppedCallDistribution(QueueVariables variables, int nCallsInSystem, int nNewCalls) {
//        return new DroppedCallDistributionFactory(variables, nCallsInSystem, nNewCalls).build();
//    }

    public static class DroppedCallDistributionFactory {

        private final QueueVariables variables;
        private final int nCallsInSystem;
        private final int nNewCalls;

        public DroppedCallDistributionFactory(QueueVariables variables, int nCallsInSystem, int nNewCalls) {
            this.variables = variables;
            this.nCallsInSystem = nCallsInSystem;
            this.nNewCalls = nNewCalls;
        }

        public float getAccuracy() {
            return 0;
        }

        public long executionCost() {
            int totalCallCount = nCallsInSystem + nNewCalls;
            if (totalCallCount <= variables.nServers) {
                return 0;
            }
            long cost = 0;
            for (int i = nNewCalls; i >= 0; i--) {
                final int jMax = totalCallCount - i;
                cost += jMax * 27;
                final int kMax = Math.max(0, jMax - variables.nServers);
                cost += (jMax + kMax + 2) >> 1;
            }
            return cost;
        }

        public long memoryCost() {
            int totalCallCount = nCallsInSystem + nNewCalls;

            if (totalCallCount <= variables.nServers) {
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

        public double[] distribution() {
            int totalCallCount = nCallsInSystem + nNewCalls;

            if (totalCallCount <= variables.nServers) {
                return new double[]{1.0};
//                return new UniformIntegerDistribution(0, 0);
            }

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

            for (int i = nNewCalls; i >= 0; i--) {

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
                        if (i < nNewCalls) {
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
            double[] dist = new double[resultMatrix.length];
            
            for (int k = 0; k < resultMatrix.length; k++) {
                //get the last result of each matrix column
                dist[k] = resultMatrix[k][totalCallCount - k];
            }
            return dist;
//            return new EnumeratedIntegerDistribution(MathArrays.natural(resultMatrix.length), dist);
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
    }

}
