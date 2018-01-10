/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.exception;

public class UnauthenticatedException extends RuntimeException {

	/**
	 * used when user is not authenticated. Handled by RestExceptionResolver
	 */
	public UnauthenticatedException() {
	}

	/**
	 * 
	 * @param msg 
	 */
	public UnauthenticatedException(String msg) {
		super(msg);
	}
}

