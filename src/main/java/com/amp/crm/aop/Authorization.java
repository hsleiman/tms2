package com.amp.crm.aop;

import com.amp.crm.constants.Permission;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Authorization {
	//Permission:  
	//	< 0:  No authentication needed => wide open
	//	  0:  Must be authenticated
	//	> 0:  Must have permission
	   Permission permission();	
	
	String noPermissionTo() ;//default "";
}