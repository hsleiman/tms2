/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.enumerated.refrence;

import com.objectbrains.tms.freeswitch.premaid.incoming.service.IVRCallerIdentity;
import com.objectbrains.tms.freeswitch.premaid.incoming.service.IVRCallerIdentity2;
import com.objectbrains.tms.freeswitch.premaid.incoming.service.IVRMain;
import com.objectbrains.tms.freeswitch.premaid.incoming.service.IVRMain2;
import com.objectbrains.tms.freeswitch.premaid.incoming.service.IVRMain2AfterHour;
import com.objectbrains.tms.freeswitch.premaid.outbound.service.DDDialplan;
import com.objectbrains.tms.service.freeswitch.FifoService;
import com.objectbrains.tms.service.freeswitch.FsAgentService;

/**
 *
 * @author hsleiman
 */
public enum BeanServices {
    IVRCallerIdentity(IVRCallerIdentity.class.getSimpleName()),
    IVRCallerIdentity2(IVRCallerIdentity2.class.getSimpleName()),
    FifoService(FifoService.class.getSimpleName()),
    IVRMain(IVRMain.class.getSimpleName()),
    IVRMain2(IVRMain2.class.getSimpleName()),
    IVRMain2AfterHour(IVRMain2AfterHour.class.getSimpleName()),
    FsAgentService(FsAgentService.class.getSimpleName()),
    DDDialplan(DDDialplan.class.getSimpleName());
    
    
    private final String method;

    private BeanServices(String method) {
        this.method = method;
    }

    public String getBeanName() {
        return method;
    }
}
