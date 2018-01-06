/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.incoming;

import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.FreeswitchContext;
import com.amp.tms.enumerated.RecordedPhrases;
import com.amp.tms.enumerated.refrence.BeanServices;
import com.amp.tms.enumerated.refrence.HOLDOrder;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.Answer;
import com.amp.tms.freeswitch.dialplan.action.BridgeToFifo;
import com.amp.tms.freeswitch.dialplan.action.BridgeToSofiaContact;
import com.amp.tms.freeswitch.dialplan.action.Fifo;
import com.amp.tms.freeswitch.dialplan.action.Playback;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.dialplan.action.TMSOrder;
import com.amp.tms.freeswitch.originate.OriginateBuilder;
import com.amp.tms.freeswitch.pojo.InboundDialerInfoPojo;
import com.amp.tms.freeswitch.premaid.DialplanBuilder;
import com.amp.tms.pojo.BorrowerInfo;

/**
 *
 * @author hsleiman
 */
public class IncomingPlaceOffHold extends DialplanBuilder {

    private InboundDialerInfoPojo dialerInfoPojo;
    private TMSDialplan oldPassedIn = null;

    public IncomingPlaceOffHold(InboundDialerInfoPojo dialerInfoPojo, TMSDialplan old) {
        super();
        this.dialerInfoPojo = dialerInfoPojo;
        this.oldPassedIn = old;
    }

    @Override
    public void createDialplans() {
        log.info("Create Dialplans");

        TMS_UUID = dialerInfoPojo.getCallUUID();

    }

    @Override
    public void buildDialplans() {
       
        if (oldPassedIn == null) {
            for (int i = 0; i < 100; i++) {
                log.error("Old TMS DIALPLAN WAS NULL SHOULD NOT BE THE CASE.");
            }
        }

        callOriginatingToFifo(oldPassedIn);
        callEnteringAgent(oldPassedIn);
        putBackIntoFifo();
        biuldVoicemailOption(oldPassedIn);

    }

    @Override
    public void saveDialplans() {

    }

    public void commonVariable(TMSDialplan tMSDialplan, TMSDialplan old) {
        tMSDialplan.setDebugOn(getDebugOn());
        tMSDialplan.setTms_type(this.getClass().getSimpleName());

        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());

        tMSDialplan.setRecord(Boolean.FALSE);
        tMSDialplan.setCallDirection(CallDirection.INBOUND);
        tMSDialplan.setAutoAswer(dialerInfoPojo.getSettings().isAutoAnswerEnabled());
        tMSDialplan.setPopupType(dialerInfoPojo.getSettings().getPopupDisplayMode());
        tMSDialplan.setCaller(dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber() + "");
        tMSDialplan.setCallee(dialerInfoPojo.getAgentExt() + "");
        tMSDialplan.setOutboundVendor(Boolean.FALSE);
        tMSDialplan.setDialer(Boolean.FALSE);
        tMSDialplan.setDialerQueueId(dialerInfoPojo.getSettings().getDialerQueuePk());

        BorrowerInfo borrowerInfo = new BorrowerInfo();
        borrowerInfo.setLoanId(dialerInfoPojo.getLoanId());
        borrowerInfo.setBorrowerFirstName(dialerInfoPojo.getBorrowerFirstName());
        borrowerInfo.setBorrowerLastName(dialerInfoPojo.getBorrowerLastName());
        borrowerInfo.setBorrowerPhoneNumber(dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber() + "");
        tMSDialplan.setBorrowerInfo(borrowerInfo);
        tMSDialplan.setIvrAuthorized(callDetailRecordService.getIVRAuthorized(TMS_UUID));
        if (old != null) {

            tMSDialplan.addAction(new Set("fifo_bridge_uuid=" + old.getUniqueID()));
        }
    }

    public void callEnteringAgent(TMSDialplan old) {
        TMSDialplan agentDialplan;
        agentDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.agent_dp, HOLDOrder.PLACE_OFF_HOLD);
        commonVariable(agentDialplan, old);

        agentDialplan.setCallee(Integer.toString(dialerInfoPojo.getAgentExt()));
        agentDialplan.addAction(Set.create(FreeswitchVariables.hangup_after_bridge, Boolean.TRUE));
        if (dialerInfoPojo.getSettings().getMaxDelayBeforeAgentAnswer() == null) {
            agentDialplan.addAction(Set.create(FreeswitchVariables.call_timeout, 20));
            agentDialplan.setMaxDelayBeforeAgentAnswer(20);
        } else {
            agentDialplan.addAction(Set.create(FreeswitchVariables.call_timeout, dialerInfoPojo.getSettings().getMaxDelayBeforeAgentAnswer()));
            agentDialplan.setMaxDelayBeforeAgentAnswer(dialerInfoPojo.getSettings().getMaxDelayBeforeAgentAnswer());
        }
        agentDialplan.addAction(Set.create(FreeswitchVariables.continue_on_fail, Boolean.TRUE));

        agentDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));

        if (dialerInfoPojo.getAgentGroupId() != null) {
            agentDialplan.addAction(Set.create(FreeswitchVariables.agent_group_id, dialerInfoPojo.getAgentGroupId()));
            agentDialplan.setAgentGroupId(dialerInfoPojo.getAgentGroupId());
        }

        //agentDialplan.addAction(new Bridge("${sofia_contact(" + dialerInfoPojo.getAgentExt() +"@"+agenService.getAgent(dialerInfoPojo.getAgentExt()).getFreeswitchDomain()+ ")}"));
        agentDialplan.addAction(new BridgeToSofiaContact(dialerInfoPojo.getAgentExt(), agenService.getAgent(dialerInfoPojo.getAgentExt()).getFreeswitchDomain()));

        agentDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_ON_HOLD.name()));
        agentDialplan.addAction(Set.create(FreeswitchVariables.ringback, "${us-ring}"));
        agentDialplan.addBridge(new BridgeToFifo(FreeswitchContext.agent_dp, freeswitchService.getFreeswitchIPNew(agentDialplan.getCall_uuid(), FreeswitchContext.fifo_dp)));

        agentDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(agentDialplan);

    }

    public void callOriginatingToFifo(TMSDialplan old) {
        TMSDialplan fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_OFF_HOLD);
        commonVariable(fifoDialplan, old);

        fifoDialplan.addAction(new Set("fifo_music=$${hold_music}"));
        fifoDialplan.addBridge(new Answer());
        if (dialerInfoPojo.getSettings() == null) {
            fifoDialplan.addBridge(new Fifo("InboundDialerQueue_default" + " out nowait"));
        } else {
            fifoDialplan.addBridge(new Fifo("InboundDialerQueue_" + dialerInfoPojo.getSettings().getDialerQueuePk() + " out nowait"));
        }

        OriginateBuilder originateBuilder = new OriginateBuilder();
        originateBuilder.putInBothLegs(FreeswitchVariables.tms_uuid, TMS_UUID);
        originateBuilder.putInBothLegs(FreeswitchVariables.is_tms_dp, Boolean.TRUE);
        originateBuilder.putInBothLegs(FreeswitchVariables.tms_order, HOLDOrder.PLACE_OFF_HOLD.name());
        //originateBuilder.putInBothLegs(FreeswitchVariables.origination_caller_id_name, dialerInfoPojo.getPhoneToTypeSingle().getFirstName() + "_" + dialerInfoPojo.getPhoneToTypeSingle().getLastName());
        originateBuilder.putInBothLegs(FreeswitchVariables.origination_caller_id_number, dialerInfoPojo.getPhoneToTypeSingle().getPhoneNumber());
        originateBuilder.putInBothLegs(FreeswitchVariables.tms_transfer, Boolean.FALSE);

        originateBuilder.appendALeg("sofia/fifo/sip:");
        originateBuilder.appendALeg(dialerInfoPojo.getAgentExt());
        originateBuilder.appendALeg("@");
//        originateBuilder.appendALeg(configuration.getFreeswitchIPNew(fifoDialplan.getCall_uuid(), FreeswitchContext.agent_dp));
        originateBuilder.appendALeg(agenService.getFreeswitchIPForExt(dialerInfoPojo.getAgentExt()));
        originateBuilder.appendALeg(":");
        originateBuilder.appendALeg(FreeswitchContext.agent_dp.getPort());
        originateBuilder.appendALeg(";transport=tcp");
        originateBuilder.appendALeg(" ");

        originateBuilder.appendBLeg(1000);
        originateBuilder.appendBLeg(" XML " + FreeswitchContext.fifo_dp);
        fifoDialplan.setOriginate(originateBuilder.build());
        //fifoDialplan.setOriginateIP(configuration.getFreeswitchIPForExt(dialerInfoPojo.getAgentExt()));
        fifoDialplan.setOriginateIP(freeswitchService.getFreeswitchIPNew(fifoDialplan.getCall_uuid(), FreeswitchContext.fifo_dp));

        fifoDialplan.setXMLFromDialplan();

        setOriginate(fifoDialplan);

        dialplanService.updateTMSDialplan(fifoDialplan);
    }

    private void putBackIntoFifo() {
        TMSDialplan fifoDialplan;
        fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_ON_HOLD.name());
        commonVariable(fifoDialplan, null);
        fifoDialplan.addAction(new Set("fifo_music", configuration.getFiFoHoldMusic()));

        fifoDialplan.addAction(new Answer());
        fifoDialplan.setBean(BeanServices.FifoService);
        fifoDialplan.setFunctionCall("placeCallOnHold");

        fifoDialplan.addAction(new Playback(RecordedPhrases.PLEASE_WAIT_FOR_NEXT_AGENT));

        fifoDialplan.addAction(new TMSOrder(HOLDOrder.PLACE_IN_VOICEMAIL));
        fifoDialplan.addAction(new Set("fifo_orbit_exten=1000:" + configuration.getMaxCallOnHoldAllowed()));
        fifoDialplan.addAction(new Set("fifo_chime_list=" + RecordedPhrases.HOLD_ANNOUNCE_PRESS1.getAudioPath()));
        fifoDialplan.addAction(new Set("fifo_chime_freq=" + configuration.getMaxHoldAnnounceTimeInSec()));
        fifoDialplan.addAction(new Set("fifo_caller_exit_key=1"));
        fifoDialplan.addAction(new Set("fifo_caller_exit_to_orbit=true"));

        if (dialerInfoPojo.getSettings() == null) {
            fifoDialplan.addBridge(new Fifo("InboundDialerQueue_default" + " in"));
        } else {
            fifoDialplan.setDialerQueueId(dialerInfoPojo.getSettings().getDialerQueuePk());
            fifoDialplan.addBridge(new Fifo("InboundDialerQueue_" + dialerInfoPojo.getSettings().getDialerQueuePk() + " in"));
        }

        log.info("Saving " + fifoDialplan.getTms_type() + " Dialplan...");

        fifoDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(fifoDialplan);
    }

    private void biuldVoicemailOption(TMSDialplan old) {
        log.info("Info: {}, {}", dialerInfoPojo.toString(), old.getCaller(), old.getCallee());
        IncomingVoicemail builder = new IncomingVoicemail(dialerInfoPojo, old.getCaller(), old.getCallee());
        builder.setTMS_UUID(TMS_UUID);
        builder.buildDialplansWithoutSBC();
    }
}
