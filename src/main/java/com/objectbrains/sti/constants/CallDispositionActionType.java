/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.objectbrains.sti.constants;


public enum CallDispositionActionType {
    
    RETRY_CALL,
    DO_NOT_CALL,
    DO_NOT_CALL_NUMBER,
    MARK_ACCOUNT_AS_COMPLETED,
    TRY_NEXT_PHONE_NUMBER;
    
    public static final String RETRY_CALL_ACTION = "RETRY_CALL";
    public static final String DO_NOT_CALL_ACTION = "DO_NOT_CALL";
    public static final String DO_NOT_CALL_NUMBER_ACTION = "DO_NOT_CALL_NUMBER";
    public static final String MARK_ACCOUNT_AS_COMPLETED_ACTION = "MARK_ACCOUNT_AS_COMPLETED";
    public static final String TRY_NEXT_PHONE_NUMBER_ACTION = "TRY_NEXT_PHONE_NUMBER";
   
}
