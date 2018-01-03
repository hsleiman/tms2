/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service.freeswitch;

import com.objectbrains.svc.iws.AgentWeightPriority;
import com.objectbrains.svc.iws.CallDispositionCode;
import com.objectbrains.svc.iws.CallRoutingOption;
import com.objectbrains.svc.iws.SvDialerQueueSettings;
import com.objectbrains.svc.iws.SvInboundDialerQueueSettings;
import com.objectbrains.svc.iws.SvOutboundDialerQueueSettings;
import com.objectbrains.svc.iws.SvcException;
import com.objectbrains.svc.iws.TMSService;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.exception.CallNotFoundException;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.hazelcast.entity.AgentWeightedPriority;
import com.objectbrains.tms.service.AgentCallService;
import com.objectbrains.tms.service.AgentQueueAssociationService;
import com.objectbrains.tms.service.AgentService;
import com.objectbrains.tms.service.DialerQueueRecordService;
import com.objectbrains.tms.service.DialplanService;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import com.objectbrains.tms.service.dialer.CallService;
import com.objectbrains.tms.service.dialer.Dialer;
import com.objectbrains.tms.service.dialer.DialerService;
import com.objectbrains.tms.utility.HttpClient;
import com.objectbrains.tms.websocket.Websocket;
import com.objectbrains.tms.websocket.message.outbound.PhoneToType;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service
public class CallingOutService {

    private static final Logger log = LoggerFactory.getLogger(CallingOutService.class);

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private DialerService dialerService;

    @Autowired
    private CallService callService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentCallService agentCallService;

    @Autowired
    private DialerQueueRecordService recordService;

    @Autowired
    private DialplanService dialplanRepository;

    @Autowired
    private TMSService tmsIws;

    @Autowired
    private AgentQueueAssociationService associationService;

    @Autowired
    @Lazy
    private Websocket websocket;
//    @Autowired
//    private TmsLocal tmsLocal;

    @Async
    public void PlaceOriginateToFreedwitchAsyc(String arg, String freeswitchIP) {
        if (freeswitchIP.equals(configuration.getLocalHostAddress())) {
            log.info("Overriding IP From " + freeswitchIP + " to 127.0.0.1 for originate command");
            freeswitchIP = "127.0.0.1";
        }
        log.info("Executing: " + freeswitchIP + " - " + arg);
        try {
            Thread.sleep(50l);
        } catch (InterruptedException ex) {
        }
//        tmsLocal.asyncOriginate(arg);
        try {
            HttpClient.sendPostRequestAsText("http://" + freeswitchIP + ":7070/tms_local/freeswitch/sendAsyncApiCommand/originate", arg);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        log.info("Executed: " + freeswitchIP + " - " + arg);
    }

    @Async
    public void PlaceFreeswitchCommandAsyc(String method, String arg, String freeswitchIP) {
        PlaceFreeswitchCommand(method, arg, freeswitchIP);
    }

    public void PlaceFreeswitchCommand(String method, String arg, String freeswitchIP) {
        if (freeswitchIP.equals(configuration.getLocalHostAddress())) {
            log.info("Overriding IP From " + freeswitchIP + " to 127.0.0.1 for originate command");
            freeswitchIP = "127.0.0.1";
        }
        log.info("Executing: " + freeswitchIP + " - " + arg);
        try {
            Thread.sleep(50l);
        } catch (InterruptedException ex) {
        }
//        tmsLocal.asyncOriginate(arg);
        try {
            HttpClient.sendPostRequestAsText("http://" + freeswitchIP + ":7070/tms_local/freeswitch/sendAsyncApiCommand/" + method, arg);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        log.info("Executed: " + freeswitchIP + " - " + arg);
    }

    public void PlaceFreeswitchCommandSync(String method, String arg, String freeswitchIP) {
        if (freeswitchIP.equals(configuration.getLocalHostAddress())) {
            log.info("Overriding IP From " + freeswitchIP + " to 127.0.0.1 for originate command");
            freeswitchIP = "127.0.0.1";
        }
        log.info("Executing: " + freeswitchIP + " - " + arg);
        try {
            Thread.sleep(50l);
        } catch (InterruptedException ex) {
        }
//        tmsLocal.asyncOriginate(arg);
        try {
            HttpClient.sendPostRequestAsText("http://" + freeswitchIP + ":7070/tms_local/freeswitch/sendSyncApiCommand/" + method, arg);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        log.info("Executed: " + freeswitchIP + " - " + arg);
    }
    
    @Async
    public void PlaceShellCommandAsync(String arg, String freeswitchIP) {
        PlaceShellCommand(arg, freeswitchIP);
    }

    public void PlaceShellCommand(String arg, String freeswitchIP) {
        if (freeswitchIP.equals(configuration.getLocalHostAddress())) {
            log.info("Overriding IP From " + freeswitchIP + " to 127.0.0.1 for originate command");
            freeswitchIP = "127.0.0.1";
        }
        log.info("Executing: " + freeswitchIP + " - " + arg);
        try {
            Thread.sleep(50l);
        } catch (InterruptedException ex) {
        }
//        tmsLocal.asyncOriginate(arg);
        try {
            HttpClient.sendPostRequestAsText("http://" + freeswitchIP + ":7070/tms_local/freeswitch/runShellCommand/tms_shell/", arg);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        log.info("Executed: " + freeswitchIP + " - " + arg);
    }

    @Async
    public void callEndedForAgent(Integer ext, String call_uuid, CallDispositionCode dispositionCode, Long queuePK) {
        log.info("Agent Call Service Call Ended For Agent: [{}] [{}]", ext, call_uuid);
        dialplanRepository.LogDialplanInfoIntoDb(call_uuid, "Agent Call Service Call Ended For Agent: [{}] [{}]", ext, call_uuid);
        try {
            Thread.sleep(50l);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage());
        }
        agentCallService.callEnded(ext, call_uuid);
        if (dispositionCode != null) {
            callEndedAsync(call_uuid, dispositionCode, queuePK);
        }
    }

    @Async
    public void callDropped(String callUUID, long waitTimeMillis, CallDispositionCode dispositionCode) {
        log.info("Dialer Call Dropped: [{}] [{}]", dispositionCode.getDescription(), callUUID);
        dialplanRepository.LogDialplanInfoIntoDb(callUUID, "Dialer Call Dropped: [{}] [{}]", dispositionCode.getDescription(), callUUID);
        try {
            Thread.sleep(50l);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage());
        }
        dialerService.callDropped(callUUID, waitTimeMillis, dispositionCode);
    }

    @Async
    public void callRespondedAsync(String callUUID, long responseTimeMillis, Dialer.CallRespondedCallback callback) {
        log.info("Dialer Device Call Responded: [{}] [{}]", callUUID, responseTimeMillis);
        dialplanRepository.LogDialplanInfoIntoDb(callUUID, "Dialer Device Call Responded: [{}] [{}]", callUUID, responseTimeMillis);
        try {
            Thread.sleep(50l);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage());
        }
        dialerService.callResponded(callUUID, responseTimeMillis, callback);
    }

    @Async
    public void callEndedAsync(String callUUID, CallDispositionCode dispositionCode, Long queuePK) {
        log.info("Dialer Device Call Ended: [{}] [{}] [{}]", dispositionCode.getDescription(), dispositionCode.getDispositionId(), callUUID);
        dialplanRepository.LogDialplanInfoIntoDb(callUUID, "Dialer Device Call Ended: [{}] [{}] [{}]", dispositionCode.getDescription(), dispositionCode.getDispositionId(), callUUID);
        try {
            Thread.sleep(50l);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage());
        }
        dialerService.callEnded(callUUID, dispositionCode);
        if (queuePK != null) {
            callService.removeWaitingCall(queuePK, callUUID);
        }
    }

    private Boolean filterAgent(Agent next) {

        return websocket.checkAgentExt(next.getExtension());

    }

    @Async
    public void putCallOnWaitForTransferCallAsync(Long queuePK, String callUUID, PhoneToType phone, Long loanPk, Integer originalExtToExclude) {
        try {
            Thread.sleep(100l);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage());
        }
        if (originalExtToExclude == null) {
            originalExtToExclude = 0;
        }

        log.info("Creating Transfer call for Queue {} for call uuid {}", queuePK, callUUID);
        callService.putCallOnWait(queuePK, callUUID, loanPk, phone, 1010);

        List<AgentWeightPriority> awpList;
        try {
            awpList = tmsIws.getAgentWeightPriorityListForDq(queuePK);
        } catch (SvcException | RuntimeException ex) {
            log.error("Could not get agents for queue {}", queuePK, ex);
            return;
        }

        List<Agent> agents = agentService.getAgents(awpList, null, null);

        log.info("Creating Transfer call for Agents for call uuid {}", callUUID);
        for (Agent agent : agents) {
            if (!Objects.equals(originalExtToExclude, agent.getExtension())) {
                callService.addPrimaryCall(agent.getExtension(), queuePK, callUUID);
            }
        }

    }

    @Async
    public void putCallOnWaitForDialerAsync(long queuePk, String callUUID, Long loanId, PhoneToType phoneToTypes, long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage());
        }
        callService.putCallOnWait(queuePk, callUUID, loanId, phoneToTypes);
    }

    @Async
    public void putCallOnWaitAsync(long queuePk, String callUUID, Long loanId, CallDirection callDirection, boolean autoDialed, PhoneToType phoneToTypes, long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage());
        }
        Map<Integer, AgentWeightedPriority> map = associationService.getParticipatingAgents(queuePk, callDirection, autoDialed);
        log.info("putCallOnWaitAsync {}, {} isEmpty: {}", callUUID, loanId, map.isEmpty());
        dialplanRepository.LogDialplanInfoIntoDb(callUUID, "putCallOnWaitAsync {}, {} isEmpty: {}", callUUID, loanId, map.isEmpty());

        if (!map.isEmpty()) {
            SvDialerQueueSettings queueSettings = recordService.getQueueSettings(queuePk);

            boolean isInbound = false;
            CallRoutingOption order = CallRoutingOption.ROUND_ROBIN;
            if (queueSettings instanceof SvInboundDialerQueueSettings) {
                order = ((SvInboundDialerQueueSettings) queueSettings).getCallRoutingOption();
                isInbound = true;
            }
            com.objectbrains.svc.iws.WeightedPriority defaultWeightedPriorioty = null;
            if (queueSettings != null) {
                defaultWeightedPriorioty = queueSettings.getWeightedPriority();
            }
            log.info("putCallOnWaitAsync {}, {} isisInbound: {}", callUUID, loanId, isInbound);
            dialplanRepository.LogDialplanInfoIntoDb(callUUID, "putCallOnWaitAsync {}, {} isisInbound: {}", callUUID, loanId, isInbound);
            List<Agent> agents = (agentService.getAgents(map, defaultWeightedPriorioty, order));
            try {
                for (Agent agent : agents) {
                    if (isInbound) {
                        log.info("callService.connectInboundCallToAgent {}, {} isisInbound: {} agentExt: {}", callUUID, loanId, isInbound, agent.getExtension());
                        dialplanRepository.LogDialplanInfoIntoDb(callUUID, "callService.connectInboundCallToAgent {}, {} isisInbound: {} agentExt: {}", callUUID, loanId, isInbound, agent.getExtension());
                        if (filterAgent(agent) && callService.connectInboundCallToAgent(agent.getExtension(), callUUID, (SvInboundDialerQueueSettings) queueSettings, loanId, phoneToTypes)) {
                            log.info("callService.connectInboundCallToAgent {}, {} agentExt: {} true", callUUID, loanId, agent.getExtension());
                            dialplanRepository.LogDialplanInfoIntoDb(callUUID, "callService.connectInboundCallToAgent {}, {} agentExt: {} true", callUUID, loanId, agent.getExtension());
                            return;
                        }
                    } else {
                        log.info("callService.connectInboundCallToAgent {}, {} isisInbound: {} agentExt: {}", callUUID, loanId, isInbound, agent.getExtension());
                        dialplanRepository.LogDialplanInfoIntoDb(callUUID, "callService.connectInboundCallToAgent {}, {} isisInbound: {} agentExt: {}", callUUID, loanId, isInbound, agent.getExtension());
                        if (filterAgent(agent) && callService.connectOutboundCallToAgent(agent.getExtension(), callUUID, (SvOutboundDialerQueueSettings) queueSettings, loanId, phoneToTypes)) {
                            log.info("callService.connectInboundCallToAgent {}, {} agentExt: {} true", callUUID, loanId, agent.getExtension());
                            dialplanRepository.LogDialplanInfoIntoDb(callUUID, "callService.connectInboundCallToAgent {}, {} agentExt: {} true", callUUID, loanId, agent.getExtension());
                            return;
                        }
                    }
                }
            } catch (CallNotFoundException ex) {
                return;
            }
        }
        callService.putCallOnWait(queuePk, callUUID, loanId, phoneToTypes);
    }

    @Async
    public void InvokTMSLocal(String method, String body, String freeswitchIP) {
        if (freeswitchIP.equals(configuration.getLocalHostAddress())) {
            log.info("Overriding IP From " + freeswitchIP + " to 127.0.0.1 for originate command");
            freeswitchIP = "127.0.0.1";
        }
        log.info("Executing: " + freeswitchIP + " - " + method);
        try {
            Thread.sleep(75l);
        } catch (InterruptedException ex) {
        }
//        tmsLocal.asyncOriginate(arg);
        try {
            HttpClient.sendPostRequestAsText("http://" + freeswitchIP + ":7070/tms_local/freeswitch/" + method, body);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        log.info("Executed: " + freeswitchIP + " - " + method);
    }

    @Async(value = "tms-recordingUpload")
    public void InvokRecordingUpload(String callUUID, String ip, String data) {
        try {

            String http = "http://" + ip + ":7070/recording-upload/upload" + data;
            log.info("Executing Recording Upload: " + http);
            HttpClient.sendGetRequestAsText(http);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        log.info("Executed Recording Upload: " + data);
    }

    @Async(value = "tms-recordingUpload")
    public void InvokRecordingUploadAndTranslate(String callUUID, String ip, String data) {
        try {

            String http = "http://" + ip + ":7070/recording-upload/upload" + data;

            if (configuration.enableSpeechToTextTranslationWhileUploadingRecoding()) {
                http = "http://" + ip + ":7070/recording-upload/upload_and_translate" + data;
            }

            log.info("Executing Recording Upload: " + http);
            HttpClient.sendGetRequestAsText(http);

            if (configuration.enableSpeechToTextTranslationWhileUploadingRecoding() == false) {
                http = "http://" + configuration.getSpeechToTextTranslationOffloadingServerIP() + ":7070/recording-upload/translate-recording-from-bucket" + data;
                log.info("Executing Recording Transcript Upload: " + http);
                HttpClient.sendGetRequestAsText(http);
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        log.info("Executed Recording Upload: " + data);
    }
}
