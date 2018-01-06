/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.exception;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public class ReportNotFoundException extends Exception{

    public ReportNotFoundException(Long pk) {
        super("Report with pk="+pk+" could not be found");
    }
    
}
