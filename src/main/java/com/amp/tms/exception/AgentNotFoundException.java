/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.exception;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class AgentNotFoundException extends Exception{

    public AgentNotFoundException(String agentName) {
         super("Unable to find Agent with name [" + agentName + "]");
    }
    
}
