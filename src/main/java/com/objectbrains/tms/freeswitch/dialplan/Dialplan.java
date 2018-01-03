/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.dialplan;

import com.objectbrains.sti.constants.CallerId;
import static com.objectbrains.tms.constants.Constants.FREESWITCH_RECORDING_LOCATION;
import com.objectbrains.tms.db.entity.freeswitch.TMSDialplan;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.RecordedPhrases;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.freeswitch.dialplan.action.Answer;
import com.objectbrains.tms.freeswitch.dialplan.action.Bridge;
import com.objectbrains.tms.freeswitch.dialplan.action.BridgeExport;
import com.objectbrains.tms.freeswitch.dialplan.action.Export;
import com.objectbrains.tms.freeswitch.dialplan.action.Hangup;
import com.objectbrains.tms.freeswitch.dialplan.action.Info;
import com.objectbrains.tms.freeswitch.dialplan.action.Playback;
import com.objectbrains.tms.freeswitch.dialplan.action.Privacy;
import com.objectbrains.tms.freeswitch.dialplan.action.RecordSession;
import com.objectbrains.tms.freeswitch.dialplan.action.Set;
import com.objectbrains.tms.freeswitch.dialplan.action.SipCopyCustomHeaders;
import com.objectbrains.tms.freeswitch.dialplan.action.Sleep;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import static com.objectbrains.tms.service.FreeswitchConfiguration.formatToYYYY_MM_DD;
import java.util.ArrayList;
import java.util.Random;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hsleiman
 */
public class Dialplan {

    private String description;
    private FreeswitchContext callerContext;
    private String XML;

    private ArrayList<Extension> extensions;

    private static final Logger log = LoggerFactory.getLogger(Dialplan.class);

    public Dialplan(TMSDialplan tmsDialplan) {
        extensions = new ArrayList<>();
        this.description = tmsDialplan.getTms_type();
        this.callerContext = tmsDialplan.getKey().getContext();
        buildXML(tmsDialplan);
    }

    private void buildXML(TMSDialplan tmsDialplan) {
        String BlegBridgeAddition = "";
        Condition condition = null;
        if (tmsDialplan.getConditionDefault()) {
            condition = new Condition();
        } else if (tmsDialplan.getConditionDefault() == false && tmsDialplan.getConditionField() != null && tmsDialplan.getConditionExpression() != null) {
            condition = new Condition(tmsDialplan.getConditionField(), tmsDialplan.getConditionExpression());
        } else {
            condition = new Condition(null, null);
        }
        if (tmsDialplan.getSipCopyCustomHeaders() != null) {
            condition.addAction(new SipCopyCustomHeaders(tmsDialplan.getSipCopyCustomHeaders()));
        }
        if (tmsDialplan.getIgnore_early_media() != null) {
            condition.addAction(Set.create("ignore_early_media", tmsDialplan.getIgnore_early_media()));
        }
        condition.addAction(Set.create(FreeswitchVariables.is_tms_dp, Boolean.TRUE));
        condition.addAction(Set.create(FreeswitchVariables.tms_uuid, tmsDialplan.getKey().getTms_uuid()));
        condition.addAction(Set.create(FreeswitchVariables.cdr_uuid, tmsDialplan.getCdr_uuid()));
        condition.addAction(Set.create(FreeswitchVariables.tms_order, tmsDialplan.getKey().getOrderPower()));
        condition.addAction(Set.create(FreeswitchVariables.call_uuid, tmsDialplan.getCall_uuid()));
        condition.addAction(Set.create(FreeswitchVariables.is_auto_answer, tmsDialplan.getAutoAswer()));
        condition.addAction(Set.create(FreeswitchVariables.call_direction, tmsDialplan.getCallDirection()));
        condition.addAction(Set.create(FreeswitchVariables.is_dialer, tmsDialplan.getDialer()));
        condition.addAction(new BridgeExport("hold_music", FreeswitchConfiguration.getHoldMusic()));

        if (tmsDialplan.getKey().getContext() == FreeswitchContext.ivr_dp) {
            condition.addAction(Set.create(FreeswitchVariables.ivr_step, tmsDialplan.getIvrStepCount()));
        }
        if (tmsDialplan.getIvrAuthorized() != null) {
            condition.addAction(Set.create(FreeswitchVariables.ivr_authorized, tmsDialplan.getIvrAuthorized()));
        }

//        if (tmsDialplan.getKey().getContext() == FreeswitchContext.agent_dp && tmsDialplan.getCallDirection() == CallDirection.OUTBOUND) {
//            //if ("ManualDial".equals(tmsDialplan.getTms_type())) {
//                condition.addAction(new Export("nolocal:absolute_codec_string=PCMU"));
//            //}
//        }
//        
//        if(tmsDialplan.getKey().getContext() == FreeswitchContext.fifo_dp && tmsDialplan.getTms_type().equalsIgnoreCase("ConnectCallToAgent")){
//            condition.addAction(new Export("nolocal:absolute_codec_string=PCMU"));
//        }
        if (tmsDialplan.getKey().getContext() == FreeswitchContext.sbc_dp && tmsDialplan.getCallDirection() == CallDirection.OUTBOUND) {
            condition.addAction(new Export("nolocal:absolute_codec_string=PCMU"));
        }

        if (tmsDialplan.getRecord() != null && tmsDialplan.getRecord()) {
            String loanId = "Unknown";
            if (tmsDialplan.getBorrowerInfo() != null && tmsDialplan.getBorrowerInfo().getLoanId() != null) {
                loanId = tmsDialplan.getBorrowerInfo().getLoanId() + "";
            }
            condition.addAction(new Set("RECORD_TITLE", tmsDialplan.getCallDirection().name() + " - ${destination_number} - ${caller_id_number} - " + loanId + " - " + tmsDialplan.getCall_uuid()));
            condition.addAction(new Set("RECORD_STEREO", "true"));
            condition.addAction(new Set("RECORD_COPYRIGHT", "(c) 2017"));
            condition.addAction(new Set("FreeSWITCH-SBC"));
            condition.addAction(new Set("RECORD_COMMENT", "FreeSWITCH-" + tmsDialplan.getChannelCallUUID() + " ---- CU: " + tmsDialplan.getCall_uuid()));
            condition.addAction(new Set("record_sample_rate", "8000"));
            condition.addAction(new Set("RECORD_DATE", "${strftime(%Y-%m-%d %H:%M)}"));

            condition.addAction(new Info());

            if (tmsDialplan.getCallDirection() == CallDirection.INBOUND) {
                // condition.addAction(new DisplaceSession("tone_stream://%(100,15000,800);loops=-1 mux"));
            } else if (tmsDialplan.getCallDirection() == CallDirection.OUTBOUND) {
                if (tmsDialplan.getOutboundBeepUseNew()) {
                    BlegBridgeAddition = "{execute_on_answer='lua /usr/local/freeswitch/scripts/beep.lua'}";

                } else {
                    if (tmsDialplan.getOutboundBeepVolume() == null) {
                        condition.addAction(new Export("beep_api_result=${sched_api(@" + tmsDialplan.getOutboundBeepLapseSpace() + " ${uuid}_sc" + " uuid_broadcast ${uuid} playback::tone_stream://%(" + tmsDialplan.getOutboundBeepOnDuration() + "," + tmsDialplan.getOutboundBeepOffDuration() + "," + tmsDialplan.getOutboundBeepHz() + ") bleg)}"));
                    } else {
                        condition.addAction(new Export("beep_api_result=${sched_api(@" + tmsDialplan.getOutboundBeepLapseSpace() + " ${uuid}_sc" + " uuid_broadcast ${uuid} playback::tone_stream://" + tmsDialplan.getOutboundBeepVolume() + "%(" + tmsDialplan.getOutboundBeepOnDuration() + "," + tmsDialplan.getOutboundBeepOffDuration() + "," + tmsDialplan.getOutboundBeepHz() + ") bleg)}"));
                    }
                    condition.addAction(Set.create("session_in_hangup_hook", Boolean.TRUE));
                    condition.addAction(Set.create("api_hangup_hook", "sched_del ${uuid}_sc"));
                }

            }
            LocalDateTime now = LocalDateTime.now();
            String date = formatToYYYY_MM_DD(now);

            String extra = "";
            if (tmsDialplan.getKey().getContext() != FreeswitchContext.sbc_dp || (tmsDialplan.getKey().getContext() == FreeswitchContext.sbc_dp && tmsDialplan.getTms_type().equalsIgnoreCase("AgentToAgentTransferToP1"))) {
                extra = "_" + tmsDialplan.getKey().getContext().name() + "_" + now.getMillisOfDay();
            }

            String localRecordingPath = FREESWITCH_RECORDING_LOCATION + date + "/" + tmsDialplan.getCall_uuid() + "/" + tmsDialplan.getCall_uuid() + extra + ".wav";
            condition.addAction(new RecordSession(localRecordingPath));

            String gceRecordingPath = " gs://" + FreeswitchConfiguration.getPhoneRecordingBucket() + "/" + date + "/" + tmsDialplan.getCall_uuid() + "/" + tmsDialplan.getCall_uuid() + extra + ".wav";
            String bash = "gsutil -q cp " + localRecordingPath + gceRecordingPath;
            String curl = "/" + date + "/" + FreeswitchConfiguration.getPhoneRecordingBucket() + "/" + tmsDialplan.getCall_uuid() + "/" + tmsDialplan.getCall_uuid() + extra;

            if (tmsDialplan.getUploadRecodingOnCallEnd()) {
                condition.addAction(new Set("record_post_process_exec_app", "system " + bash));
            } else if (tmsDialplan.getUploadRecodingOnTouch()) {
                bash = "curl " + tmsDialplan.getUploadRecodingURL() + curl;
                condition.addAction(new Set("record_post_process_exec_app", "system " + bash));
            }
            condition.addAction(Set.create(FreeswitchVariables.recroding_upload_tms, curl));
        }

        if (tmsDialplan.getCallerId() != null) {
            condition.addAction(Set.create(FreeswitchVariables.tms_callerId, tmsDialplan.getCallerId().value()));
        }
        if (tmsDialplan.getCallDirection() == CallDirection.OUTBOUND) {
            try {
                if (tmsDialplan.getCallerId() == CallerId.CUSTOM) {
                    //condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_name, "714627" + tmsDialplan.getCaller()));
                    //condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_number, "714627" + tmsDialplan.getCaller()));
                    //condition.addAction(Set.create(FreeswitchVariables.effective_caller_id_number, "714627" + tmsDialplan.getCaller()));
                    if (tmsDialplan.getCallerIdNumberMask() == null) {
                        condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_name, tmsDialplan.getDefaultCallerIdNumber()));
                        condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_number, tmsDialplan.getDefaultCallerIdNumber()));
                        condition.addAction(Set.create(FreeswitchVariables.effective_caller_id_number, tmsDialplan.getDefaultCallerIdNumber()));
                    } else {
                        condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_name, tmsDialplan.getCallerIdNumberMask()));
                        condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_number, tmsDialplan.getCallerIdNumberMask()));
                        condition.addAction(Set.create(FreeswitchVariables.effective_caller_id_number, tmsDialplan.getCallerIdNumberMask()));
                    }

                } else if (tmsDialplan.getCallerId() == CallerId.BLOCK_CALLER_ID) {
                    condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_name, "Unknown"));
                    //condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_number, "0000000000"));
                    //condition.addAction(Set.create(FreeswitchVariables.effective_caller_id_number, "0000000000"));
                    condition.addAction(new Privacy("full"));
                    condition.addAction(Set.create("sip_h_Privacy", "id"));
                    condition.addAction(Set.create("privacy", "yes"));
                } else {
                    if (tmsDialplan.getCallee().length() > 4) {

                        condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_name, tmsDialplan.getDefaultCallerIdNumber()));
                        condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_number, tmsDialplan.getDefaultCallerIdNumber()));
                        condition.addAction(Set.create(FreeswitchVariables.effective_caller_id_number, tmsDialplan.getDefaultCallerIdNumber()));
                    } else {
                        condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_name, "" + tmsDialplan.getCaller()));
                        condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_number, "" + tmsDialplan.getCaller()));
                        condition.addAction(Set.create(FreeswitchVariables.effective_caller_id_number, "" + tmsDialplan.getCaller()));
                    }
                }
            } catch (Exception ex) {
                condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_name, tmsDialplan.getDefaultCallerIdNumber()));
                condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_number, tmsDialplan.getDefaultCallerIdNumber()));
                condition.addAction(Set.create(FreeswitchVariables.effective_caller_id_number, tmsDialplan.getDefaultCallerIdNumber()));
            }
        } else {
            condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_name, tmsDialplan.getCaller()));
            condition.addAction(Set.create(FreeswitchVariables.origination_caller_id_number, tmsDialplan.getCaller()));
            condition.addAction(Set.create(FreeswitchVariables.tms_other_phone, tmsDialplan.getCaller()));
            condition.addAction(Set.create(FreeswitchVariables.effective_caller_id_name, tmsDialplan.getCaller()));
            condition.addAction(Set.create(FreeswitchVariables.effective_caller_id_number, tmsDialplan.getCaller()));
        }

        if (tmsDialplan.getBorrowerInfo().getLoanId() != null) {
            condition.addAction(Set.create(FreeswitchVariables.loan_id, tmsDialplan.getBorrowerInfo().getLoanId()));
        }
        if (allowedForSBCOutbound(tmsDialplan)) {
            if (tmsDialplan.getPopupType() != null) {
                condition.addAction((Set.create(FreeswitchVariables.popup_type, tmsDialplan.getPopupType().value())));
            }
            if (tmsDialplan.getBorrowerInfo().getBorrowerFirstName() != null) {
                condition.addAction(Set.create(FreeswitchVariables.borrower_first_name, tmsDialplan.getBorrowerInfo().getBorrowerFirstName()));
            }
            if (tmsDialplan.getBorrowerInfo().getBorrowerLastName() != null) {
                condition.addAction(Set.create(FreeswitchVariables.borrower_last_name, tmsDialplan.getBorrowerInfo().getBorrowerLastName()));
            }

            if (tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber() != null) {
                condition.addAction(Set.create(FreeswitchVariables.borrower_phone, tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber()));
            } else if (tmsDialplan.getCallDirection() != CallDirection.INTERNAL) {
                condition.addAction(Set.create(FreeswitchVariables.borrower_phone, tmsDialplan.getCaller()));
            }

            if (tmsDialplan.getDialerQueueId() != null) {
                condition.addAction(Set.create(FreeswitchVariables.dialer_queue_id, tmsDialplan.getDialerQueueId()));
            }
            if (tmsDialplan.getIgnore_disposition() != null && tmsDialplan.getIgnore_disposition()) {
                condition.addAction(Set.create(FreeswitchVariables.ignore_disposition, Boolean.TRUE));
            } else {
                condition.addAction(Set.create(FreeswitchVariables.ignore_disposition, Boolean.FALSE));
            }
        }

        if (tmsDialplan.getActions() != null) {
            condition.addAction(tmsDialplan.getActions());
        }
        //condition.addAction(new Info());
        if (tmsDialplan.getOutboundVendor() != null && tmsDialplan.getOutboundVendor()) {

            condition.addAction(new Set("sip_copy_custom_headers=false"));

            if (tmsDialplan.getGatewayVersion() == 2) {
                condition.addAction(new Set("gateway", "rsbc-110-2"));
            } else {
                condition.addAction(new Set("gateway", "ca-smh-as5400-01"));
            }

            if (tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber().startsWith("1111111") || tmsDialplan.getBorrowerInfo().getBorrowerPhoneNumber().endsWith("1111111")) {

                long random = (long) (Math.random() * 3000 + 1);

                condition.addAction(new Sleep(random));
                condition.addAction(new Answer());
                condition.addAction(new Playback(RecordedPhrases.WELCOME_TO_CASHCALL_AUTO));
                condition.addAction(new Sleep(2000l));
                condition.addAction(new Playback(RecordedPhrases.DNC));
                condition.addAction(new Sleep(1001l));
                condition.addAction(new Playback(RecordedPhrases.VOICEMAIL_PROMPT));
                condition.addAction(new Sleep(1100l));
                condition.addAction(new Playback(RecordedPhrases.VOICEMAIL_TO_LEAVE_VOICEMAIL_PRESS_1));
                condition.addAction(new Sleep(1200l));
                condition.addAction(new Playback(RecordedPhrases.CLOSED_PROMPT_CASHCALL));
                condition.addAction(new Sleep(1300l));

                Random randomB = new Random();
                if (tmsDialplan.getDialer() && randomB.nextBoolean()) {
                    random = (long) (Math.random() * 5000 + 1);
                    random = (long) (Math.random() * random + 1);
                    condition.addAction(new Sleep(random));
                }
                
                log.info("tmsDialplan.getDialerQueueId() {}", tmsDialplan.getDialerQueueId());
                if (tmsDialplan.getDialer() && (tmsDialplan.getDialerQueueId() == null || tmsDialplan.getDialerQueueId() == 2)) {
                    condition.addAction(new Sleep(1800000l));
                }
                condition.addAction(new Playback(RecordedPhrases.GOODEBYE));
                condition.addAction(new Hangup("NORMAL_CLEARING"));
            } else if (tmsDialplan.getCallee().length() == 10) {

                if (tmsDialplan.getGatewayVersion() == 2) {
                    condition.addAction(new Bridge(BlegBridgeAddition + "sofia/gateway/${gateway}/1$1|sofia/gateway/rsbc-110-3/1$1|sofia/gateway/rsbc-111-2/1$1|sofia/gateway/rsbc-111-3/1$1"));
                } else {
                    condition.addAction(new Bridge(BlegBridgeAddition + "sofia/gateway/${gateway}/1$1"));
                }
            } else {
                if (tmsDialplan.getGatewayVersion() == 2) {
                    condition.addAction(new Bridge(BlegBridgeAddition + "sofia/gateway/${gateway}/$1|sofia/gateway/rsbc-110-3/$1|sofia/gateway/rsbc-111-2/$1|sofia/gateway/rsbc-111-3/$1"));
                } else {
                    condition.addAction(new Bridge(BlegBridgeAddition + "sofia/gateway/${gateway}/$1"));
                }
            }
        } else {
            if (tmsDialplan.getBridges() != null) {
                condition.addAction(tmsDialplan.getBridges());
            } else if (tmsDialplan.getBean() != null && tmsDialplan.getFunctionCall() != null) {
                log.info("Future TMS Dialplan");
            } else {
                log.error("Dialplan was not for the future and did not have bridge assign to it.");
            }
        }
        if (tmsDialplan.getRecord() != null && tmsDialplan.getRecord() && tmsDialplan.getCallDirection() == CallDirection.OUTBOUND) {
            //condition.addAction(Set.create("unsched_api_result", "${sched_del("+tmsDialplan.getCall_uuid()+"_sc"+")}"));
        }

        addSingleCondition(condition);
    }

    private boolean allowedForSBCOutbound(TMSDialplan tmsDialplan) {
        if (tmsDialplan.getKey().getContext().equals(FreeswitchContext.sbc_dp) && tmsDialplan.getCallDirection() == CallDirection.OUTBOUND) {
            return false;
        } else {
            return true;
        }
    }

    public Dialplan(String description, FreeswitchContext callerContext) {
        extensions = new ArrayList<>();
        this.description = description;
        this.callerContext = callerContext;
    }

    public Dialplan(String description, FreeswitchContext callerContext, Condition condition) {
        extensions = new ArrayList<>();
        this.description = description;
        this.callerContext = callerContext;
        addSingleCondition(condition);
    }

    public Dialplan(String xml) {
        this.XML = xml;
    }

    public String getXML() {
        StringBuilder xml = new StringBuilder();
        if (XML != null) {
            xml.append(XML);
        } else {
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
            xml.append("<document type=\"freeswitch/xml\">\n");
            xml.append("<section name=\"dialplan\" description=\"").append(this.description).append("\">\n");
            xml.append("<context name=\"").append(this.callerContext).append("\">\n");
            for (Extension get : extensions) {
                get.appendXML(xml);
            }
            xml.append("</context>\n");
            xml.append("</section>\n");
            xml.append("</document>");
        }
        return xml.toString();
    }

    public Extension createExtension() {
        return new Extension(this.description);
    }

    public void addSingleCondition(Condition condition) {
        Extension extension = createExtension();
        extension.addCondition(condition);
        addExtension(extension);
    }

    public void addExtension(Extension extension) {
        extensions.add(extension);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String Description) {
        this.description = Description;
    }

    public FreeswitchContext getCallerContext() {
        return callerContext;
    }

    public void setCallerContext(FreeswitchContext CallerContext) {
        this.callerContext = CallerContext;
    }

    public ArrayList<Extension> getExtensions() {
        return extensions;
    }

    public void setExtensions(ArrayList<Extension> extensions) {
        this.extensions = extensions;
    }

}
