/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.service;


import com.objectbrains.sti.service.dialer.DialerQueueService;
import com.objectbrains.sti.service.tms.TMSService;
import com.objectbrains.tms.service.AgentCallService;
import com.objectbrains.tms.service.TMSAgentService;
import com.objectbrains.tms.service.AgentStatsService;
import com.objectbrains.tms.service.CallDetailRecordService;
import com.objectbrains.tms.service.DialerQueueRecordService;
import com.objectbrains.tms.service.DialplanService;
import com.objectbrains.tms.service.DncService;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import com.objectbrains.tms.service.FreeswitchService;
import com.objectbrains.tms.service.GCESignedUtility;
import com.objectbrains.tms.service.InboundCallService;
import com.objectbrains.tms.service.TextToSpeechService;
import com.objectbrains.tms.service.freeswitch.CallingOutService;
import com.objectbrains.tms.service.freeswitch.common.Incoming2;
import com.objectbrains.tms.websocket.Websocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service("dialplanBuilderAutowireOb")
public class DialplanBuilderAutowireOb {

    private static final Logger log = LoggerFactory.getLogger(DialplanBuilderAutowireOb.class);

    @Autowired
    private TMSDialplanService variableService;
    @Autowired
    private PremaidActions premaidActions;
    @Autowired
    private TMSAgentService agentService;
    @Autowired
    private AgentCallService agentCallService;
    @Autowired
    private DialplanService dialplanService;
    @Autowired
    private TMSService tmsIWS;
    @Autowired
    private DncService dnc;
    @Autowired
    private FreeswitchConfiguration configuration;
    @Autowired
    @Lazy
    private Websocket websocket;
    @Autowired
    private CallDetailRecordService callDetailRecordService;
    @Autowired
    private CallingOutService callingOutService;
    @Autowired
    private TextToSpeechService textToSpeechService;
    @Autowired
    private AgentStatsService agentStatsService;
    @Autowired
    private InboundCallService inboundCallService;
    @Autowired
    private Incoming2 incoming2;
    @Autowired
    private DialerQueueService dialerQueueService;

    @Autowired
    private DialerQueueRecordService dialerQueueRecordRepository;

    @Autowired
    private FreeswitchService freeswitchService;

    @Autowired
    private GCESignedUtility gcssurl;

    public TMSDialplanService getVariableService() {
        return variableService;
    }

    public PremaidActions getPremaidActions() {
        return premaidActions;
    }

    public TMSAgentService getAgentService() {
        return agentService;
    }

    public DialplanService getDialplanService() {
        return dialplanService;
    }

    public void setDialplanService(DialplanService dialplanService) {
        this.dialplanService = dialplanService;
    }

    public void setAgentService(TMSAgentService agentService) {
        this.agentService = agentService;
    }

    public TMSService getTmsIWS() {
        return tmsIWS;
    }

    public DncService getDnc() {
        return dnc;
    }

    public FreeswitchConfiguration getConfiguration() {
        return configuration;
    }

    public Websocket getWebsocket() {
        return websocket;
    }

    public void setWebsocket(Websocket websocket) {
        this.websocket = websocket;
    }

    public CallDetailRecordService getCallDetailRecordService() {
        return callDetailRecordService;
    }

    public void setCallDetailRecordService(CallDetailRecordService callDetailRecordService) {
        this.callDetailRecordService = callDetailRecordService;
    }

    public CallingOutService getCallingOutService() {
        return callingOutService;
    }

    public void setCallingOutService(CallingOutService callingOutService) {
        this.callingOutService = callingOutService;
    }

    public TextToSpeechService getTextToSpeechService() {
        return textToSpeechService;
    }

    public void setTextToSpeechService(TextToSpeechService textToSpeechService) {
        this.textToSpeechService = textToSpeechService;
    }

    public AgentCallService getAgentCallService() {
        return agentCallService;
    }

    public void setAgentCallService(AgentCallService agentCallService) {
        this.agentCallService = agentCallService;
    }

    public AgentStatsService getAgentStatsService() {
        return agentStatsService;
    }

    public void setAgentStatsService(AgentStatsService agentStatsService) {
        this.agentStatsService = agentStatsService;
    }

    public InboundCallService getInboundCallService() {
        return inboundCallService;
    }

    public void setInboundCallService(InboundCallService inboundCallService) {
        this.inboundCallService = inboundCallService;
    }

    public GCESignedUtility getGcssurl() {
        return gcssurl;
    }

    public void setGcssurl(GCESignedUtility gcssurl) {
        this.gcssurl = gcssurl;
    }

    public Incoming2 getIncoming2() {
        return incoming2;
    }

    public void setIncoming2(Incoming2 incoming2) {
        this.incoming2 = incoming2;
    }

    public DialerQueueRecordService getDialerQueueRecordRepository() {
        return dialerQueueRecordRepository;
    }

    public void setDialerQueueRecordRepository(DialerQueueRecordService dialerQueueRecordRepository) {
        this.dialerQueueRecordRepository = dialerQueueRecordRepository;
    }

    public FreeswitchService getFreeswitchService() {
        return freeswitchService;
    }

    public void setFreeswitchService(FreeswitchService freeswitchService) {
        this.freeswitchService = freeswitchService;
    }

    public DialerQueueService getDialerQueueService() {
        return dialerQueueService;
    }

    public void setDialerQueueService(DialerQueueService dialerQueueService) {
        this.dialerQueueService = dialerQueueService;
    }
    
    

}
