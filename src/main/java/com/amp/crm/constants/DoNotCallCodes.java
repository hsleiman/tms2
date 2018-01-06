/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.constants;

/**
 *
 * @author David
 */
public class DoNotCallCodes {
   
    public static final int OKAY_TO_CALL = 0;
    public static final int IS_DO_NOT_CALL_TRUE = 1;
    public static final int NO_DATABASE_MATCH = 2;
    public static final int TOO_EARLY_TO_CALL = 3;
    public static final int TOO_LATE_TO_CALL = 4;
    public static final int ACCOUNT_DO_NOT_CALL_TRUE = 5;
    
    public static String getCodeDesciption(Integer doNotCallCode){
        String code;
        if(doNotCallCode == null){
            return "NULL";
        }
        switch(doNotCallCode){
            case 0:
                code = "OKAY_TO_CALL";
                break;
            case 1:
                code = "IS_DO_NOT_CALL_TRUE";
                break;
            case 2:
                code = "NO_DATABASE_MATCH";
                break;
            case 3:
                code = "TOO_EARLY_TO_CALL";
                break;
            case 4:
                code = "TOO_LATE_TO_CALL";
                break;
            case 5:
                code = "ACCOUNT_DO_NOT_CALL_TRUE";
                break;
            default:
                code = "UNKNOWN";
                break;
        }
        return code;
    }
}
