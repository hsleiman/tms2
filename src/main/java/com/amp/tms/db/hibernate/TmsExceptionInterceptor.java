/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.hibernate;

import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine.cabal
 */
@Component
@Aspect
public class TmsExceptionInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(TmsExceptionInterceptor.class);

    @AfterThrowing(pointcut = "within(@javax.ws.rs.Path *)", throwing = "th")
    public void afterThrowing(JoinPoint joinPoint, Throwable th) throws Throwable {
        Signature signature = joinPoint.getSignature();
        String methodName = signature.getName();
        String path = signature.toString();
        String arguments = Arrays.toString(joinPoint.getArgs());
//        WebFaultBean faultBean = null;
        LOG.error("Error executing service "
                + methodName + "[" + path + "]" + " with arguments "
                + arguments + ": "
                + th.toString(), th);
//        if (th instanceof SvcException) {
//            SvcException ex = (SvcException) th;
//            if (ex.getFaultInfo() == null) {
//                faultBean = new WebFaultBean();
//                faultBean.setFaultString(ExceptionUtils.getStackTrace(th));
//                ex.setFaultInfo(faultBean);
//            }
//        }

//        SOAPFault sf = SOAPFactory.newInstance().createFault();
//        sf.setFaultString(th.getMessage());
//        throw new SOAPFaultException(sf);
        //throw new SvcException(th, faultBean);
        throw th;
    }

}

