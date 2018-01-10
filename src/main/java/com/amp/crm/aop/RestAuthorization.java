/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.aop;

import com.amp.crm.constants.Permission;
import com.amp.crm.exception.ForbiddenException;
import com.amp.crm.exception.UnauthenticatedException;
import com.amp.crm.service.auth.UserAuth;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
public class RestAuthorization { //implements ApplicationContextAware {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RestAuthorization.class);

    final int PERMISSION_AUTHENTICIATED = 0;
    @Autowired
    private UserAuth userAuth;

    //syntax <method scope (public, private)> <method's return type> <class name >.*(parameters)
    @Pointcut("execution( * com.amp.crm.iws.restful.rest..* (..))")
    private void restAPI() {
    }

    // to add advice before and after a target method. Resource cosuming . Shd be used sparingly
    @Around("restAPI() && @annotation(requestMapping)")
    public Object doAroundTask(ProceedingJoinPoint procJoinPoint, RequestMapping requestMapping) throws UnauthenticatedException, Throwable {

        //Get the permision required for the REST method
        MethodSignature signature = (MethodSignature) procJoinPoint.getSignature();
        Method method = signature.getMethod();

        int permissionId;
        Permission permission;
        String unauthorizedMessage;

        Authorization authAnnotation = method.getAnnotation(Authorization.class);
        if (authAnnotation != null) {
            permission = authAnnotation.permission();
            permissionId = permission.getId();
            unauthorizedMessage = authAnnotation.noPermissionTo();
        } else {
            //Error ==>  All RESTful method must have @Authorization;
            throw new RuntimeException(String.format("The '%s' RESTfull method must be annotated with @Authorization.",
                    procJoinPoint.getStaticPart().getSignature().toString()));

        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

		//Check to see if user can execute the method
        //Permission:  
        //	< 0:  No authentication needed => wide open
        //	  0:  Must be authenticated
        //	> 0:  Must have permission
        if (permissionId >= PERMISSION_AUTHENTICIATED) {
            //Check to see if user is logged in
            String username = request.getHeader("username");
            String token = request.getHeader("tokenkey");
            LOG.info("username {} - token {}", username, token);

            if (username == null || token == null) {
                throw new UnauthenticatedException("Not Authenticated.  Please login.");
            }
//            
//            if (userAuth.isValid(username, token) == false) {
//				//Please see com.objectbrains.svcfe.auth.RestExceptionResolver class.  
//                //It handles UnauthenticatedException exception.  
//                throw new UnauthenticatedException("Not Authenticated.  Please login.");
//            }

            if (permissionId > PERMISSION_AUTHENTICIATED && !userAuth.hasPermission(username, permissionId)) {
				//Please see com.objectbrains.svcfe.auth.RestExceptionResolver class.  
                //It handles ForbiddenException exception.  
                if (unauthorizedMessage == null || unauthorizedMessage.trim().length() == 0) {
                    unauthorizedMessage = "You do not have permission to access this resource.";
                } else {
                    unauthorizedMessage = "You do not have permission to " + unauthorizedMessage.trim();
                }
                if (unauthorizedMessage.endsWith(".")) {
                    unauthorizedMessage = unauthorizedMessage.substring(0, unauthorizedMessage.length() - 1);
                }

                unauthorizedMessage += " (" + permission + ").";
                throw new ForbiddenException(unauthorizedMessage);
            }
        }

        Object retObj = null;
        try {
            retObj = procJoinPoint.proceed();

        } catch (Throwable ex) {
            throw ex;
        } finally {
			//System.out.println(procJoinPoint.getStaticPart().getSignature().toString() + " ==> 2 request.getRequestURL():  " + request.getRequestURL() );

            //System.out.println("AOP Around RestAuthorization Ending: " + procJoinPoint.getStaticPart().getSignature().toString());
        }

        return retObj;
    }

}
