/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.incoming;

import com.objectbrains.sti.constants.CallerId;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.freeswitch.dialplan.action.Answer;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToFifo;
import com.objectbrains.tms.freeswitch.dialplan.action.Fifo;
import com.objectbrains.tms.freeswitch.dialplan.action.Set;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;

/**
 *
 * @author hsleiman
 */
public class IncomingBlackhole extends DialplanBuilder {
    
  
    public IncomingBlackhole(DialplanVariable variable) {
        setVariable(variable);
    }

    @Override
    public void createDialplans() {
//        agentDialplan = dialplanRepository.createTMSDialplan(TMS_UUID, FreeswitchContext.AGENT_DIALPLAN);
//        agentDialplan.setTms_type(this.getClass().getSimpleName());

    }

    @Override
    public void buildDialplans() {
        callEnteringSBC();
        BlackholeFifo();

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
        tMSDialplan.setCallDirection(CallDirection.INBOUND);
        tMSDialplan.setDialer(Boolean.FALSE);
        tMSDialplan.setCallee(inVariables.getCalleeIdNumber());
        tMSDialplan.setCaller(inVariables.getCallerIdNumber());

    }

    private void callEnteringSBC() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.sbc_dp);
        commonVariable(tmsDialplan);
        tmsDialplan.setRecord(Boolean.TRUE);
        tmsDialplan.setOutboundVendor(Boolean.FALSE);
        tmsDialplan.setVariables(inVariables.toJson());
        tmsDialplan.setDialer(Boolean.FALSE);
        tmsDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
        tmsDialplan.addBridge(new BridgeToFifo(FreeswitchContext.sbc_dp, freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
        setReturnDialplan(tmsDialplan);
    }

    private void BlackholeFifo() {
        TMSDialplan fifoDialplan;
        fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp);
        commonVariable(fifoDialplan);
        fifoDialplan.addAction(new Set("fifo_music", configuration.getFiFoHoldMusic()));
        fifoDialplan.addAction(new Answer());
        fifoDialplan.addBridge(new Fifo("InboundDialerQueue_black_hole" + " in"));
        fifoDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(fifoDialplan);
    }

}
