/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.exception;

import javax.xml.ws.WebFault;

/**
 *
 * @author David
 */
@WebFault(targetNamespace = "http://exception.sti.objectbrains.com")
public class WorkQueueNotFoundException extends Exception{
    
    private long faultInfo;
    /**
     * Creates a new instance of <code>BorrowerNotFoundException2</code> without
     * detail message.
     */
    public WorkQueueNotFoundException(long faultInfo) {
        super("WorkQueue with pk [" + faultInfo + "] could not be found");
        this.faultInfo = faultInfo;
    }

    /**
     * Constructs an instance of <code>BorrowerNotFoundException2</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public WorkQueueNotFoundException(String msg, long faultInfo) {
        super(msg);
        this.faultInfo = faultInfo;
    }

}