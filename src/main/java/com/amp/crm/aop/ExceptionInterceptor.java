/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.aop;

import com.amp.crm.pojo.ExceptionToJson;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author raine.cabal, agoodman copied from Raine's interceptor in svc project
 */
@Component
@Aspect
public class ExceptionInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionInterceptor.class);

    @AfterThrowing(pointcut = "execution( * com.objectbrains.sti.iws.restful.rest..* (..))", throwing = "th")
    public ExceptionToJson afterThrowing(JoinPoint joinPoint, Throwable th) throws Throwable {
        for (int i = 0; i < 100; i++) {
            LOG.info("Exception thrown Hussien need to re-write. ");
        }
        
        LOG.info("joinPoint.getSignature().getName() -> {}",joinPoint.getSignature().getName());
        LOG.info("joinPoint.getSignature().toString() -> {}",joinPoint.getSignature().toString());
        ExceptionToJson exceptionToJson =  new ExceptionToJson();
        exceptionToJson.setLongDescription(th.toString());
        exceptionToJson.setShortDescription(th.getMessage());

        return exceptionToJson;
    }
}
