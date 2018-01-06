/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.incoming;

import com.amp.crm.constants.CallerId;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.refrence.IVROrder;
import com.amp.tms.freeswitch.dialplan.action.Answer;
import com.amp.tms.freeswitch.dialplan.action.Hangup;
import com.amp.tms.freeswitch.dialplan.action.Sleep;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;

/**
 *
 * @author hsleiman
 */
public class HangupSimply extends DialplanBuilder {

    public HangupSimply(DialplanVariable variable) {
        setVariable(variable);
        this.setDebugOn(Boolean.TRUE);
    }

    @Override
    public void createDialplans() {
//        agentDialplan = dialplanRepository.createTMSDialplan(TMS_UUID, FreeswitchContext.AGENT_DIALPLAN);
//        agentDialplan.setTms_type(this.getClass().getSimpleName());

    }

    @Override
    public void buildDialplans() {
        SimplyHangup();

    }

    @Override
    public void saveDialplans() {
    }

    private void commonVariable(TMSDialplan tMSDialplan) {
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setDebugOn(getDebugOn());
        tMSDialplan.setTms_type(this.getClass().getSimpleName());

        tMSDialplan.setCallerId(CallerId.ACTUAL);
        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());

        if (inVariables.getCallDirection() == null) {
            if (inVariables.getCallerIdNumber().length() == inVariables.getCalleeIdNumber().length()) {
                tMSDialplan.setCallDirection(CallDirection.INTERNAL);
            } else if (inVariables.getCallerIdNumber().length() < inVariables.getCalleeIdNumber().length()) {
                tMSDialplan.setCallDirection(CallDirection.OUTBOUND);
            } else if (inVariables.getCallerIdNumber().length() > inVariables.getCalleeIdNumber().length()) {
                tMSDialplan.setCallDirection(CallDirection.INBOUND);
            } else {
                tMSDialplan.setCallDirection(CallDirection.INTERNAL);
            }
        } else {
            tMSDialplan.setCallDirection(inVariables.getCallDirection());
        }
        tMSDialplan.setDialer(inVariables.getDialer());
        tMSDialplan.setCallee(inVariables.getCalleeIdNumber());
        tMSDialplan.setCaller(inVariables.getCallerIdNumber());

    }

    private void SimplyHangup() {
        TMSDialplan fifoDialplan;
        fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, inVariables.getContext(), IVROrder.HANGUP_CALL);
        commonVariable(fifoDialplan);
        fifoDialplan.addAction(new Answer());
        fifoDialplan.addAction(new Sleep(1l));
        fifoDialplan.addBridge(new Hangup("NORMAL_CLEARING"));
        fifoDialplan.setXMLFromDialplan();
        setReturnDialplan(fifoDialplan);
        dialplanService.updateTMSDialplan(fifoDialplan);
    }

}
