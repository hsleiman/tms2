/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.dialplan.action;

/**
 *
 * 
 */
public class Log extends AbstractAction{

    public Log(String data) {
        super("log", data);
    }
}
