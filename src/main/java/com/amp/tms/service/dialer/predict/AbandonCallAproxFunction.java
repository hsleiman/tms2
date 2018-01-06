/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer.predict;

import java.util.Arrays;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.FastMath;

/**
 * f(x) = (e^x)/(1+e^x).<br>
 * g(x) = a*(1+(x-2)*f(x))<br>
 * h(x) = g(bx+c)+d<br>
 * <br>
 * g'(x) = (a(x+e^x+1)+b)*(e^x)/(1+e^x)^2<br>
 * h'(x) = g'(bx+c)*b<br>
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public class AbandonCallAproxFunction implements UnivariateFunction {

    public static final Parametric PARAMETRIC = new Parametric();

    private final double[] parameters;

    public AbandonCallAproxFunction(double[] parameters) {
        validateParameters(parameters);
        this.parameters = parameters;
    }

    @Override
    public double value(double x) {
        return PARAMETRIC.value(x, parameters);
    }

    public PolynomialFunction getPositiveConvergenceFunction() {
        return new PolynomialFunction(getPositiveConvergenceCoefficients());
    }

    public double[] getPositiveConvergenceCoefficients() {
        double a = parameters[0];
        double b = parameters[1];
        double c = parameters[2];
        double d = parameters[3];
        double aa = a*a;
//        //line(x) = a*(1+(bx+c-2)) + d
//        // = a*(bx+c-1) + d
//        //=abx+ac-a+d
//        //=abx + a(c-1)+d
//        return new double[]{a * (c - 1) + d, a * b};

        return new double[]{aa * c + d, aa * b * b};
    }

    /**
     * Validates parameters to ensure they are appropriate for the evaluation of
     * the {@link #value(double,double[])} and
     * {@link #gradient(double,double[])} methods.
     *
     * @param param Values for lower and higher bounds.
     * @throws NullArgumentException if {@code param} is {@code null}.
     * @throws DimensionMismatchException if the size of {@code param} is not 2.
     */
    private static void validateParameters(double[] param)
            throws NullArgumentException,
            DimensionMismatchException {
        if (param == null) {
            throw new NullArgumentException();
        }
        if (param.length != 4) {
            throw new DimensionMismatchException(param.length, 4);
        }
    }

    public static class Parametric implements ParametricUnivariateFunction {

        public double[] createStart(double c0, double c1) {
            /**
             * 
             */
            //let asume d=0
            //c0 = aa*c
            //c1 = aa*bb
            //c1/bb = c0/c
            
            //0=f(x0) = c1*x+c0
            //x = -c0/c1
            //
            
//            double a = 1.0;
//            double b = ;
//            double c = -c0/c1;
//            double d = 0;
           
            return new double[]{1.0, FastMath.sqrt(c1), c0/c1, 0};
            
            /*
             this is the number of responding calls before it exceeds the 
             rate at which the servers can service them, i.e. when the abandon
             count starts to become significant
             since we only want to know the number of additional calls that can be reasonably handled
             we subtract the calls already in the system.
             */
//            double n = nServers * serviceRate / responseRate - nCallsInSystem;
//
//            double baseSlope = responseRate / serviceRate;
//
//            return new double[]{baseSlope, 1, -n, 0};
            
            
        }

        /**
         *
         * @param x value of x
         * @return log(1+e^(x))
         */
        private double log1pe(double x) {
            if (x > 0) {
                return x + FastMath.log1p(FastMath.exp(-x));
            } else {
                return FastMath.log1p(FastMath.exp(x));
            }
        }

        @Override
        public double value(double x, double... parameters) {
            if (x == Double.POSITIVE_INFINITY) {
                return Double.POSITIVE_INFINITY;
            }
            if (x == Double.NEGATIVE_INFINITY) {
                return 0;
            }

            double a = parameters[0];
            double b = parameters[1];
            double c = parameters[2];
            double d = parameters[3];

            //aa*log(1+e^(bbx+c))+d
            return a * a * log1pe(b * b * x + c) + d;
        }

        @Override
        public double[] gradient(double x, double... parameters) {

            double a = parameters[0];
            double b = parameters[1];
            double c = parameters[2];
            double d = parameters[3];

            double aa = a * a;

            if (x == Double.POSITIVE_INFINITY) {
                return new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, aa, 1.0};
            }
            if (x == Double.NEGATIVE_INFINITY) {
                return new double[]{0, 0, Double.NEGATIVE_INFINITY, 1.0};
            }

            double bbxc = b * b * x + c;

            //g = log(1+e^(bbx+c))
            double g = log1pe(bbxc);
            /*
             h = e^(bbx+c)/(e^(bbx+c)+1)
             log(h) = log(e^(bbx+c)) - log(e^(bbx+c)+1)
             log(h) = bbx+c - g
             h = e^(bbx+c - g)
             */
            double h = FastMath.exp(bbxc - g);

            /*
             d/da = 2a*log(e^(bbx+c)+1)
             d/da = 2a*g
             */
            double dda = 2 * a * g;

            /*
             d/dc = (aa*e^(bbx+c))/(e^(bbx+c)+1)
             d/dc = aa*h
             */
            double ddc = aa * h;

            /*
             d/db = (2aabx*e^(bbx+c))/(e^(bbx+c)+1)
             d/db = 2aabx*h
             d/db = 2*x*b*(d/dc)
             */
            double ddb = 2 * b * x * ddc;

            return new double[]{dda, ddb, ddc, 1.0};
        }

    }

}
