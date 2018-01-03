/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.dialplan.action;

import com.objectbrains.tms.enumerated.refrence.DDD;
import com.objectbrains.tms.enumerated.refrence.HOLDOrder;
import com.objectbrains.tms.enumerated.refrence.IVROrder;
import com.objectbrains.tms.enumerated.refrence.IVROrder2;
import com.objectbrains.tms.enumerated.refrence.IVROrder3;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;

/**
 *
 * @author hsleiman
 */
public class TMSOrder extends AbstractAction {

    public TMSOrder(int data) {
        super("export", FreeswitchVariables.tms_order_next + "=" + data);
    }

    public TMSOrder(String data) {
        super("export", FreeswitchVariables.tms_order_next + "=" + data);
    }

    public TMSOrder(DDD data) {
        super("export", FreeswitchVariables.tms_order_next + "=" + data.name());
    }

    public TMSOrder(IVROrder data) {
        super("export", FreeswitchVariables.tms_order_next + "=" + data.name());
    }

    public TMSOrder(IVROrder2 data) {
        super("export", FreeswitchVariables.tms_order_next + "=" + data.name());
    }

    public TMSOrder(IVROrder3 data) {
        super("export", FreeswitchVariables.tms_order_next + "=" + data.name());
    }

    public TMSOrder(HOLDOrder data) {
        super("export", FreeswitchVariables.tms_order_next + "=" + data.name());
    }

}
