/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid;

import com.amp.crm.service.dialer.DialerQueueService;
import com.amp.crm.service.tms.TMSService;
import com.amp.tms.db.entity.cdr.CallDetailRecordTMS;
import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.db.hibernate.ApplicationContextProvider;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.freeswitch.premaid.service.DialplanBuilderAutowireOb;
import com.amp.tms.freeswitch.premaid.service.PremaidActions;
import com.amp.tms.freeswitch.premaid.service.TMSDialplanService;
import com.amp.tms.service.AgentCallService;
import com.amp.tms.service.TMSAgentService;
import com.amp.tms.service.AgentStatsService;
import com.amp.tms.service.CallDetailRecordService;
import com.amp.tms.service.DialerQueueRecordService;
import com.amp.tms.service.DialplanService;
import com.amp.tms.service.DncService;
import com.amp.tms.service.FreeswitchConfiguration;
import com.amp.tms.service.FreeswitchService;
import com.amp.tms.service.InboundCallService;
import com.amp.tms.service.TextToSpeechService;
import com.amp.tms.service.freeswitch.CallingOutService;
import com.amp.tms.service.freeswitch.common.Incoming2;
import com.amp.tms.websocket.Websocket;
import java.util.UUID;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hsleiman
 */
public abstract class DialplanBuilder implements DialplanInterface {

    private Boolean debugOn;
    private String originate;
    private String originateIP;

    private TMSDialplan tmsDialplan;

    protected String TMS_UUID;
    protected DialplanVariable inVariables;
    protected final static Logger log = LoggerFactory.getLogger(DialplanBuilder.class);

    protected DialerQueueRecordService dialerQueueRecordRepository;

    protected TMSDialplanService variableService;
    protected PremaidActions premaidActions;
    protected TMSAgentService agenService;
    protected AgentCallService agentCallService;
    protected AgentStatsService agentStatsService;
    protected DialplanService dialplanService;
    protected TMSService tmsIWS;
    protected DncService dnc;
    protected FreeswitchConfiguration configuration;
    protected Websocket websocket;
    protected CallDetailRecordService callDetailRecordService;
    protected CallingOutService callingOutService;
    protected TextToSpeechService textToSpeechService;
    protected InboundCallService inboundCallService;
    protected Incoming2  incoming;
    protected FreeswitchService freeswitchService;
    protected DialerQueueService dialerQueueService;

    public DialplanBuilder() {
        TMS_UUID = UUID.randomUUID().toString();
        DialplanBuilderAutowireOb autowireOb = (DialplanBuilderAutowireOb) ApplicationContextProvider.getApplicationContext().getBean(DialplanBuilderAutowireOb.class);
        variableService = autowireOb.getVariableService();
        premaidActions = autowireOb.getPremaidActions();
        agenService = autowireOb.getAgentService();
        dialplanService = autowireOb.getDialplanService();
        agentCallService = autowireOb.getAgentCallService();
        tmsIWS = autowireOb.getTmsIWS();
        dnc = autowireOb.getDnc();
        configuration = autowireOb.getConfiguration();
        websocket = autowireOb.getWebsocket();
        callDetailRecordService = autowireOb.getCallDetailRecordService();
        callingOutService = autowireOb.getCallingOutService();
        textToSpeechService = autowireOb.getTextToSpeechService();
        agentStatsService = autowireOb.getAgentStatsService();
        inboundCallService = autowireOb.getInboundCallService();
        incoming = autowireOb.getIncoming2();
        dialerQueueRecordRepository = autowireOb.getDialerQueueRecordRepository();
        freeswitchService = autowireOb.getFreeswitchService();
        dialerQueueService = autowireOb.getDialerQueueService();
        this.debugOn = Boolean.TRUE;
        
    }

    public void setVariable(DialplanVariable variable) {
        this.inVariables = variable;
    }

    public DialplanVariable getVariable() {
        return this.inVariables;
    }

    public void setTMS_UUID(String TMS_UUID) {
        this.TMS_UUID = TMS_UUID;
    }

    public String getTMS_UUID() {
        return TMS_UUID;
    }

    public void build(String TMS_UUID) {
        this.TMS_UUID = TMS_UUID;
        build();
    }

    public void build() {
        CallDetailRecordTMS callDetailRecord = callDetailRecordService.getCDR(TMS_UUID);
        callDetailRecord.setDialplan_type(this.getClass().getSimpleName());
        callDetailRecordService.saveCDR(callDetailRecord);

        this.createDialplans();
        this.buildDialplans();
        this.saveDialplans();

//        if (configuration.sendCDRSyncToSVCAtStartOfDialplan()) {
//            if (callDetailRecord.getDialplan_type().equalsIgnoreCase("AgentToAgent") == false) {
//                if (tmsDialplan != null) {
//                    SvCallDetailRecord callDetailRecordMaster = new SvCallDetailRecord();
//                    callDetailRecordMaster.setCallUUID(callDetailRecord.getCall_uuid());
//                    callDetailRecordMaster.setDialer(tmsDialplan.getDialer());
//                    callDetailRecordService.saveInitialMasterRecordIntoSVC(callDetailRecordMaster);
//                }
//            }
//        }
    }

    public void execute() {
        build();
        PlaceCall();
    }

    public TMSDialplan getDialplan() {
        build();
        return tmsDialplan;
    }

    protected Boolean getDebugOn() {
        return debugOn;
    }

    protected void setReturnDialplan(TMSDialplan dialplan) {
        dialplan.setCompleted(true);
        dialplan.setRetreived(LocalDateTime.now());
        dialplan.setElapseLife(System.currentTimeMillis() - dialplan.getCreateLife());
        if (inVariables != null) {
            dialplan.setUniqueID(inVariables.getUniqueID());
            dialplan.setChannelCallUUID(inVariables.getChannelCallUUID());
            dialplan.setVariables(inVariables.toJson());
        }
        tmsDialplan = dialplan;
        dialplanService.updateTMSDialplan(dialplan);
    }

    protected void setOriginate(TMSDialplan dialplan) {
        originate = dialplan.getOriginate();
        originateIP = dialplan.getOriginateIP();
    }

    protected void setDebugOn(Boolean debugOn) {
        this.debugOn = debugOn;
    }

    private void PlaceCall() {
        callingOutService.PlaceOriginateToFreedwitchAsyc(originate, originateIP);
    }
}
