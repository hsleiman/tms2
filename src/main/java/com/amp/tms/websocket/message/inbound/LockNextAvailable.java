/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message.inbound;

import com.amp.crm.constants.CallRoutingOption;
import java.io.Serializable;

/**
 * {function:"LOCK_NEXT_AVAILABLE" , lockNextAvailable:{callUUID:"blah",groupPk:45}}
 * @author HS
 */
public class LockNextAvailable implements Serializable {

    private String callUUID;
    private Long groupPk;
    private Long queuePk;
    private String lockedToExt;
    private CallRoutingOption routingOrder;

    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }

    public Long getGroupPk() {
        return groupPk;
    }

    public void setGroupPk(Long groupPk) {
        this.groupPk = groupPk;
    }

    public Long getQueuePk() {
        return queuePk;
    }

    public void setQueuePk(Long queuePk) {
        this.queuePk = queuePk;
    }

    public CallRoutingOption getRoutingOrder() {
        return routingOrder;
    }

    public void setRoutingOrder(CallRoutingOption routingOrder) {
        this.routingOrder = routingOrder;
    }    

    public String getLockedToExt() {
        return lockedToExt;
    }

    public void setLockedToExt(String lockedToExt) {
        this.lockedToExt = lockedToExt;
    }
    

}
