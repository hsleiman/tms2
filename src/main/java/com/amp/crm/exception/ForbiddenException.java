/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.exception;

/**
 *
 * @author hsleiman
 */
public class ForbiddenException extends RuntimeException {

	/**
	 * Creates a new instance of <code>RuntimeException</code> without detail message.
	 */
	public ForbiddenException() {
	}

	/**
	 * Constructs an instance of <code>RuntimeException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public ForbiddenException(String msg) {
		super(msg);
	}
}
