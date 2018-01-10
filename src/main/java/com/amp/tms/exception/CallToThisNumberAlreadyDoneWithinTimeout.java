/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.exception;

/**
 *
 * 
 */
public class CallToThisNumberAlreadyDoneWithinTimeout extends Exception{

    public CallToThisNumberAlreadyDoneWithinTimeout(String message) {
        super("Unable to find Agent with name [" + message + "]");
    }
    
}
