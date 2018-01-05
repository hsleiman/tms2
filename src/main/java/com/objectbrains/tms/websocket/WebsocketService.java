/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.websocket;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.sti.db.entity.disposition.CallDispositionCode;
import com.objectbrains.sti.embeddable.AgentWeightPriority;
import com.objectbrains.sti.pojo.CallDispositionLogData;
import com.objectbrains.sti.pojo.PhoneNumberAccountData;
import com.objectbrains.sti.pojo.UserData;
import com.objectbrains.sti.service.dialer.DialerQueueService;
import com.objectbrains.sti.service.tms.CallDispositionService;
import com.objectbrains.sti.service.tms.TMSService;
import com.objectbrains.tms.db.entity.Chat;
import com.objectbrains.tms.db.entity.cdr.CallDetailRecordTMS;
import com.objectbrains.tms.db.repository.CallDetailRecordRepository;
import com.objectbrains.tms.db.repository.CdrRepository;
import com.objectbrains.tms.db.repository.DialplanRepository;
import com.objectbrains.tms.db.repository.WebsocketRepository;
import com.objectbrains.tms.enumerated.AgentState;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.DialerActiveStatus;
import com.objectbrains.tms.enumerated.PhoneStatus;
import static com.objectbrains.tms.enumerated.SetAgentState.IDLE;
import com.objectbrains.tms.freeswitch.pojo.FreeswitchCommand;
import com.objectbrains.tms.hazelcast.ListAdapter;
import com.objectbrains.tms.hazelcast.entity.AgentTMS;
import com.objectbrains.tms.hazelcast.entity.AgentCall;
import com.objectbrains.tms.hazelcast.entity.AgentWeightedPriority;
import com.objectbrains.tms.pojo.AgentStatus;
import com.objectbrains.tms.pojo.PostChatBody;
import com.objectbrains.tms.service.AgentCallService;
import com.objectbrains.tms.service.TMSAgentService;
import com.objectbrains.tms.service.AgentStatsService;
import com.objectbrains.tms.service.CallDetailRecordService;
import com.objectbrains.tms.service.CdrService;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import com.objectbrains.tms.service.FreeswitchService;
import com.objectbrains.tms.service.InboundCallService;
import com.objectbrains.tms.service.TransferService;
import com.objectbrains.tms.service.Utils;
import com.objectbrains.tms.service.dialer.CallService;
import com.objectbrains.tms.service.freeswitch.PhoneOperationService;
import com.objectbrains.tms.utility.HttpClient;
import com.objectbrains.tms.websocket.message.FreeswitchCheck;
import com.objectbrains.tms.websocket.message.Function;
import com.objectbrains.tms.websocket.message.PhoneCheck;
import com.objectbrains.tms.websocket.message.PhoneCheckData;
import com.objectbrains.tms.websocket.message.PlayPrompt;
import com.objectbrains.tms.websocket.message.inbound.CallDisposition;
import com.objectbrains.tms.websocket.message.inbound.LockNextAvailable;
import com.objectbrains.tms.websocket.message.inbound.Recieve;
import com.objectbrains.tms.websocket.message.outbound.CallRecent;
import com.objectbrains.tms.websocket.message.outbound.CallSipHeader;
import com.objectbrains.tms.websocket.message.outbound.ChangeFreeswitchIp;
import com.objectbrains.tms.websocket.message.outbound.PhoneToType;
import com.objectbrains.tms.websocket.message.outbound.PushNotification;
import com.objectbrains.tms.websocket.message.outbound.Send;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
@Service
public class WebsocketService {

    private static final Logger LOG = LoggerFactory.getLogger(WebsocketService.class);

    @Autowired
    private PhoneOperationService phoneOperationService;

    @ConfigContext
    private ConfigurationUtility config;

    @Autowired
    private TMSAgentService agentService;

    @Autowired
    private AgentCallService agentCallService;

    @Autowired
    private DialplanRepository dialplanRepository;

    @Autowired
    private AgentStatsService agentStatsService;

    @Autowired
    private CdrRepository cdrRepository;

    @Autowired
    private CdrService cdrService;

    @Autowired
    private CallDetailRecordRepository callDetailRecordRepository;

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private TMSService tmsIws;
    
    @Autowired
    private DialerQueueService dialerQueueService;
    
    @Autowired
    private CallDispositionService callDispositionService;

    @Autowired
    private CallService callService;

    @Autowired
    private WebsocketCache websocketCache;

    @Autowired
    @Lazy
    private InboundCallService inboundCallService;

    @ConfigContext
    private WebsocketConfig websocketConfig;

    @Autowired
    private WebsocketRepository websocketRepository;

    @Autowired
    private FreeswitchService freeswitchService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private FreeswitchConfiguration freeswitchConfiguration;

    @Autowired
    @Lazy
    private Websocket websocket;

    @Scheduled(initialDelay = 30000, fixedRate = 30000)
    public void keepAlive() {
        for (Integer key : websocket.getConnectedExtensions()) {
            Send send = new Send(Function.KEEP_ALIVE);
            websocket.sendWithRetry(key, send);
        }
    }

    public void refresh(Integer ext) {
        Send send = new Send(Function.Refresh);
        websocket.sendWithRetry(ext, send);
    }

    @Async
    public void refreshDelay(Integer ext) {
        try {
            Thread.sleep(20l);
        } catch (InterruptedException ex) {
        }
        refresh(ext);
    }

    @Async
    public void sendPushNotification(List<AgentTMS> agents, String msg) {
        if (websocketConfig.enablePushNotification() == false) {
            return;
        }
        for (int i = 0; i < agents.size(); i++) {
            AgentTMS agent = agents.get(i);
            sendPushNotification(agent.getExtension(), msg);
        }
    }

    public void sendPushNotification(int ext, String msg) {
        if (websocketConfig.enablePushNotification() == false) {
            return;
        }
        Send send = new Send(Function.PUSH_NOTIFICATION);
        PushNotification pushNotification = new PushNotification();
        pushNotification.setMsg(msg);
        send.setPushNotification(pushNotification);
        websocket.sendWithRetry(ext, send);
    }

    @Async
    public void sendUpdateOfDirectory() {
        websocket.sendToAll(new SendUpdateOfDirectoryTask(phoneOperationService.getAgentDirectory()));
    }

    @Async
    public void handleMessage(int ext, Recieve recieve) {
        try {
            switch (recieve.getFunction()) {
                case CHAT:

                    Chat chat = recieve.getChat();
                    if (chat.getIsBorrower()) {
                        sendChatMessageToBorrower(ext, chat);
                    } else {
                        sendChatMessage(ext, chat);
                    }
                    break;

//                case Play_PROMOTS:
//                    try { 10.240.0.83
//
//                        Prompts prompt = recieve.getPrompt();
//                        sendPushNotification(ext, prompt.getAmount().toString());
//
//                    } catch (NumberFormatException | NullPointerException ex) {
//                        LOG.error("Exception {}", ex);
//                    }
//                    break;
                case PHONE_CHECK:
                    PhoneCheck phoneCheck = recieve.getPhoneCheck();
                    phoneCheck.setStatus(1);
                    Long phoneNumber = null;

                    String outboundCallUUID = UUID.randomUUID().toString();
                    phoneCheck.setCallUUID(outboundCallUUID);

                    String phoneStr = phoneCheck.getPhoneNumber();

                    if (phoneStr.length() <= 4) {
                        phoneCheck.setCount(0);
                        Integer otherEndExt = Integer.parseInt(phoneStr);
                        if (agentService.agentExists(otherEndExt) == false) {
                            phoneCheck.setStatus(-1);
                        } else {
                            AgentTMS agent = agentService.getAgent(otherEndExt);
                            // AgentState agentState = agentStatsService.getAgentState(otherEndExt);
                            if (agent != null) {
                                if (agent.getStatusExt() != IDLE) {
                                    phoneCheck.setStatus(-2);
                                }
                            } else {
                                phoneCheck.setStatus(-1);
                            }
                        }
                    } else {
                        try {
                            if (phoneStr.length() > 10) {
                                phoneStr = phoneStr.substring(phoneStr.length() - 10);
                            }
                            phoneNumber = Long.parseLong(phoneStr);
                        } catch (NumberFormatException ex) {
                            phoneCheck.setCount(0);
                        }
                    }
                    if (phoneNumber != null) {
                        List<PhoneNumberAccountData> pnld = tmsIws.getAccountsForPhoneNumber(phoneNumber);
                        phoneCheck.setCount(pnld.size());

                        List<PhoneCheckData> borrowerInfoList = new ArrayList<>();
                        for (PhoneNumberAccountData data : pnld) {
                            PhoneCheckData borrowerInfo = new PhoneCheckData();
                            borrowerInfo.setBorrowerFirstName(data.getFirstName());
                            borrowerInfo.setBorrowerLastName(data.getLastName());
                            borrowerInfo.setLoanId(data.getLoanPk());
                            borrowerInfo.setDnc(data.getDoNotCall());
                            borrowerInfo.setBorrowerPhoneNumber(phoneNumber.toString());
                            borrowerInfoList.add(borrowerInfo);
                        }
                        phoneCheck.setList(borrowerInfoList);
                        if (websocketConfig.enableCallStartOnPhoneCheck()) {
                            agentCallService.callStarted(ext, outboundCallUUID, null, false, null, CallDirection.OUTBOUND, null, null, false, websocketConfig.callStartOnPhoneCheckExpireTime());
                        }
                    }

                    Send sendPhoneCheck = new Send(Function.PHONE_CHECK);
                    sendPhoneCheck.setPhoneCheck(phoneCheck);
                    websocket.sendWithRetry(ext, sendPhoneCheck);
                    break;
                case PLAY_PROMPT:
                    LOG.info("Playing prompt for ext {}", ext);

                    if (websocketConfig.enablePlayPromptAll()) {
                        LOG.info("Playing prompt for ext {} is enabled", ext);

                        PlayPrompt prompt = recieve.getPlayPrompt();

                        if (websocketConfig.enablePlayPromptCustom(prompt.getPromptType())) {
                            LOG.info("Playing prompt for ext {} is enabled for prompt type {}", ext, prompt.getPromptType());
                            AgentCall agentCall = agentCallService.getActiveCall(ext);
                            AgentTMS agent = agentService.getAgent(ext);

                            if (agentCall == null || agentCall.getAgentFreeswitchUUID() == null || agent == null || agent.getFreeswitchIP() == null) {
                                LOG.info("agentCall == {} || agentCall.getAgentFreeswitchUUID() == null || agent == {} || agent.getFreeswitchIP() == null", agentCall, agent);
                                break;
                            }
                            
                            LOG.info("Playing prompt for ext {} - {}", ext, prompt.toJson());
//                            String channalB;
                            //channalB = agentService.getOtherChannalBForChannalA(agentCall.getAgentFreeswitchUUID());

                            prompt.setFreeswitchChannalId(agentCall.getAgentFreeswitchUUID());
                            prompt.setFreeswitchIp(agent.getFreeswitchIP());
                            prompt.setExtOnCall(ext);

                            ArrayList<FreeswitchCommand> list = new ArrayList<>();
                            try {
                                if (null != websocketConfig.getPromptPlayType(prompt.getPromptType())) {
                                    LOG.info("Playing prompt for ext {} is enabled for prompt method {}", ext, websocketConfig.getPromptPlayType(prompt.getPromptType()));
                                    switch (websocketConfig.getPromptPlayType(prompt.getPromptType())) {
                                        case 0:
                                            LOG.info("Playing prompt for ext {} is case 0", ext);
                                            list = prompt.buildWithBroadCast();
                                            break;
                                        default:
                                            break;
                                    }
                                }
                                if (list.isEmpty() == false) {
                                    phoneOperationService.playPrompts(list);
                                }
                            } catch (Exception ex) {
                                LOG.error("Exception {}", ex);
                            }

                        }
                    }
                    break;

                case AGENT_GROUP:
                    Send sendAgentGroup = new Send(Function.AGENT_GROUP);
                    sendAgentGroup.setDialerQueueDetailPojos(websocketCache.getDialerQueueDetailPojos());
                    //sendAgentGroup.setDialerGroups(tmsIws.getAllDialerGroups());
                    websocket.sendWithRetry(ext, sendAgentGroup);
                    break;

                case SpeechToText:
                    //LOG.info(ext+" -> TextSpeech: " + recieve.getSpeechToText().getText());
                    callDetailRecordService.addSpeechToText(ext, recieve.getSpeechToText());
                    break;
                case Payment:
                    callDetailRecordRepository.addPayment(ext, recieve);
                    break;
                case PTP:
                    callDetailRecordRepository.addPTP(ext, recieve);
                    break;
                case Phone: {
                    LOG.debug("Phone update: {}", recieve);
                    PhoneStatus phoneStatus = recieve.getPhone().getStatus();
                    CallDisposition disposition = recieve.getCallDisposition();
                    Long dispositionId = null;
                    String log = null;
                    if (disposition != null) {
                        dispositionId = disposition.getDispositionPk();
                        log = disposition.getLog();
                    }
                    AgentCall call = agentCallService.updateCallState(ext, recieve.getCall_uuid(), phoneStatus, recieve.getPhone(), dispositionId);
                    callDetailRecordService.updateCallState(recieve.getCall_uuid(), phoneStatus, dispositionId);
//                    if (phoneStatus == PhoneStatus.WRAP) {
//                        CallDetailRecordTMS mcdr = callDetailRecordService.getCDR(recieve.getCall_uuid());
//                        cdrService.offerNewMasterCallDetailRecordToSVCONLY(mcdr);
//
//                        if (log != null) {
//                            sendDispositionLogToSvc(ext, recieve.getCall_uuid(), dispositionId, log, call);
//                        }
//                    }
                    break;
                }
                case FREESWITCH_CHECK:

                    FreeswitchCheck check = recieve.getFreeswitchCheck();
                    Send sendFreeswitchCheck = new Send(Function.FREESWITCH_CHECK);
                    FreeswitchCheck freeswitchCheck = new FreeswitchCheck();
                    freeswitchCheck.setExt(check.getExt());
                    freeswitchCheck.setExtCheck(websocket.checkAgentExt(check.getExt()));
                    freeswitchCheck.setIsRegisterd(freeswitchService.isRegisteredOnFreeswitch(check.getExt()));
                    sendFreeswitchCheck.setFreeswitchCheck(freeswitchCheck);
                    websocket.sendWithRetry(ext, sendFreeswitchCheck);

                    break;
                case SET_AGENT_OFFLINE_STATE:
                case SET_AGENT_STATE:
                    agentService.setAgentState(ext, recieve.getAgentState());
                    refresh(ext);
                    break;
                case RESET_AGENT_STATUS_TO_IDLE:
                    AgentStatus agentStatus = agentService.getAgentStatus(ext);

                    LOG.info("RESET_AGENT_STATUS_TO_IDLE: {} - {}", ext, agentStatus.getPhoneActive());

                    if (agentStatus.getPhoneActive() == AgentState.ONCALL) {
                        if (config.getBoolean("reset.agent.to.idle.if.stuck", Boolean.FALSE)) {
                            LOG.info("RESET_AGENT_STATUS_TO_IDLE: {} - {} -> IDLE", ext, agentStatus.getPhoneActive());

                            AgentCall agentCall = agentCallService.getActiveCall(ext);
                            agentCallService.callEnded(ext, agentCall.getCallUUID());
                            agentStatsService.setAgentToIdle(ext);
                        }
                    }
                case AGENT_STATUS:
                    Send send = new Send(Function.AGENT_STATUS);
                    send.setValueCheckVersion(recieve.getValueCheckVersion());
                    send.setAgentStatus(agentService.getAgentStatus(ext));
                    websocket.sendWithRetry(ext, send);
                    break;
                case SET_AGENT_DIALER_ACTIVE_STATUS:
                    agentStatsService.setDialerActive(ext, recieve.getDialerActiveStatus() == DialerActiveStatus.ACTIVE);
                    break;
                case CALL_RECENT:
                    Send sendRecent = new Send(Function.CALL_RECENT);
                    sendRecent.setCallRecent(new CallRecent(cdrRepository.getAgentCallHistory(ext, 0, 30)));
                    websocket.sendWithRetry(ext, sendRecent);
                    break;
                case ATTACH_LOAN_TO_CALL:
                    if (config.getBoolean("enable.attach.loan.to.call", Boolean.TRUE)) {
                        LOG.info("Attaching loan {} to call {} ", recieve.getAttachLoanToCallUUID().getLoanId(), recieve.getCall_uuid());
                        dialplanRepository.logDialplanInfoIntoDb(recieve.getCall_uuid(), "Attaching loan {} to call {} ", recieve.getAttachLoanToCallUUID().getLoanId(), recieve.getCall_uuid());
                        CallDetailRecordTMS mcdr = callDetailRecordService.getCDR(recieve.getCall_uuid());
                        dialplanRepository.logDialplanInfoIntoDb(recieve.getCall_uuid(), "Should Attach loan xxx {},{},{}", recieve.getAttachLoanToCallUUID().getLoanId(), mcdr.getBorrowerInfo().getLoanId(), recieve.getCall_uuid());
                        if (mcdr.getBorrowerInfo().getLoanId() == null || mcdr.getBorrowerInfo().getLoanId() == 0) {
                            LOG.info("Attached loan {} to call {} ", recieve.getAttachLoanToCallUUID().getLoanId(), recieve.getCall_uuid());
                            LOG.info("Attached loan {} to call {} ", recieve.getAttachLoanToCallUUID().getLoanId(), recieve.getCall_uuid());
                            dialplanRepository.logDialplanInfoIntoDb(recieve.getCall_uuid(), "Attached loan {} to call {} ", recieve.getAttachLoanToCallUUID().getLoanId(), recieve.getCall_uuid());
                            callDetailRecordService.updateLoanId(recieve.getCall_uuid(), recieve.getAttachLoanToCallUUID().getLoanId());
                        }
                    } else {
                        CallDetailRecordTMS mcdr = callDetailRecordService.getCDR(recieve.getCall_uuid());
                        if (mcdr != null && mcdr.getBorrowerInfo() != null) {
                            LOG.info("Should Attached loan {} from {} to call {} ", recieve.getAttachLoanToCallUUID().getLoanId(), mcdr.getBorrowerInfo().getLoanId(), recieve.getCall_uuid());
                            dialplanRepository.logDialplanInfoIntoDb(recieve.getCall_uuid(), "Should Attached loan {} from {} to call {} ", recieve.getAttachLoanToCallUUID().getLoanId(), mcdr.getBorrowerInfo().getLoanId(), recieve.getCall_uuid());
                            dialplanRepository.logDialplanInfoIntoDb(recieve.getCall_uuid(), "Should Attached loan xxx {},{},{}", recieve.getAttachLoanToCallUUID().getLoanId(), mcdr.getBorrowerInfo().getLoanId(), recieve.getCall_uuid());
                        }
                    }
                    break;
                case LOG_SERVER_IP:
                    Send sendLogServerIP = new Send(Function.LOG_SERVER_IP);
                    sendLogServerIP.setLogServerIPAddress(websocketConfig.getLoggingServerIPForExt());
                    websocket.sendWithRetry(ext, sendLogServerIP);
                    break;

                case OPERATOR:
                    switch (recieve.getOperator().getOperation()) {
                        case THREE_WAY_CALL:
                            phoneOperationService.threeWayCall(recieve.getCall_uuid(), recieve.getOperator().getCallee(), recieve.getOperator().getOnCall());
                            break;
                        case EAVSDROP_ON_CALL:
                            phoneOperationService.eavsdropOnCall(recieve.getCall_uuid(), recieve.getOperator().getCallee(), recieve.getOperator().getOnCall());
                            break;
                        case WHISPER_ON_CALL:
                            phoneOperationService.whisperOnCall(recieve.getCall_uuid(), recieve.getOperator().getCallee(), recieve.getOperator().getOnCall());
                            break;
                    }
                    break;
                case LOCK_NEXT_AVAILABLE_CANCEL: {

                    String internalCallUUID = recieve.getLockNextAvailable().getCallUUID();
                    String lockedToExtStr = recieve.getLockNextAvailable().getLockedToExt();
                    Integer lockedToExt = null;
                    try {
                        lockedToExt = Integer.parseInt(lockedToExtStr);
                    } catch (NumberFormatException ex) {
                        LOG.info("Could not parse Locked EXT {}.", lockedToExtStr);
                    }

                    String orginalCallUUID = null;

                    LOG.info("Canceling Transfer call to Agent for call uuid Internal: {} - LockedToExt: {}", internalCallUUID, lockedToExt);

                    if (internalCallUUID == null) {

                        orginalCallUUID = recieve.getCall_uuid();
                        LOG.info("Internal CallUUID is NULL trying to find by using the original CallUUID: {}", orginalCallUUID);
                        if (orginalCallUUID != null) {
                            internalCallUUID = transferService.getTransferCallUUIDForOriginalCallUUID(orginalCallUUID);
                        }
                    }

                    if (orginalCallUUID == null) {
                        orginalCallUUID = transferService.getTransferCallUUIDForInternalCallUUID(internalCallUUID);
                    }

                    LOG.info("Canceling Transfer call to Agent for call uuid Origianl: {} - Internal: {} - LockedToExt: {}", orginalCallUUID, internalCallUUID, lockedToExt);
                    if (lockedToExt == null || lockedToExt == -1) {
                        LOG.info("Canceling transfer call to agent Lock Ext was NULL checking memory for Origianl: {} - Internal: {}", orginalCallUUID, internalCallUUID);
                    } else {
                        agentCallService.callTransfered(lockedToExt, internalCallUUID);
                        agentCallService.callEnded(lockedToExt, internalCallUUID);
                    }

                    Long queue = callService.getAgentTransferQueueMap(internalCallUUID);
                    if (queue != null) {
                        callService.removeWaitingCall(queue, internalCallUUID);
                        LOG.info("Canceling Transfer call to Agent for call uuid {}", internalCallUUID);
                        dialplanRepository.logDialplanInfoIntoDb(internalCallUUID, "Canceling Transfer call to Agent for call uuid {}", internalCallUUID);
                    }
                    break;
                }
                case LOCK_NEXT_AVAILABLE: {
                    String internalCallUUID = UUID.randomUUID().toString();
                    LockNextAvailable avail = recieve.getLockNextAvailable();

                    send = new Send(Function.LOCK_NEXT_AVAILABLE);
                    send.setOriginalTransferCallUUID(avail.getCallUUID());
                    send.setInternalTransferCallUUID(internalCallUUID);

                    buildResponse:
                    {
                        List<AgentWeightPriority> awpList;
                        if (avail.getQueuePk() != null) {
                            try {
                                awpList = dialerQueueService.getAgentWeightPriorityListForDq(avail.getQueuePk());
                            } catch (Exception ex) {
                                LOG.error("ext [{}]. Could not get agents for queue {}", ext, avail.getQueuePk(), ex);
                                break buildResponse;
                            }
                        } else {
                            LOG.error("ext [{}]. Failed to LOCK_NEXT_AVAILABLE, neither a groupPk or queuePk was provided.", ext);
                            break buildResponse;
                        }
                        Map<String, AgentWeightedPriority> awpMap = Utils.convertToMap(awpList);
                        List<AgentTMS> agents = agentService.getAgents(awpList, null, avail.getRoutingOrder());
                        for (AgentTMS agent : agents) {
                            int agentExt = agent.getExtension();
                            LOG.info("Checking agent {}", agentExt);
                            dialplanRepository.logDialplanInfoIntoDb(null, "Checking agent {}", agentExt);

                            //the call direction doesn't really matter here, it just need to not be an Internal call.
                            if (agentExt != ext && inboundCallService.shouldRecieveCall(agentExt, false, CallDirection.INBOUND, false, awpMap.get(agent.getUserName()))) {

                                Boolean isReg = true;
                                if (websocketConfig.enableFreeswitchRegisteredCheck()) {
                                    isReg = freeswitchService.isRegisteredOnFreeswitch(agentExt);
                                }
                                if (websocketConfig.enableExtCheckForLockedAgent()) {
                                    isReg = websocket.checkAgentExt(agentExt);
                                }
                                dialplanRepository.logDialplanInfoIntoDb(null, "Checking agent {} Freeswich Check {}", agentExt, isReg);
                                if (isReg) {
                                    if (agentCallService.callStarted(agentExt, internalCallUUID, null, true, null, CallDirection.INTERNAL, null, null, false, websocketConfig.callTimeoutForInternalCall()) == true) {
                                        agentCallService.callTransferring(agentExt, internalCallUUID);
                                        LOG.info("Agent {} can receive call, locking", agentExt);
                                        dialplanRepository.logDialplanInfoIntoDb(null, "Agent {} can receive call, locking", agentExt);
                                        send.setLockedExtension(agentExt);
                                        transferService.setTransferCallUUIDToCallUUID(internalCallUUID, avail.getCallUUID());
                                        transferService.setTransferCallUUIDToExt(internalCallUUID, agentExt);
                                        break buildResponse;
                                    }
                                }

                            }
                            LOG.info("Agent {} cannot receive call, checking next agent", agentExt);
                            dialplanRepository.logDialplanInfoIntoDb(null, "Agent {} cannot receive call, checking next agent", agentExt);
                        }
                        if (send.getLockedExtension() == null) {

                            CallSipHeader callSipHeader = new CallSipHeader();
                            callSipHeader.setCall_uuid(internalCallUUID);
                            send.setCallSipHeader(callSipHeader);
                            callService.addAgentTransferQueueMap(avail.getQueuePk(), internalCallUUID);
                            transferService.setTransferCallUUIDToCallUUID(internalCallUUID, avail.getCallUUID());

                            LOG.info("Createing Transfer call to Agent for call uuid {}", internalCallUUID);
                            dialplanRepository.logDialplanInfoIntoDb(internalCallUUID, "Createing Transfer call to Agent for call uuid {}", internalCallUUID);
                            Long loanPk = null;//TODO optional
                            PhoneToType phone = null;//TODO might be optional

                            callService.putCallOnWait(avail.getQueuePk(), internalCallUUID, loanPk, phone, ext);
                            for (AgentTMS agent : agents) {
                                if (ext != agent.getExtension()) {
                                    callService.addPrimaryCall(agent.getExtension(), avail.getQueuePk(), internalCallUUID);
                                }
                            }
                        }
                    }

                    websocket.sendWithRetry(ext, send);
                    break;
                }

                case GET_DISPOSITIONS: {
                    List<CallDispositionCode> codes = getCallDispositionCodes(ext, recieve.getCall_uuid());
                    List<CallDisposition> ret = new ListAdapter<>();
                    for (CallDispositionCode code : codes) {
                        ret.add(new CallDisposition(code));
                    }

                    send = new Send(Function.GET_DISPOSITIONS);
                    send.setCallDispositions(ret);
                    websocket.sendWithRetry(ext, send);
                    break;
                }

                default:
                    LOG.error("Unrecognized function type: {}", recieve.getFunction());
                    break;
            }
        } catch (Throwable ex) {
            LOG.error("Woops", ex);
        }
    }

    private List<CallDispositionCode> getCallDispositionCodes(int ext, String callUUID) {
        AgentCall call = agentCallService.getAgentCall(ext, callUUID);
        if (call == null) {
            return callDispositionService.getAllCallDispositionCodes();
        }
        Long loanId = call.getBorrowerInfo().getLoanId();
        if (loanId != null) {
            try {
                return dialerQueueService.getCallDispositionCodesForAccount(loanId, call.getCallDirection() == CallDirection.INBOUND);
            } catch (Throwable ex) {
                LOG.warn("Ext: {}, CallUUID: {}. "
                        + "Unable to get call disposition list for loan {},"
                        + " using queue dispositions instead.", ext, callUUID, loanId, ex);
            }
        }
        Long queuePk = call.getQueuePk();
        if (queuePk != null) {
            try {
                return dialerQueueService.getCallDispositionCodesForQueue(queuePk);
            } catch (Throwable ex) {
                LOG.warn("Ext: {}, CallUUID: {}. "
                        + "Unable to get call disposition list for queue {},"
                        + " using default dispositions instead.", ext, callUUID, queuePk, ex);
            }
        }
        switch (call.getCallDirection()) {
            case INBOUND:
                return callDispositionService.getAllDefaultInboundDispositionCodes();
            case OUTBOUND:
                return callDispositionService.getAllDefaultOutboundDispositionCodes();
            default:
                return Collections.emptyList();
        }

    }

    public int changeFreeswitchIp(String ip) {
        int count = 0;
        LOG.info(ip);
        ChangeFreeswitchIp freeswitchip = new ChangeFreeswitchIp();
        freeswitchip.setFreeswitchIp(ip);
        Send send = new Send(Function.CHANGE_FREESWITCH_IP);
        send.setChangeFreeswitchIp(freeswitchip);
        Set<Integer> agents = websocket.getConnectedExtensions();
        for (Integer i : agents) {
            count++;
            LOG.info("Ext: {}, CallUUID: {}. "
                    + "sending change freeswitch to"
                    + i);
            websocket.sendWithRetry(i, send);
        }
        return count;
    }

    public void sendChatMessage(int ext, Chat chat) {
        try {
            chat.setCreateDateTime(LocalDateTime.now());
            websocketRepository.logChat(ext, chat);

            Send sendChat = new Send(Function.CHAT);
            sendChat.setChat(chat);

            String[] extArray = chat.getTo_ext().split(",");
            for (String string : extArray) {
                Integer to_ext = Integer.parseInt(string);
                websocket.sendWithRetry(to_ext, sendChat);
            }

        } catch (NumberFormatException | NullPointerException ex) {
            LOG.error("Exception {}", ex);
        }
    }

    public void sendChatMessageToBorrower(Integer ext, Chat chat) {
        try {
            chat.setCreateDateTime(LocalDateTime.now());
            websocketRepository.logChat(ext, chat);

            PostChatBody postChatBody = chat.agentToBorrowerChatBuilder();
            String[] callbackIPs = postChatBody.getCallBackIP().split(",");

            for (int i = 0; i < callbackIPs.length; i++) {
                String callbackIP = callbackIPs[i];
                if (callbackIP.equals(freeswitchConfiguration.getLocalHostAddress())) {
                    LOG.info("Overriding IP From " + callbackIP + " to 127.0.0.1 for send Chat Message To Borrower");
                    callbackIP = "127.0.0.1";
                }
                LOG.debug("Executing: " + callbackIP + " - " + postChatBody.toJson());
                HashMap<String,String> headers = new HashMap<>();
                headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                headers.put("apikey", websocketConfig.getMobileApiKey());
                HttpClient.sendPostRequestWithHeaders(callbackIP, postChatBody.toJson(), headers);
                LOG.debug("Executed: " + callbackIP + " - " + postChatBody.toJson());
            }
        } catch (NumberFormatException | NullPointerException | IOException ex) {
            LOG.error("Exception {}", ex);
        }
    }

    public int restartExtension(int ext) {
        int count = 0;
        Send send = new Send(Function.RESTART_EXTENSION);
        if (ext == 0) {
            count++;
            Set<Integer> agents = websocket.getConnectedExtensions();
            for (Integer i : agents) {
                LOG.info("Ext: {}, CallUUID: {}. "
                        + "sending change freeswitch to"
                        + i);
                websocket.sendWithRetry(i, send);
            }
        } else {
            count = 1;
            websocket.sendWithRetry(ext, send);
        }
        return count;
    }

    public int resetFreeswitchIp() {
        int count = 0;
        Send send = new Send(Function.RESET_FREESWITCH_IP);
        count++;
        Set<Integer> agents = websocket.getConnectedExtensions();
        for (Integer i : agents) {
            LOG.info("Ext: {}, CallUUID: {}. "
                    + "sending reset freeswitch to"
                    + i);
            websocket.sendWithRetry(i, send);
        }
        return count;
    }

    private void sendDispositionLogToSvc(int ext, String callUUID, Long dispositionId, String log, AgentCall call) {
        if (log == null) {
            return;
        }
        CallDispositionLogData data = new CallDispositionLogData();
        data.setDispositionId(dispositionId);
        data.setCallUUID(callUUID);
        data.setNote(log);
        if (call == null) {
            call = agentCallService.getAgentCall(ext, callUUID);
        }
        Long loanId = null;
        if (call != null) {
            loanId = call.getBorrowerInfo().getLoanId();
            data.setCallDirection(com.objectbrains.sti.constants.CallDirection.valueOf(call.getCallDirection().name()));
            data.setPhoneNumber(call.getBorrowerInfo().getBorrowerPhoneNumber());
        }
        if (loanId == null) {
            LOG.warn("Ext: {}, CallUUID: {}. Unable to determine loan id.", ext, callUUID);
            return;
        }
        data.setAccountPk(loanId);

        AgentTMS agent = agentService.getAgent(ext);
        UserData userData = new UserData();
        userData.setUserName(agent.getUserName());
//        try {
//            tmsIws.addCallDispositionLog(data, userData);
//        } catch (Throwable ex) {
//            LOG.error("Ext: {}, CallUUID: {}. Error occured while saving disposition note in svc", ext, callUUID, ex);
//        }
    }
}
