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
import com.objectbrains.tms.enumerated.RecordedPhrases;
import com.objectbrains.tms.enumerated.RecordedWords;
import com.objectbrains.tms.enumerated.WorkHours;
import com.objectbrains.tms.enumerated.refrence.BeanServices;
import com.objectbrains.tms.enumerated.refrence.HOLDOrder;
import com.objectbrains.tms.enumerated.refrence.IVROrder;
import com.objectbrains.tms.enumerated.refrence.IVROrder2;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.freeswitch.dialplan.action.Answer;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeToIVR;
import com.objectbrains.tms.freeswitch.dialplan.action.Fifo;
import com.objectbrains.tms.freeswitch.dialplan.action.Hangup;
import com.objectbrains.tms.freeswitch.dialplan.action.Info;
import com.objectbrains.tms.freeswitch.dialplan.action.PlayAndGetDigits;
import com.objectbrains.tms.freeswitch.dialplan.action.Playback;
import com.objectbrains.tms.freeswitch.dialplan.action.Set;
import com.objectbrains.tms.freeswitch.dialplan.action.Sleep;
import com.objectbrains.tms.freeswitch.dialplan.action.TMSOrder;
import com.objectbrains.tms.freeswitch.dialplan.action.Transfer;
import com.objectbrains.tms.freeswitch.pojo.AgentIncomingDistributionOrder;
import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.pojo.InboundDialerInfoPojo;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;
import org.joda.time.LocalDate;

/**
 *
 * @author hsleiman
 */
public class IncomingVoicemail extends DialplanBuilder {

    private AgentIncomingDistributionOrder aido;
    private InboundDialerInfoPojo dialerInfoPojo;
    private String CallerIdNumber;
    private String CalleeIdNumber;
    private WorkHours workHours;
    private Boolean isAfterHour = Boolean.FALSE;

    public IncomingVoicemail(DialplanVariable variable) {
        super();
        setVariable(variable);
    }

    public IncomingVoicemail(DialplanVariable variable, AgentIncomingDistributionOrder aido) {
        super();
        this.aido = aido;
        setVariable(variable);
    }

    public IncomingVoicemail(DialplanVariable variable, AgentIncomingDistributionOrder aido, WorkHours workHours, Boolean isAfterHour) {
        super();
        this.aido = aido;
        setVariable(variable);
        this.workHours = workHours;
        this.isAfterHour = isAfterHour;
    }

    public IncomingVoicemail(InboundDialerInfoPojo dialerInfoPojo, String CallerIdNumber, String CalleeIdNumber) {
        super();
        this.dialerInfoPojo = dialerInfoPojo;
        this.CalleeIdNumber = CalleeIdNumber;
        this.CallerIdNumber = CallerIdNumber;
    }

    @Override
    public void createDialplans() {
//        agentDialplan = dialplanRepository.createTMSDialplan(TMS_UUID, FreeswitchContext.AGENT_DIALPLAN);
//        agentDialplan.setTms_type(this.getClass().getSimpleName());

    }

    @Override
    public void buildDialplans() {
        callEnteringSBC();
        WelcomeIVR();
        InboundLeaveVoicemail();
        PlaceInVoicemail();
        InboundLeaveVoicemailAfterHourClosed();
        HangupCall();
    }

    public TMSDialplan buildDialplansWithoutSBC() {
        PlaceInVoicemail();
        InboundLeaveVoicemailAfterHourClosed();
        HangupCall();
        return InboundLeaveVoicemail();
    }

    @Override
    public void saveDialplans() {
    }

    public void commonVariable(TMSDialplan tMSDialplan) {
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setDebugOn(getDebugOn());
        tMSDialplan.setTms_type(this.getClass().getSimpleName());
        tMSDialplan.setCallerId(CallerId.ACTUAL);
        tMSDialplan.setCall_uuid(tMSDialplan.getKey().getTms_uuid());
        tMSDialplan.setCallDirection(CallDirection.INBOUND);
        tMSDialplan.setDialer(Boolean.FALSE);
        if (inVariables != null) {
            tMSDialplan.setCallee(inVariables.getCalleeIdNumber());
            tMSDialplan.setCaller(inVariables.getCallerIdNumber());

        } else {
            tMSDialplan.setCallee(CalleeIdNumber);
            tMSDialplan.setCaller(CallerIdNumber);
        }
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
        tmsDialplan.addBridge(new BridgeToIVR(freeswitchService.getFreeswitchIPNew(tmsDialplan.getCall_uuid(), FreeswitchContext.ivr_dp)));
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
        setReturnDialplan(tmsDialplan);
    }

    private void WelcomeIVR() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp);
        commonVariable(tmsDialplan);
        tmsDialplan.addAction(new Answer());
        tmsDialplan.addAction(new Sleep(1000l));

        if (isAfterHour) {
            tmsDialplan.addAction(new Playback(RecordedPhrases.SORRY_COULD_NOT_PROCESS_YOUR_REQUEST_AT_THIS_TIME));
            String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
            tmsDialplan.addAction(new TMSOrder(IVROrder.INBOUND_LEAVE_VOICE_MAIL.name()));
            tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 2, 5000, "#*", RecordedPhrases.IF_YOU_LIKE_TO_LEAVE_VOICE_MAIL.getAudioPath(), invalidToPlay, "INBOUND_LEAVE_VM_INPUT", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
            
        } else {
            if (false) {
                if (workHours == null && configuration.getStartWorkingHourGlobal(LocalDate.now().getDayOfWeek()) == configuration.getEndWorkingHourGlobal(LocalDate.now().getDayOfWeek())) {
                    tmsDialplan.addAction(new Playback(RecordedPhrases.CLOSED_PROMPT_CASHCALL));
                } else if (workHours != null && workHours.getStart().isEqual(workHours.getEnd())) {
                    tmsDialplan.addAction(new Playback(RecordedPhrases.CLOSED_PROMPT_CASHCALL));
                } else {
                    tmsDialplan.addAction(new Playback(RecordedPhrases.VOICEMAIL_PROMPT_AFTER_HOUR_PART1_CASHCALL));
                    if (workHours == null) {
                        tmsDialplan.addAction(new Playback(configuration.getRecordingFile(configuration.getStartWorkingHourGlobal(LocalDate.now().getDayOfWeek()))));
                    } else {
                        tmsDialplan.addAction(new Playback(configuration.getRecordingFile(workHours.getStart().getHourOfDay())));
                    }
                    tmsDialplan.addAction(new Playback(RecordedWords.AM));
                    tmsDialplan.addAction(new Playback(RecordedWords.AND));
                    if (workHours == null) {
                        if (configuration.getEndWorkingHourGlobal(LocalDate.now().getDayOfWeek()) >= 12) {
                            int change = 0;
                            if (configuration.getEndWorkingHourGlobal(LocalDate.now().getDayOfWeek()) > 12) {
                                change = 12;
                            }
                            tmsDialplan.addAction(new Playback(configuration.getRecordingFile(configuration.getEndWorkingHourGlobal(LocalDate.now().getDayOfWeek()) - change)));
                            tmsDialplan.addAction(new Playback(RecordedWords.PM));
                        } else {
                            tmsDialplan.addAction(new Playback(configuration.getRecordingFile(configuration.getEndWorkingHourGlobal(LocalDate.now().getDayOfWeek()))));
                            tmsDialplan.addAction(new Playback(RecordedWords.AM));
                        }
                    } else {
                        if (workHours.getEnd().getHourOfDay() >= 12) {
                            int change = 0;
                            if (workHours.getEnd().getHourOfDay() > 12) {
                                change = 12;
                            }
                            tmsDialplan.addAction(new Playback(configuration.getRecordingFile(workHours.getEnd().getHourOfDay() - change)));
                            tmsDialplan.addAction(new Playback(RecordedWords.PM));
                        } else {
                            tmsDialplan.addAction(new Playback(configuration.getRecordingFile(workHours.getEnd().getHourOfDay())));
                            tmsDialplan.addAction(new Playback(RecordedWords.AM));
                        }
                    }
                    tmsDialplan.addAction(new Playback(RecordedPhrases.VOICEMAIL_PROMPT_AFTER_HOUR_PART2));
                }
                String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
                tmsDialplan.addAction(new TMSOrder(IVROrder.INBOUND_LEAVE_VOICE_MAIL.name()));
                tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 2, 5000, "#*", RecordedPhrases.VOICEMAIL_TO_LEAVE_VOICEMAIL_PRESS_1.getAudioPath(), invalidToPlay, "INBOUND_LEAVE_VM_INPUT", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));
            } else {
                String invalidToPlay = RecordedPhrases.INVALID_ENTRY.getAudioPath();
                tmsDialplan.addAction(new TMSOrder(IVROrder.INBOUND_LEAVE_VOICE_MAIL.name()));
                tmsDialplan.addAction(new PlayAndGetDigits(1, 1, 2, 5000, "#*", RecordedPhrases.VOICEMAIL_TO_LEAVE_VOICEMAIL_STATIC_CASHCALL.getAudioPath(), invalidToPlay, "INBOUND_LEAVE_VM_INPUT", "\\d", 7000, "1000 XML " + FreeswitchContext.ivr_dp));

            }
        }

        tmsDialplan.addAction(new Set(FreeswitchVariables.option_selected_id, "${INBOUND_LEAVE_VM_INPUT}"));
        tmsDialplan.addAction(new Sleep(1000l));
        tmsDialplan.addAction(new Info());
        tmsDialplan.addBridge(new Transfer("1000 XML " + FreeswitchContext.ivr_dp));
        tmsDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(tmsDialplan);
    }

    private TMSDialplan InboundLeaveVoicemail() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder.INBOUND_LEAVE_VOICE_MAIL);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain);
        tmsDialplan.setFunctionCall(IVROrder.INBOUND_LEAVE_VOICE_MAIL.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        dialplanService.updateTMSDialplan(tmsDialplan);
        return tmsDialplan;
    }
    
    private TMSDialplan InboundLeaveVoicemailAfterHourClosed() {
        TMSDialplan tmsDialplan;
        tmsDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.ivr_dp, IVROrder2.INBOUND_LEAVE_VOICE_MAIL_AFTER_HOUR_CLOSED);
        commonVariable(tmsDialplan);
        tmsDialplan.setBean(BeanServices.IVRMain2AfterHour);
        tmsDialplan.setFunctionCall(IVROrder2.INBOUND_LEAVE_VOICE_MAIL_AFTER_HOUR_CLOSED.getMethodName());
        tmsDialplan.setOnce(Boolean.FALSE);
        dialplanService.updateTMSDialplan(tmsDialplan);
        return tmsDialplan;
    }

    private void PlaceInVoicemail() {
        TMSDialplan fifoDialplan;
        fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp, HOLDOrder.PLACE_IN_VOICEMAIL);
        commonVariable(fifoDialplan);
        fifoDialplan.addAction(new Answer());
        fifoDialplan.addAction(new Sleep(500l));
        fifoDialplan.addAction(new Playback(RecordedPhrases.VOICEMAIL_PROMPT));
        fifoDialplan.addAction(new Sleep(500l));
        fifoDialplan.addAction(new Playback(RecordedWords.TONE_540));

        fifoDialplan.addAction(new Set("fifo_music", ""));
        fifoDialplan.addAction(new Set("fifo_chime_freq=" + (configuration.getVoicemailLengthAllowed() + 300)));
        fifoDialplan.addAction(new Set("fifo_chime_list=" + ""));
        fifoDialplan.addAction(new Set("fifo_caller_exit_key=1"));
        fifoDialplan.addAction(new Set("fifo_caller_exit_to_orbit=false"));

        fifoDialplan.setBean(BeanServices.IVRMain);
        fifoDialplan.setFunctionCall(HOLDOrder.PLACE_IN_VOICEMAIL.getMethodName());

        fifoDialplan.addAction(new TMSOrder(IVROrder.HANGUP_CALL));
        //fifoDialplan.addAction(TMSOrder.CreateTMSOrder(IVROrder.HANGUP_CALL));
        //fifoDialplan.addAction(new Export("nolocal:"+FreeswitchVariables.tms_order_next+"="+IVROrder.HANGUP_CALL.name()));
        //fifoDialplan.addAction(new Set(FreeswitchVariables.tms_order_next+"="+IVROrder.HANGUP_CALL.name()));
        fifoDialplan.addAction(new Set("fifo_orbit_exten=1000:" + configuration.getVoicemailLengthAllowed()));
        Long queue = Long.MIN_VALUE;
        if (aido != null && aido.getSettings() != null) {
            queue = aido.getSettings().getDialerQueuePk();
        } else if (dialerInfoPojo != null && dialerInfoPojo.getSettings() != null) {
            queue = dialerInfoPojo.getSettings().getDialerQueuePk();
        }

        if (queue != Long.MIN_VALUE) {
            fifoDialplan.setDialerQueueId(queue);
            fifoDialplan.addBridge(new Fifo("InboundDialerQueue_" + queue + " in"));
        } else {
            fifoDialplan.addBridge(new Fifo("InboundDialerQueue_default in"));
        }
        fifoDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(fifoDialplan);
    }

    private void HangupCall() {
        TMSDialplan fifoDialplan;
        fifoDialplan = dialplanService.createTMSDialplan(TMS_UUID, FreeswitchContext.fifo_dp, IVROrder.HANGUP_CALL);
        commonVariable(fifoDialplan);
        fifoDialplan.addAction(new Answer());
        fifoDialplan.addAction(new Sleep(1000l));
        fifoDialplan.addBridge(new Hangup("NORMAL_CLEARING"));
        fifoDialplan.setXMLFromDialplan();
        dialplanService.updateTMSDialplan(fifoDialplan);
    }
}
