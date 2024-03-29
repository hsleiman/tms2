/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.outbound;

import com.amp.crm.constants.CallerId;
import com.amp.crm.exception.CrmException;
import com.amp.crm.pojo.TMSCallDetails;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.RefreshSVCEnum;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.Bridge;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.pojo.BorrowerInfo;
import com.amp.tms.websocket.message.Function;
import com.amp.tms.websocket.message.outbound.CallSipHeader;
import com.amp.tms.websocket.message.outbound.RefreshSVC;
import com.amp.tms.websocket.message.outbound.Send;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * 
 */
public class ManualDial extends DialplanBuilder {

    public ManualDial(DialplanVariable variable) {
        super();
        log.info("ManualDial");
        setVariable(variable);

    }

    @Override
    public void createDialplans() {
        log.info("Create Dialplans");
        
    }

    @Override
    public void buildDialplans() {
        log.info("Build Dialplans");

        AgentTMS agent = agenService.getAgent(inVariables.getCallerIdInteger());

        TMSCallDetails callDetails = null;

        if (inVariables.getLoanId() != null && inVariables.getLoanId() != 0l) {
            try {
                log.info("GetLoanInfoByLoanPk CALLED: ");
                Long startTime = System.currentTimeMillis();
                callDetails = tmsIWS.getLoanInfoByLoanPk(inVariables.getLoanId());
                log.info("GetLoanInfoByLoanPk CALLED: " + (System.currentTimeMillis() - startTime));
            } catch (CrmException ex) {
                Logger.getLogger(ManualDial.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                log.info("GetLoanInfoByPhoneNumber CALLED: ");
                Long startTime = System.currentTimeMillis();
                callDetails = tmsIWS.getLoanInfoByPhoneNumber(inVariables.getCalleeIdLong());
                log.info("GetLoanInfoByPhoneNumber CALLED: " + (System.currentTimeMillis() - startTime));
            } catch (CrmException ex) {
                Logger.getLogger(ManualDial.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        callEnteringSBC(callDetails, agent);
        callEnteringAgent(callDetails, agent);

//        if (agent != null) {
//            agenService.saveAgent(agent);
//        }
    }

    @Override
    public void saveDialplans() {

    }

    public void callEnteringAgent(TMSCallDetails callDetails, AgentTMS agent) {
        TMSDialplan agentDialplan;
        agentDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.agent_dp);
        commonVariable(agentDialplan, callDetails, agent);

        agentDialplan.setRecord(Boolean.FALSE);
        agentDialplan.setAutoAswer(Boolean.FALSE);
        agentDialplan.setOutboundVendor(Boolean.FALSE);
        BorrowerInfo borrowerInfo = new BorrowerInfo();

        if (callDetails != null) {
            borrowerInfo.setBorrowerFirstName(callDetails.getFirstName());
            borrowerInfo.setBorrowerLastName(callDetails.getLastName());
            borrowerInfo.setBorrowerPhoneNumber(inVariables.getCalleeIdNumber());
            borrowerInfo.setLoanId(callDetails.getAccountPk());
//            if (callDetails.getAccountPk() == null) {
//                agentDialplan.setIgnore_disposition(Boolean.TRUE);
//            }

            agentDialplan.getBorrowerInfo().setBorrowerFirstName(callDetails.getFirstName());
            agentDialplan.getBorrowerInfo().setBorrowerLastName(callDetails.getLastName());
            agentDialplan.getBorrowerInfo().setLoanId(callDetails.getAccountPk());

        }

        if ((callDetails != null && callDetails.isDoNotCall())) {
            agentDialplan.setDnc(Boolean.TRUE);
//            premaidActions.getDNC(agentDialplan);
//            agentDialplan.setXMLFromDialplan();
//            setReturnDialplan(agentDialplan);
//            dialplanService.updateTMSDialplan(agentDialplan);
//            return;
        } else if (dnc.isInDNC(inVariables.getCalleeIdNumber())) {
            agentDialplan.setDnc(Boolean.TRUE);
            premaidActions.getDNC(agentDialplan);
            agentDialplan.setXMLFromDialplan();
            setReturnDialplan(agentDialplan);
            dialplanService.updateTMSDialplan(agentDialplan);
            return;
        } else {
            agentDialplan.setDnc(Boolean.FALSE);
        }
        agentDialplan.addAction(new Set(FreeswitchVariables.ringback, "${us-ring}"));
        agentDialplan.addBridge(new Bridge("sofia/agent/sip:$1@" + freeswitchService.getFreeswitchIPNew(agentDialplan.getCall_uuid(), FreeswitchContext.sbc_dp) + ":" + FreeswitchContext.sbc_dp.getPort() + ";transport=tcp"));

//        if (agent != null) {
//            agenService.setAgent(agent.getExtension(), agent);
//        }
        log.info("Saving " + agentDialplan.getTms_type() + " Dialplan...");
        agentDialplan.setXMLFromDialplan();

        setReturnDialplan(agentDialplan);
        dialplanService.updateTMSDialplan(agentDialplan);

        Send send = new Send(Function.CallUUID);
        CallSipHeader callSipHeader = new CallSipHeader();
        callSipHeader.setBorrowerInfo(borrowerInfo);
        callSipHeader.setCallDirection(CallDirection.OUTBOUND);
        callSipHeader.setCall_uuid(agentDialplan.getCall_uuid());
        callSipHeader.setIgnore_disposition(agentDialplan.getIgnore_disposition());
        send.setCallSipHeader(callSipHeader);
        websocket.sendWithRetry(inVariables.getCallerIdInteger(), send);

        if (callDetails.getAccountPk() != null) {
            if (callDetails.getCallerId() != CallerId.ACTUAL) {
//                try {
//                    tmsIWS.resetCallerIdStatusForLoan(callDetails.getAccountPk());
//                } catch (Exception ex) {
//                    log.info(ex.getMessage(), ex);
//                }
                Send sendR = new Send(Function.Refresh_SVC);
                RefreshSVC refreshSVC = new RefreshSVC();
                refreshSVC.setKey(RefreshSVCEnum.UNKNOWN_CALLER_ID);
                sendR.setRefreshSVC(refreshSVC);
                websocket.sendWithRetry(inVariables.getCallerIdInteger(), send);
            }
        }
    }

    public void callEnteringSBC(TMSCallDetails callDetails, AgentTMS agent) {
        TMSDialplan sbcDialplan;
        sbcDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.sbc_dp);

        commonVariable(sbcDialplan, callDetails, agent);

        sbcDialplan.setRecord(Boolean.TRUE);
        sbcDialplan.setOutboundVendor(Boolean.TRUE);
        if (callDetails.getCallerId() == CallerId.BLOCK_CALLER_ID) {
            sbcDialplan.setCallerId(CallerId.BLOCK_CALLER_ID);
        }

        sbcDialplan.addAction(new Set(FreeswitchVariables.ringback, "${us-ring}"));
        if (callDetails != null) {
            sbcDialplan.getBorrowerInfo().setBorrowerFirstName(callDetails.getFirstName());
            sbcDialplan.getBorrowerInfo().setBorrowerLastName(callDetails.getLastName());
            sbcDialplan.getBorrowerInfo().setLoanId(callDetails.getAccountPk());
        }

        sbcDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(sbcDialplan);
    }

    public void commonVariable(TMSDialplan tMSDialplan, TMSCallDetails callDetails, AgentTMS agent) {
        tMSDialplan.setDebugOn(getDebugOn());
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setCallerId(callDetails.getCallerId());
        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());
        tMSDialplan.setCallDirection(CallDirection.OUTBOUND);
        tMSDialplan.setDialer(Boolean.FALSE);
        tMSDialplan.setCallee(inVariables.getCalleeIdNumber());
        tMSDialplan.setCaller(inVariables.getCallerIdNumber());
        if (agent.getCallerIdForOutboundCalls() != null) {
            tMSDialplan.setEffective_caller_id_number(agent.getCallerIdForOutboundCalls());
        }
        if (inVariables.getUserOverrideCallerId() != null && inVariables.getUserOverrideCallerId().equalsIgnoreCase("Blocked")) {
            tMSDialplan.setCallerId(CallerId.BLOCK_CALLER_ID);
        }else if (inVariables.getUserOverrideCallerId() != null && inVariables.getUserOverrideCallerId().equalsIgnoreCase("Default") == false) {
            try {
                Long number = Long.parseLong(inVariables.getUserOverrideCallerId());
                tMSDialplan.setEffective_caller_id_number(number + "");
                tMSDialplan.setCallerId(CallerId.CUSTOM);
                tMSDialplan.setCallerIdNumberMask(number);
            } catch (NumberFormatException ex) {

            }
        }
        tMSDialplan.getBorrowerInfo().setBorrowerPhoneNumber(inVariables.getCalleeIdNumber() + "");

    }
}
