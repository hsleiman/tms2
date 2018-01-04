/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.objectbrains.scheduler.annotation.Sync;
import com.objectbrains.sti.db.entity.disposition.CallDispositionCode;
import com.objectbrains.sti.service.tms.TMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
@Sync
public class DispositionCodeService {

    @Autowired
    private TMSService tmsIWS;
    
    

    public CallDispositionCode answeringMachineCode() {
        return tmsIWS.getCallDispositionCode(7);
    }
    
    public CallDispositionCode answeringMachineDialerLeftMessageCode() {
        return tmsIWS.getCallDispositionCode(168);
    }
    
    public CallDispositionCode callDroppedFromDialer() {
        return tmsIWS.getCallDispositionCode(6);
    }
    
    public CallDispositionCode callNoAnswer(){
        return tmsIWS.getCallDispositionCode(50);
    }

    public CallDispositionCode getDispositionCodeForQCode(int qCode) {
        return tmsIWS.getCallDispositionCodeByQCode(qCode);
    }

    public CallDispositionCode noResponseCode() {
        return tmsIWS.getCallDispositionCodeByQCode(18);//use the qcode for NO_USER_RESPONSE
    }

    public CallDispositionCode recordRestrictedCode() {
        return tmsIWS.getCallDispositionCodeByDispositionName("RECORD RESTRICTED"); 
    }
    
    public CallDispositionCode callerUnknownCode() {
        return tmsIWS.getCallDispositionCodeByDispositionName("CALLER UNKNOWN");
    }
    
    public CallDispositionCode noWrapCode(){
        return tmsIWS.getCallDispositionCodeByDispositionName("NO WRAP");
    }

    public CallDispositionCode getDispositionCodeFromId(long id) {
        return tmsIWS.getCallDispositionCode(id);
    }

}
