/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.incoming;

import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.RecordedPhrases;
import com.amp.tms.enumerated.refrence.BeanServices;
import com.amp.tms.enumerated.refrence.HOLDOrder;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.Answer;
import com.amp.tms.freeswitch.dialplan.action.Bridge;
import com.amp.tms.freeswitch.dialplan.action.BridgeToAgent;
import com.amp.tms.freeswitch.dialplan.action.BridgeToFifo;
import com.amp.tms.freeswitch.dialplan.action.BridgeToSofiaContact;
import com.amp.tms.freeswitch.dialplan.action.Export;
import com.amp.tms.freeswitch.dialplan.action.Fifo;
import com.amp.tms.freeswitch.dialplan.action.Playback;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.dialplan.action.TMSOrder;
import com.amp.tms.freeswitch.dialplan.action.Transfer;
import com.amp.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.service.freeswitch.FsAgentService;
import java.util.List;

/**
 *
 * 
 */
public class IncomingDialerOrder extends DialplanBuilder {

    private AgentIncomingDistributionOrder aido;
    private boolean isBorrowerKnown = true;

    public IncomingDialerOrder(DialplanVariable variable, AgentIncomingDistributionOrder aido) {
        this.aido = aido;
        setVariable(variable);
    }

    public IncomingDialerOrder(DialplanVariable variable, AgentIncomingDistributionOrder aido, boolean isBorrowerKnown) {
        this.aido = aido;
        this.isBorrowerKnown = isBorrowerKnown;
        setVariable(variable);

    }

    @Override
    public void createDialplans() {

    }

    @Override
    public void buildDialplans() {
        List<AgentTMS> agents = aido.getAgents();
        //HzAgent defaultAgent = agenService.getAgent(aido.getDefaultExtension());
        AgentTMS agent = agents.get(0);
        // create the dialplan for sbc to rout the call to agent box.
        callEnteringSBC(agent);
        // create the dialplan for agent to rout the call to the ext.
        callEnteringAgent(agents);
        // if agents dont answer rout the call to the fifo.
        callEnteringFifo();
        biuldVoicemailOption();
    }

    public TMSDialplan buildDialplansWithoutSBC() {
        List<AgentTMS> agents = aido.getAgents();
        //HzAgent defaultAgent = agenService.getAgent(aido.getDefaultExtension());
//        AgentTMS agent = agents.get(0);
        // if agents dont answer rout the call to the fifo.
        callEnteringFifo();
        biuldVoicemailOption();
        return callEnteringAgent(agents);
    }

    @Override
    public void saveDialplans() {

    }

    public void commonVariable(TMSDialplan tMSDialplan) {
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());
        tMSDialplan.setCallDirection(inVariables.getCallDirection());
        tMSDialplan.setAutoAswer(aido.getIsAutoAnswer());
        tMSDialplan.setPopupType(aido.getPopupDisplayMode());
        tMSDialplan.setDialerQueueId(aido.getDialerQueuePK());
        

        tMSDialplan.setCaller(inVariables.getCallerIdNumber());
        tMSDialplan.setCallee(inVariables.getCalleeIdNumber());
        tMSDialplan.setOutboundVendor(Boolean.FALSE);
        if (this.isBorrowerKnown) {
            tMSDialplan.setBorrowerInfo(aido.getBorrowerInfo());
        }
        tMSDialplan.setIvrAuthorized(callDetailRecordService.getIVRAuthorized(TMS_UUID));

        tMSDialplan.setVariables(inVariables.toJson());
        tMSDialplan.setDialer(Boolean.FALSE);
    }

    private void callEnteringSBC(AgentTMS agent) {
        TMSDialplan sbcDialplan;
        sbcDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.sbc_dp);
        commonVariable(sbcDialplan);
        sbcDialplan.setRecord(Boolean.TRUE);
        //if(aido.isDirectLine()){
        if (configuration.enableAnswerOnInboundSBCIncomingDialerOrder()) {
            sbcDialplan.addAction(new Answer());
        }
        sbcDialplan.addAction(new Playback(RecordedPhrases.WELCOME_TO_CASHCALL_AUTO, configuration.getCompanyInfo()));
        //}
        sbcDialplan.addAction(new TMSOrder(0));
        sbcDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
        sbcDialplan.addBridge(new Bridge("sofia/agent/sip:" + agent.getExtension() + "@" + agent.getFreeswitchIP() + ":5044;transport=tcp"));

        log.info("Saving " + sbcDialplan.getTms_type() + " Dialplan...");
        sbcDialplan.setXMLFromDialplan();
        setReturnDialplan(sbcDialplan);
        dialplanService.updateTMSDialplan(sbcDialplan);
    }

    private TMSDialplan secondAgent(AgentTMS agent, AgentTMS nextAgent, int orderPower) {
        TMSDialplan agentDialplan;
        agentDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.agent_dp, orderPower);

        commonVariable(agentDialplan);

        agentDialplan.setBean(BeanServices.FsAgentService);
        agentDialplan.setFunctionCall(FsAgentService.startCallForAgentCallee);

        if (aido.getIsAutoAnswer()) {
            agentDialplan.addAction(new Export("nolocal:execute_on_media=displace_session tone_stream://%(" + configuration.getInboundPlayBeepToAgentDuration() + ",50," + configuration.getInboundPlayBeepToAgentHZ() + ");loops=" + configuration.getAMDPlayBeepToAgentLast()));
        }

        agentDialplan.setCallee(Integer.toString(agent.getExtension()));
        log.info("Setting agent for muliline {}, {}", agent.getExtension(), aido.getIsInlineForAgent(agent.getExtension()));
        agentDialplan.setAgentInline(aido.getIsInlineForAgent(agent.getExtension()));
        agentDialplan.addAction(Set.create(FreeswitchVariables.hangup_after_bridge, Boolean.TRUE));
        if (aido.getMaxDelayBeforeAgentAnswer() == null) {
            agentDialplan.addAction(Set.create(FreeswitchVariables.call_timeout, 20));
            agentDialplan.setMaxDelayBeforeAgentAnswer(20);
        } else {
            agentDialplan.addAction(Set.create(FreeswitchVariables.call_timeout, aido.getMaxDelayBeforeAgentAnswer()));
            agentDialplan.setMaxDelayBeforeAgentAnswer(aido.getMaxDelayBeforeAgentAnswer());
        }
        agentDialplan.addAction(Set.create(FreeswitchVariables.continue_on_fail, Boolean.TRUE));
        agentDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));

        Long agentGroupId = aido.getGroupPkForAgent(agent.getExtension());
        if (agentGroupId != null) {
            agentDialplan.addAction(Set.create(FreeswitchVariables.agent_group_id, agentGroupId));
            agentDialplan.setAgentGroupId(agentGroupId);
        }

//agentDialplan.addAction(new Bridge("${sofia_contact(agent/" + agent.getExtension() + "@" + agent.getFreeswitchDomain() + ")}"));
        agentDialplan.addAction(new BridgeToSofiaContact(agent.getExtension(), agent.getFreeswitchDomain()));
        if (nextAgent != null) {
            agentDialplan.addAction(new TMSOrder(orderPower + 1));

            if (configuration.useTransferForIncomingDialerOrder() == false) {
                agentDialplan.addAction(new BridgeToAgent(agenService.getFreeswitchIPForExt(nextAgent.getExtension()), nextAgent.getExtension()));
            } else {
                agentDialplan.addAction(Set.create(FreeswitchVariables.tms_transfer, false));
                agentDialplan.addAction(new Transfer(nextAgent.getExtension() + " XML " + FreeswitchContext.agent_dp));
            }
        }

        return agentDialplan;
    }

    private TMSDialplan callEnteringAgent(List<AgentTMS> agents) {
        TMSDialplan firstDialplan = null;
        int size = agents.size();
        for (int i = 0; i < size; i++) {
            AgentTMS agent = agents.get(i);
            AgentTMS nextAgent = i + 1 < size ? agents.get(i + 1) : null;
            TMSDialplan agentDialplan = secondAgent(agent, nextAgent, i);
            if (nextAgent == null) {
                agentDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_ON_HOLD));
                agentDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
                agentDialplan.addBridge(new BridgeToFifo(FreeswitchContext.agent_dp, freeswitchService.getFreeswitchIPNew(agentDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));
            }
            log.info("Saving " + agentDialplan.getTms_type() + " Dialplan...");
            agentDialplan.setXMLFromDialplan();
            dialplanService.updateTMSDialplan(agentDialplan);
            if (i == 0) {
                firstDialplan = agentDialplan;
            }
        }
        return firstDialplan;
    }

    private void callEnteringFifo() {
        TMSDialplan fifoDialplan;
        fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_ON_HOLD);
        commonVariable(fifoDialplan);
        fifoDialplan.addAction(new Set("fifo_music", configuration.getFiFoHoldMusic()));

        fifoDialplan.addAction(new Answer());
        fifoDialplan.setBean(BeanServices.FifoService);
        fifoDialplan.setFunctionCall("placeCallOnHold");

        fifoDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_IN_VOICEMAIL));
        fifoDialplan.addAction(new Set("fifo_orbit_exten=1000:" + configuration.getMaxCallOnHoldAllowed()));
        fifoDialplan.addAction(new Set("fifo_chime_list=" + RecordedPhrases.HOLD_ANNOUNCE_PRESS1.getAudioPath()));
        fifoDialplan.addAction(new Set("fifo_chime_freq=" + configuration.getMaxHoldAnnounceTimeInSec()));
        fifoDialplan.addAction(new Set("fifo_caller_exit_key=1"));
        fifoDialplan.addAction(new Set("fifo_caller_exit_to_orbit=true"));

        fifoDialplan.addAction(new Playback(RecordedPhrases.PLEASE_WAIT_FOR_NEXT_AGENT));
        if (aido.getSettings() == null) {
            fifoDialplan.addBridge(new Fifo("InboundDialerQueue_default" + " in"));
        } else {
            if (aido.getDialerQueuePK() == null) {
                fifoDialplan.setDialerQueueId(aido.getSettings().getDialerQueuePk());
            } else {
                fifoDialplan.setDialerQueueId(aido.getDialerQueuePK());
            }
            fifoDialplan.addBridge(new Fifo("InboundDialerQueue_" + fifoDialplan.getDialerQueueId() + " in"));
        }

        log.info("Saving " + fifoDialplan.getTms_type() + " Dialplan...");

        fifoDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(fifoDialplan);
    }

    private void biuldVoicemailOption() {
        IncomingVoicemail builder = new IncomingVoicemail(inVariables, aido);
        builder.setTMS_UUID(TMS_UUID);
        builder.buildDialplansWithoutSBC();
    }
}
