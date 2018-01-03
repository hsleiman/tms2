/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.exception;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public class CallNotFoundException extends Exception{

    public CallNotFoundException(String message) {
        super(message);
    }
    
}
