/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.objectbrains.sti.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 *
 * @author vnguyen
 */
@Component
@Aspect
public class Tracing {
    //syntax <method scope (public, private)> <method's return type> <class name >.*(parameters)
    @Pointcut("execution( * com.objectbrains.loan..* (..))")
    private void selectAll(){}
    
    // Advice is applied before traget method is execute
	@Before ( "execution( * com.objectbrains.loan..* (..))")
  public void doTaskBefore(JoinPoint jointPoint)
   {  
      //System.out.println( "AOP Entering: " + jointPoint.getStaticPart().getSignature().toString() );
   }
   
  
   // Advice is applied after traget method is executed
   @After ( "selectAll()")
   public void doTaskAfter( JoinPoint jointPoint )
   {  
      //System.out.println( "AOP Exiting: " + jointPoint.getStaticPart().getSignature().toString() );
     
   }
   //Advice after target method returns successfully
   @AfterReturning ( pointcut="selectAll()", returning="retVal")
   public void doTaskAfterReturning(Object retVal)
   {  
       //System.out.println("AOP retVal: " + retVal);
     
   }
   //If method matching pointcut throws exception
   @AfterThrowing ( pointcut="selectAll()", throwing="ex")
   public void doTaskAfterThrowingTask( JoinPoint jointPoint, RuntimeException ex)
   {  
      // StringBuilder arguments = generateArgumentsString(jointPoint.getArgs());
       //System.out.println("AOP Error: " + jointPoint.getSignature() + "; Exception:" +  ex.toString());
     
   }
  
   // to add advice before and after a target method. Resource cosuming . Shd be used sparingly
//   @Around ( "selectAll()")
//   public void doAroundTask( ProceedingJoinPoint point)
//   {  
//       System.out.println("around logging!" );
//       //point.proceed();
//     
//   }
}
