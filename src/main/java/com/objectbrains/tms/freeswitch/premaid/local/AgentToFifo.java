/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.local;

import com.objectbrains.svc.iws.PopupDisplayMode;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.RecordedPhrases;
import com.objectbrains.tms.enumerated.refrence.BeanServices;
import com.objectbrains.tms.enumerated.refrence.HOLDOrder;
import com.objectbrains.tms.freeswitch.dialplan.action.Answer;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToFifo;
import com.objectbrains.tms.freeswitch.dialplan.action.Fifo;
import com.objectbrains.tms.freeswitch.dialplan.action.Playback;
import com.objectbrains.tms.freeswitch.dialplan.action.Set;
import com.objectbrains.tms.freeswitch.dialplan.action.TMSOrder;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;
import com.objectbrains.tms.pojo.BorrowerInfo;
import com.objectbrains.tms.service.freeswitch.FifoService;

/**
 *
 * @author hsleiman
 */
public class AgentToFifo extends DialplanBuilder {

    public AgentToFifo(DialplanVariable variable) {
        super();
        log.info("AgentToAgent");
        setVariable(variable);
        
    }

    @Override
    public void createDialplans() {
    }

    @Override
    public void buildDialplans() {
        buildDialplanFifo();
        buildDialplanAgent();

    }
    
    private void buildDialplanAgent(){
        TMSDialplan agentDialplan;
        agentDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.agent_dp, HOLDOrder.PLACE_ON_HOLD_TRANSFER);
        commonVariable(agentDialplan);
        
        agentDialplan.setConditionDefault(Boolean.FALSE);
        agentDialplan.setConditionField("destination_number");
        agentDialplan.setConditionExpression("^(\\d+_\\d+)$");

        agentDialplan.addAction(new Answer());
        String groupTransfer = inVariables.getCallerDestinationNumber();
        groupTransfer = groupTransfer.replace("1010_", "");
        Long groupPK = Long.parseLong(groupTransfer);
        agentDialplan.setGroupPkForTransfer(groupPK);
        agentDialplan.setQueuePkForTransfer(groupPK);
        
        agentDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_ON_HOLD_TRANSFER));
        agentDialplan.addBridge(new BridgeToFifo(FreeswitchContext.agent_dp, freeswitchService.getFreeswitchIPNew(TMS_UUID, FreeswitchContext.fifo_dp)));
        agentDialplan.setUniqueID(inVariables.getUniqueID());
        agentDialplan.setChannelCallUUID(inVariables.getChannelCallUUID());
        
        log.info("Saving " + agentDialplan.getTms_type() + " Dialplan...");

        agentDialplan.setXMLFromDialplan();
        setReturnDialplan(agentDialplan);
        dialplanService.updateTMSDialplan(agentDialplan);
    }
    
    
    private  void buildDialplanFifo() {
        
        TMSDialplan fifoDialplan;
        fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_ON_HOLD_TRANSFER);
        commonVariable(fifoDialplan);
        
        fifoDialplan.addAction(new Set("fifo_music", configuration.getFiFoHoldMusic()));

        fifoDialplan.addAction(new Answer());
        fifoDialplan.setBean(BeanServices.FifoService);
        fifoDialplan.setFunctionCall(FifoService.placeCallOnHoldForTransferCall);
        
        String groupTransfer = inVariables.getCallerDestinationNumber();
        groupTransfer = groupTransfer.replace("1010_", "");
        Long groupPK = Long.parseLong(groupTransfer);
        fifoDialplan.setGroupPkForTransfer(groupPK);
        fifoDialplan.setQueuePkForTransfer(groupPK);
        
        

        fifoDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_IN_VOICEMAIL));
        fifoDialplan.addAction(new Set("fifo_orbit_exten=1000:" + configuration.getMaxCallOnHoldAllowed()));
        fifoDialplan.addAction(new Set("fifo_chime_list=" + RecordedPhrases.HOLD_ANNOUNCE_PRESS1.getAudioPath()));
        fifoDialplan.addAction(new Set("fifo_chime_freq=" + configuration.getMaxHoldAnnounceTimeInSec()));
        fifoDialplan.addAction(new Set("fifo_caller_exit_key=1"));
        fifoDialplan.addAction(new Set("fifo_caller_exit_to_orbit=true"));

        fifoDialplan.addAction(new Playback(RecordedPhrases.PLEASE_WAIT_FOR_NEXT_AGENT));

        fifoDialplan.setDialerQueueId(new Long(groupPK));
        fifoDialplan.addBridge(new Fifo("InboundDialerQueue_" + fifoDialplan.getDialerQueueId() + " in"));

        log.info("Saving " + fifoDialplan.getTms_type() + " Dialplan...");

        fifoDialplan.setXMLFromDialplan();
        
        dialplanService.updateTMSDialplan(fifoDialplan);

    }

    public void commonVariable(TMSDialplan tMSDialplan) {
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());
        tMSDialplan.setCallDirection(inVariables.getCallDirection());
        tMSDialplan.setAutoAswer(inVariables.getAutoAnswer());
        if (inVariables.getPopupType() != null) {
            tMSDialplan.setPopupType(PopupDisplayMode.fromValue(inVariables.getPopupType()));
        }

        tMSDialplan.setCaller(inVariables.getCallerIdNumber());
        tMSDialplan.setCallee(inVariables.getCalleeIdNumber());
        tMSDialplan.setOutboundVendor(Boolean.FALSE);
        tMSDialplan.setVariables(inVariables.toJson());
        tMSDialplan.setDialer(Boolean.FALSE);
        
        BorrowerInfo borrowerInfo = new BorrowerInfo();
        borrowerInfo.setLoanId(inVariables.getLoanId());
        borrowerInfo.setBorrowerFirstName(inVariables.getBorrowerFirstName());
        borrowerInfo.setBorrowerLastName(inVariables.getBorrowerLastName());
        
        
        if(inVariables.getCallDirection() == CallDirection.OUTBOUND){
            borrowerInfo.setBorrowerPhoneNumber(inVariables.getBorrower_phone());
            tMSDialplan.setOriginalTransferFromExt(inVariables.getCallerAniInteger());
        }
        else if(inVariables.getCallDirection() == CallDirection.INBOUND){
             borrowerInfo.setBorrowerPhoneNumber(inVariables.getBorrower_phone());
             tMSDialplan.setOriginalTransferFromExt(inVariables.getRdnisInteger());
        }
        tMSDialplan.setBorrowerInfo(borrowerInfo);
        
       
        
    }


    @Override
    public void saveDialplans() {

    }

}
