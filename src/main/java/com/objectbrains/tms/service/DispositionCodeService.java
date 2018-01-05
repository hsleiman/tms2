/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.objectbrains.scheduler.annotation.Sync;
import com.objectbrains.sti.db.entity.disposition.CallDispositionCode;
import com.objectbrains.sti.service.tms.CallDispositionService;
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
    private CallDispositionService callDispositionService;
    
    

    public CallDispositionCode answeringMachineCode() {
        return callDispositionService.getCallDispositionCode(7);
    }
    
    public CallDispositionCode answeringMachineDialerLeftMessageCode() {
        return callDispositionService.getCallDispositionCode(168);
    }
    
    public CallDispositionCode callDroppedFromDialer() {
        return callDispositionService.getCallDispositionCode(6);
    }
    
    public CallDispositionCode callNoAnswer(){
        return callDispositionService.getCallDispositionCode(50);
    }

    public CallDispositionCode getDispositionCodeForQCode(int qCode) {
        return callDispositionService.getCallDispositionCodeByQCode(qCode);
    }

    public CallDispositionCode noResponseCode() {
        return callDispositionService.getCallDispositionCodeByQCode(18);//use the qcode for NO_USER_RESPONSE
    }

    public CallDispositionCode recordRestrictedCode() {
        return callDispositionService.getCallDispositionCodeByDispositionName("RECORD RESTRICTED"); 
    }
    
    public CallDispositionCode callerUnknownCode() {
        return callDispositionService.getCallDispositionCodeByDispositionName("CALLER UNKNOWN");
    }
    
    public CallDispositionCode noWrapCode(){
        return callDispositionService.getCallDispositionCodeByDispositionName("NO WRAP");
    }

    public CallDispositionCode getDispositionCodeFromId(long id) {
        return callDispositionService.getCallDispositionCode(id);
    }

}
