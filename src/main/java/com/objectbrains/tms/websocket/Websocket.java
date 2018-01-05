/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.MigrationListener;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.PartitionService;
import com.hazelcast.logging.ILogger;
import com.hazelcast.mapreduce.aggregation.Aggregation;
import com.hazelcast.mapreduce.aggregation.Aggregations;
import com.hazelcast.mapreduce.aggregation.Supplier;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spring.context.SpringAware;
import com.hazelcast.util.executor.StripedExecutor;
import com.hazelcast.util.executor.StripedRunnable;
import com.objectbrains.ams.iws.User;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.sti.constants.PreviewDialerType;
import com.objectbrains.sti.pojo.QueueAgentWeightPriority;
import com.objectbrains.sti.service.dialer.DialerQueueService;
import com.objectbrains.sti.service.tms.TMSService;
import com.objectbrains.tms.db.repository.WebsocketRepository;
import com.objectbrains.tms.freeswitch.pojo.DialerInfoPojo;
import com.objectbrains.tms.hazelcast.Configs;
import com.objectbrains.tms.hazelcast.entity.AgentWeightedPriority;
import com.objectbrains.tms.pojo.StatusPojo;
import com.objectbrains.tms.service.AgentCallService;
import com.objectbrains.tms.service.AgentQueueAssociationService;
import com.objectbrains.tms.service.TMSAgentService;
import com.objectbrains.tms.service.AgentStatsService;
import com.objectbrains.tms.service.AmsService;
import com.objectbrains.tms.service.BiStoreService;
import com.objectbrains.tms.service.dialer.Dialer;
import com.objectbrains.tms.service.dialer.DialerService;
import com.objectbrains.tms.utility.JsonMapper;
import com.objectbrains.tms.websocket.message.BiMessage;
import com.objectbrains.tms.websocket.message.Function;
import com.objectbrains.tms.websocket.message.inbound.Recieve;
import com.objectbrains.tms.websocket.message.outbound.CheckExt;
import com.objectbrains.tms.websocket.message.outbound.PreviewDialerSend;
import com.objectbrains.tms.websocket.message.outbound.Send;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.SpringConfigurator;

/**
 *
 * @author hsleiman
 */
@Component(Websocket.BEAN_NAME)
@Lazy
@ServerEndpoint(value = "/websocket/websocket-e/{ext}/{authToken}", configurator = SpringConfigurator.class)
public class Websocket {

    public static final String BEAN_NAME = "tmsWebsocket";

    private static final Logger LOG = LoggerFactory.getLogger(Websocket.class);

//    private AutowireCapableBeanFactory beanFactory;
    @Autowired
    private AgentStatsService agentStatsService;

    @Autowired
    private WebsocketRepository websocketRepository;

    @Autowired
    private AgentCallService callService;

    @Autowired
    private AmsService amsService;

    @Autowired
    private TMSAgentService agentService;

    @Autowired
    private BiStoreService biStoreService;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private WebsocketService websocketService;

    @Autowired
    private AgentQueueAssociationService associationService;

    @Autowired
    private DialerService dialerService;
    
    @Autowired
    private DialerQueueService dialerQueueService;

    @Autowired
    private TMSService tmsIws;

//    @Autowired
//    private Scheduler scheduler;
    @Autowired
    private TaskScheduler scheduler;

    @Autowired
    @Qualifier("tms-executor")
    private TaskExecutor executor;

    @ConfigContext
    private WebsocketConfig websocketConfig;

    @Autowired
    private WebsocketCache websocketCache;

    private StripedExecutor writeMessageExecutor;

    private IExecutorService executorService;
//    private ReplicatedMap<Integer, Integer> sessionBiMonitor;
//    private ReplicatedMap<Integer, Member> agentRoutingTable;
    private String listenerId;
    private MultiMap<Integer, Integer> sessionBiMonitorMap;

    private final ConcurrentHashMap<Integer, Session[]> sessions = new ConcurrentHashMap<>();
    private final Set<Integer> sessionDirectoryMonitor = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
    private final ConcurrentHashMap<String, Future<?>> scheduledRetries = new ConcurrentHashMap<>();

//    private final ConcurrentHashMap<Integer, Future<?>> scheduledLogoffs = new ConcurrentHashMap<>();
    Map<String, SendAndWaitCallable> waitingCallables = new ConcurrentHashMap<>();

    Future removeScheduledRetries(String uuid) {
        return scheduledRetries.remove(uuid);
    }

    void putScheduledRetries(String uuid, Future<?> future) {
        scheduledRetries.put(uuid, future);
    }

    protected Set<Integer> getConnectedExtensions() {
        return sessions.keySet();
    }

//    void removeSessionIfPresent(int ext, Session session) {
//        sessions.remove(ext, session);
//    }
    Session[] getSessions(int ext) {
        return sessions.get(ext);
    }

    public boolean hasSessions(int ext) {
        return sessions.containsKey(ext);
    }

    @PostConstruct
    void init() {
        executorService = hazelcastService.getExecutorService(Configs.WEBSOCKET_EXECUTOR_SERVICE);
//        sessionBiMonitor = hazelcastService.getReplicatedMap(Configs.WEBSOCKET_BI_MONITOR_MAP);
        sessionBiMonitorMap = hazelcastService.getMultiMap(Configs.WEBSOCKET_BI_MONITOR_MAP);
        final PartitionService partitonService = hazelcastService.getPartitionService();

        listenerId = partitonService.addMigrationListener(new MigrationListener() {

            @Override
            public void migrationStarted(MigrationEvent migrationEvent) {
                for (Map.Entry<Integer, Session[]> entrySet : sessions.entrySet()) {
                    Integer key = entrySet.getKey();
                    Session[] value = entrySet.getValue();
                    if (partitonService.getPartition(key).getPartitionId() == migrationEvent.getPartitionId()) {
                        for (Session session : value) {
                            try {
                                session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Hazelcast is migrating"));
                            } catch (IOException ex) {
                                //do  nothing
                            }
                        }
                    }
                }
            }

            @Override
            public void migrationCompleted(MigrationEvent migrationEvent) {
            }

            @Override
            public void migrationFailed(MigrationEvent migrationEvent) {
            }

        });

        ILogger logger = hazelcastService.getLoggingService().getLogger(Websocket.class);
        writeMessageExecutor = new StripedExecutor(logger,
                "websocket-write-queue",
                new ThreadGroup("tms-websocket"),
                15, //thread pool size
                Integer.MAX_VALUE);//max pending messages

//        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
//        beanFactory = wac.getAutowireCapableBeanFactory();
    }

    @PreDestroy
    void cleanup() {
        writeMessageExecutor.shutdown();
        hazelcastService.getPartitionService().removeMigrationListener(listenerId);
    }

//    @Scheduled(initialDelay = 30000, fixedRate = 30000)
//    void keepAlive() {
//        for (Map.Entry<Integer, Session> entrySet : sessions.entrySet()) {
//            Integer key = entrySet.getKey();
//            Send send = new Send(Function.KEEP_ALIVE);
//            sendWithRetry(key, send);
//        }
//    }
    public Set<Integer> getSessionDirectoryMonitor() {
        return sessionDirectoryMonitor;
    }

//    public boolean isSessionMonitor(Integer ext) {
//        return sessionBiMonitor.containsKey(ext);
//    }
    public void sendWithRetry(int ext, Send send) {
        LOG.info("Send With Retry {} - {}", ext, send.toJson());
        executorService.executeOnKeyOwner(new SendWithRetryTask(ext, send), ext);
        LOG.info("Send With Retry {} - {}", ext, "executorService.executeOnMember(new SendWithRetryTask(ext, send), member);");
    }

    public boolean checkAgentExt(int ext) {
        LOG.info("1Doing a check on ext {}", ext);

        if (websocketConfig.enableExtHeartCheck() == false) {
            return websocketConfig.enableExtHeartCheckDefaultReturnWhenOff();
        }
        LOG.info("2Doing a check on ext {}", ext);
        Send send = new Send(Function.CHECK_EXT);
        CheckExt checkExt = new CheckExt(LocalTime.now());
        send.setCheckExt(checkExt);

        try {
            Recieve recieve = sendAndWait(ext, send, websocketConfig.enableExtHeartCheckTimeout());
            LOG.info("Doing a check on ext {} result is {}", ext, recieve.getCheckExt().isGood());
            return recieve.getCheckExt().isGood();
        } catch (InterruptedException ex) {
            LOG.warn("InterruptedException Doing a check on ext {} result is {}", ext, false);
        } catch (TimeoutException | IOException ex) {
            LOG.warn("TimeoutException/IOException Doing a check on ext {} result is {}", ext, false);
        }
        return websocketConfig.enableExtHeartCheckDefaultReturn();
    }

    public Recieve sendAndWait(int ext, Send send, int timeoutMillis) throws InterruptedException, TimeoutException, IOException {
        Future<Recieve> ret = executorService.submitToKeyOwner(new SendAndWaitCallable(ext, send, timeoutMillis), ext);
        try {
            return ret.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ex) {
            throw new IOException(ex.getCause());
        }
    }

    void sendWithRetry(int ext, Session session, Send send) {
        executor.execute(new SendWithRetryRunner(ext, session, send));
    }

    void writeMessageSend(int ext, Send send) {
        websocketRepository.logMessage(ext, send);
        Session[] session = sessions.get(ext);
        long timeout = websocketConfig.getMessageTimeoutMillis(send.getFunction());
        if (session != null) {
            for (Session session1 : session) {
                writeMessage(ext, session1, send, timeout);
            }
        }
    }

    private void writeMessageSend(int ext, Session session, Send send) {
        websocketRepository.logMessage(ext, send);
        writeMessage(ext, session, send, websocketConfig.getMessageTimeoutMillis(send.getFunction()));
    }

    private void writeMessage(int ext, Session session, Object message, long timeoutMillis) {
//        WriteMessageTask task = new WriteMessageTask(ext, message, timeoutMillis);
//        beanFactory.autowireBean(task);
//        Runnable runnable = (Runnable) beanFactory.initializeBean(task, "writeMessageTask");
        WriteMessageRunner task = new WriteMessageRunner(ext, session, message, timeoutMillis);
        writeMessageExecutor.execute(task);
    }

    void sendToAll(SendUpdateOfDirectoryTask task) {
        LOG.info("Sending Directory To All Sessions: " + sessionDirectoryMonitor.size());
        executorService.executeOnAllMembers(task);
    }

    private void verify(Session session, Recieve rec, Integer ext) {
        Send send = new Send(Function.Verified);
        send.setConfirmCode(rec.getConfirmCode());
        writeMessageSend(ext, session, send);
    }

    private void sendToAgent(BiMessage biMessage, int agentExt) {
        Send send = new Send(Function.BIStream);
        send.setBiMessage(biMessage);
        sendWithRetry(agentExt, send);
    }

    public void addListener(Integer caller, Integer callee) {
        sessionBiMonitorMap.put(caller, callee);
        LOG.info("Added From Screen monitor: " + caller + " ---> " + callee);
    }

    public void addHistoryListener(Integer sub) {
        sessionDirectoryMonitor.add(sub);
    }

    public void removeListener(Integer caller, Integer callee) {
        sessionBiMonitorMap.remove(caller, callee);
        LOG.info("Removed From Screen monitor: " + callee);
    }

    public void removeHistoryListener(Integer sub) {
        sessionDirectoryMonitor.remove(sub);
    }

    public void sendToAgent(DialerInfoPojo pojo) {
        LOG.info("-------------------------------");
        LOG.info("SENDING TO AGENT");
        LOG.info(pojo.toJson());
        LOG.info("-------------------------------");
        LOG.info("-------------------------------");

//        agentStatsService.setState(pojo.getAgentExt(), AgentState.PREVIEW, Duration.ZERO);
        Send send = new Send(Function.PreviewDialer);
        PreviewDialerSend previewDialer = new PreviewDialerSend();

        previewDialer.setCallUUID(pojo.getCallUUID());
        previewDialer.setBorrowerFirstName(pojo.getBorrowerFirstName());
        previewDialer.setBorrowerLastName(pojo.getBorrowerLastName());
        previewDialer.setLoanId(pojo.getLoanId());
        previewDialer.setPreviewType(pojo.getSettings().getPreviewDialerType().value());
        previewDialer.setPopupType(pojo.getSettings().getPopupDisplayMode());
        previewDialer.setPhone(pojo.getPhoneToType());

        if (pojo.getSettings().getPreviewDialerType() == PreviewDialerType.DELAY_CALL) {
            long delay = pojo.getSettings().getMaxDelayCallTime();
            if (delay < 10) {
                delay = 15;
            }
            previewDialer.setDelay(delay);
        }

        send.setPreviewDialer(previewDialer);
        LOG.info("*************************");
        LOG.info(previewDialer.dump());
//        sessions.get(pojo.getAgentExt()).getUserProperties().put(TMS_DIALER_POJO, pojo);

        sendWithRetry(pojo.getAgentExt(), send);
    }

    private String authenticate(int ext, int authToken) {
        User user = amsService.getUser(ext);
        if (user == null) {
            return null;
        }
        agentService.updateUserRawData(ext, user);
        if (user.getExtensionAuthToken() == authToken) {
            return user.getUserName();
        }
        return null;
    }

    private void unscheduleDisconnectLogoff(int ext) {
//        Future<?> future = scheduledLogoffs.remove(ext);
//        if (future != null) {
//            future.cancel(false);
//        }
    }

    private void scheduleDisconnectLogoff(final Integer ext) {
//        LocalDateTime disconnectTime = LocalDateTime.now().plusMillis(websocketConfig.getDisconnectLogoffDelayMillis());
//        scheduledLogoffs.put(ext, scheduler.schedule(new Runnable() {
//
//            @Override
//            public void run() {
////                 agentService.setAgentState(ext, SetAgentState.LOGOFF);
//                Set<AgentCall> calls = callService.clearAgentCalls(ext);
//                if (calls != null) {
//                    for (AgentCall call : calls) {
//                        if (!call.isWrapped()) {
//                            //TODO mark in slave cdr that call was improperly ended
//                            LOG.info("Call Cleared for agent {} because websocket closer call uuid {} - {}", ext, call.getCallUUID(), call.getCallDirection());
//                        }
//                    }
//                }
//                agentStatsService.setState(ext, AgentState.OFFLINE, Duration.ZERO);
//                scheduledLogoffs.remove(ext);
//            }
//
//        }, disconnectTime.toDate()));
    }

    private void removeDistributedAgent(int ext) {
        scheduleDisconnectLogoff(ext);
//        Set<AgentCall> calls = callService.clearAgentCalls(ext);
//        if (calls != null) {
//            for (AgentCall call : calls) {
//                if (!call.isWrapped()) {
//                    //TODO mark in slave cdr that call was improperly ended
//                    LOG.info("Call Cleared for agent {} because websocket closer call uuid {} - {}", ext, call.getCallUUID(), call.getCallDirection());
//                }
//            }
//        }
        sessionBiMonitorMap.remove(ext);
        Aggregation<Integer, Integer, Set<Integer>> agg = Aggregations.distinctValues();
        Set<Integer> observees = sessionBiMonitorMap.aggregate(new BiMonitorSupplier(ext), agg);

        for (Integer observee : observees) {
            sessionBiMonitorMap.remove(observee, ext);
        }
        sessionDirectoryMonitor.remove(ext);
    }

    private static class BiMonitorSupplier extends Supplier<Integer, Integer, Integer> implements DataSerializable {

        private int ext;

        public BiMonitorSupplier() {
        }

        public BiMonitorSupplier(int ext) {
            this.ext = ext;
        }

        @Override
        public Integer apply(Map.Entry<Integer, Integer> entry) {
            if (ext == entry.getValue()) {
                return entry.getKey();
            }
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeInt(ext);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            ext = in.readInt();
        }

    }

    private void addSession(int ext, Session session) {
        Session[] single = new Session[]{session};
        for (;;) {
            Session[] arr = sessions.putIfAbsent(ext, single);
            if (arr == null) {
                unscheduleDisconnectLogoff(ext);
                return;
            }
            Session[] copy = ArrayUtils.add(arr, session);
            if (sessions.replace(ext, arr, copy)) {
                return;
            }
        }
    }

    private void removeSession(int ext, Session session) {
        for (;;) {
            Session[] arr = sessions.get(ext);
            int index = ArrayUtils.indexOf(arr, session);
            if (index == -1) {
                return;
            }
            if (arr.length == 1) {
                if (sessions.remove(ext, arr)) {
                    removeDistributedAgent(ext);
                    return;
                }
            } else {
                Session[] copy = ArrayUtils.remove(arr, index);
                if (sessions.replace(ext, arr, copy)) {
                    return;
                }
            }
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("ext") int ext, @PathParam("authToken") int authToken) throws IOException {

        websocketRepository.logEvent(ext, "open");
        if (!hazelcastService.getPartitionService().getPartition(ext).getOwner().localMember()) {
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Connected to wrong server"));
            return;
        }

        String userName = authenticate(ext, authToken);
        if (userName == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Could not authenticate extension [" + ext + "] [" + authToken + "]"));
            return;
        }
        addSession(ext, session);

//        Session existingSession = sessions.putIfAbsent(ext, session);
//        if (existingSession != null) {
//            session.close(new CloseReason(CloseReason.CloseCodes.RESERVED, "A websocket with extension [" + ext + "] already connected"));
//            return;
//        }
        agentStatsService.startStats(ext);
        try {
            List<QueueAgentWeightPriority> ret = dialerQueueService.getQueueAgentWeightPriorityForUsername(userName);
            List<Long> queuePks = new ArrayList<>();
            List<AgentWeightedPriority> awps = new ArrayList<>();
            for (QueueAgentWeightPriority ret1 : ret) {
                long queuePk = ret1.getQueuePk();
                queuePks.add(queuePk);
                AgentWeightedPriority awp = new AgentWeightedPriority(ret1);
                Dialer dialer = dialerService.getDialer(queuePk);
                if(dialer != null){
                    awp.setDialerType(dialer.getDialerType());
                }
                awps.add(awp);
            }
            associationService.setAgentQueueAssociations(ext, queuePks, awps);
        } catch (Exception ex) {
            LOG.error("failed to set queues for agent '{}'", userName, ex);
        }

        StatusPojo sessionReturn = new StatusPojo();
        sessionReturn.setStatus("Connected");
        writeMessage(ext, session, sessionReturn, websocketConfig.getMessageTimeoutMillis());
        LOG.info("Websocket Session Openned: {} Session Count: {}", ext, sessions.keySet().size());
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason, @PathParam("ext") int ext) {
        LOG.info("-------------------------");
        LOG.info("Websocket Session {} close because of {} Session Count: {}", ext, closeReason.getReasonPhrase(), sessions.keySet().size());
        websocketRepository.logEvent(ext, "closed");
        removeSession(ext, session);

//        sessions.remove(ext);
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("ext") int ext) {
        websocketRepository.logEvent(ext, "error");
        LOG.info("-------------------------");
        LOG.info("Websocket Session {} Error because of {} Session Count: {}", ext, error.getMessage(), sessions.keySet().size(), error);
    }

    private volatile int maxMessageLength = 100;

    @OnMessage
    public void handleMessage(String message, boolean last, Session session, @PathParam("ext") int ext) {
        final String bufferProp = "messageBuffer";
        Map<String, Object> userProps = session.getUserProperties();
        StringBuilder messageBuffer = (StringBuilder) userProps.get(bufferProp);
        if (messageBuffer == null) {
            messageBuffer = new StringBuilder(maxMessageLength);
            userProps.put(bufferProp, messageBuffer);
        }
        messageBuffer.append(message);

        if (last) {
            //LOG.info("messageBuffer: " + messageBuffer.toString());
            handleMessage(messageBuffer.toString(), session, ext);
            maxMessageLength = Math.max(maxMessageLength, messageBuffer.capacity());
            messageBuffer.setLength(0);
        }
    }

    private void handleMessage(String message, Session session, int ext) {
        long time = System.currentTimeMillis();

        Recieve recieve = null;
        try {
            recieve = jsonMapper.readValue(message, Recieve.class);
            websocketRepository.logMessage(ext, message, recieve);

            log(recieve, "{} -> Recieved: {}", ext, message);

            SendAndWaitCallable callable = waitingCallables.get(recieve.getConfirmCode());
            if (callable != null) {
                callable.setReceive(recieve);
                return;
            }
            switch (recieve.getFunction()) {
                case ECHO:
                    Send sendEcho = new Send(Function.ECHO);
                    sendEcho.setStatus(recieve.getStatus());
                    sendWithRetry(ext, sendEcho);
                    break;
                case PHONE_DIRECTORY_START:
                    sessionDirectoryMonitor.add(ext);
                    //LOG.info(ext+" -> Directory Sessions: " + sessionDirectoryMonitor.size());
                    break;
                case CALLER_ID_NUMBERS:
                    Send sendCallerIds = new Send(Function.CALLER_ID_NUMBERS);
                    sendCallerIds.setCallerIds(websocketCache.getCallerIdNumbers());
                    sendWithRetry(ext, sendCallerIds);
                    break;
                case PHONE_DIRECTORY_STOP:
                    sessionDirectoryMonitor.remove(ext);
                    //LOG.info(ext+" -> Directory Sessions: " + sessionDirectoryMonitor.size());
                    break;
                case Bi:
                    handleBiMessage(ext, recieve.getBiMessage(), recieve.getCall_uuid());
                    break;
                case Verified:
                    Future<?> future = scheduledRetries.remove(recieve.getConfirmCode());
                    if (future != null) {
                        future.cancel(false);
                    }
                    if (websocketConfig.enableLastHeartbeat()) {
                        agentService.updateLastHeartbeat(ext);
                    }
                    return;
                case KEEP_ALIVE:
                    if (websocketConfig.enableLastHeartbeat() && websocketConfig.enableLastHeartbeatForKeepAlive()) {
                        agentService.updateLastHeartbeat(ext);
                    }
                    break;
                default:
                    websocketService.handleMessage(ext, recieve);
            }
            verify(session, recieve, ext);
        } catch (IOException ex) {
            LOG.error("Websocket Session [{}] recieved bad message: {}", ext, message, ex);
        } catch (RuntimeException ex) {
            LOG.error("[{}] Error while handling message: {}", ext, message, ex);
            throw ex;
        } finally {
            if (recieve != null) {
                log(recieve, "Recieved {} Websocket Function: {}, time(ms): {}", ext, recieve.getFunction(), System.currentTimeMillis() - time);
            }
        }
    }

    public void handleBiMessage(int ext, BiMessage biMessage, String callUUID) {
        Collection<Integer> observers = sessionBiMonitorMap.get(ext);
        for (Integer observer : observers) {
            //LOG.info("Sending BI back to " + key + " on behanf of " + value);
            String extExclude = websocketConfig.getString("exclude.this.ext.for.all.time.recording", "");
            if (extExclude.contains("|" + ext + "|") == false) {
                sendToAgent(biMessage, observer);
            }
        }
        biStoreService.saveBiMessage(ext, biMessage, callUUID);
    }

    private void log(Recieve recieve, String format, Object... args) {
        switch (recieve.getFunction()) {
            case Phone:
            case SET_AGENT_STATE:
            case SET_AGENT_OFFLINE_STATE:
            case SET_AGENT_DIALER_ACTIVE_STATUS:
            case AGENT_STATUS:
            case LOCK_NEXT_AVAILABLE:
            case LOCK_NEXT_AVAILABLE_CANCEL:
            case LOCK_NEXT_AVAILABLE_TRANSFER_TO_AGENT:
            case PHONE_CHECK:
            case CHECK_EXT:
            case CallUUID:
                LOG.info(format, args);
                break;
            default:
                LOG.debug(format, args);
                break;
            case Verified:
            case Bi:
                LOG.trace(format, args);
                break;
        }
    }

    private class WriteMessageRunner implements StripedRunnable {

        private final int ext;
        private final Session session;
        private final Object message;
        private final long timeoutMillis;

        public WriteMessageRunner(int ext, Session session, Object message, long timeoutMillis) {
            this.ext = ext;
            this.session = session;
            this.message = message;
            this.timeoutMillis = timeoutMillis;
        }

        @Override
        public int getKey() {
            return session.hashCode();
        }

        @Override
        public void run() {
            Future<?> future = null;
            String msg = null;
            try {
                if (session == null) {
                    LOG.warn("Websocket was unable to find ext [{}] in sessions, failed to send message: {}",
                            ext, jsonMapper.toPrettyJson(message));
                } else if (session.isOpen()) {
                    msg = jsonMapper.writeValueAsString(message);
                    future = session.getAsyncRemote().sendText(msg);
                    future.get(timeoutMillis, TimeUnit.MILLISECONDS);
                } else {
                    LOG.warn("Session [{}] closed. Failed to send message: {}",
                            ext, jsonMapper.toPrettyJson(message));
                    removeSession(ext, session);
                }
            } catch (JsonProcessingException ex) {
                LOG.error("Error while writing to extension: {}", ext, ex);
            } catch (ExecutionException ex) {
                LOG.error("Error while writing to extension: {}", ext, ex.getCause());
            } catch (InterruptedException ex) {
                LOG.warn("Interruped while sending message {} - {}", ext, msg, ex);
                if (future != null) {
                    future.cancel(true);
                }
            } catch (TimeoutException ex) {
                LOG.warn("Took too long to send message {} - {}", ext, msg, ex);
                if (future != null) {
                    future.cancel(false);
                }
            }
        }

    }

    private class SendWithRetryRunner implements Runnable {

        private int ext;
        private Session session;
        private Send send;

        public SendWithRetryRunner(int ext, Session session, Send send) {
            this.ext = ext;
            this.session = session;
            this.send = new Send(send);
        }

        @Override
        public void run() {
            try {
                String uuid = send.getConfirmCode();
                if (uuid != null) {
                    removeScheduledRetries(uuid);
                    LOG.info("Resending To {}", ext);
                }
                try {
                    if (session == null) {
                        LOG.warn("Websocket was unable to find ext [{}] in sessions, failed to send message: {}", ext, jsonMapper.toPrettyJson(send));
                        return;
                    }
                    if (!session.isOpen()) {
                        LOG.warn("Session [{}] closed. Failed to send message: {}",
                                ext, jsonMapper.toPrettyJson(send));
                        removeSession(ext, session);
                        return;
                    }
                } catch (JsonProcessingException ex) {
                    LOG.error(ex.getMessage(), ex);
                    return;
                }

                LOG.debug("Sending: {} {}", ext, send.getFunction());
                if (LOG.isDebugEnabled() && send.getFunction() != Function.BIStream) {
                    LOG.debug("Sending: {} {}", ext, send.toJson());
                }
                // LOG.info("SENDING TO getConfirmCount" + send.getConfirmCount());
                send.setConfirmCount(send.getConfirmCount() + 1);
                // LOG.info("SENDING TO getConfirmCount" + send.getConfirmCount());
                if (send.getConfirmCount() > websocketConfig.getMaxRetryCount(send.getFunction())) {
                    try {
                        LOG.warn("Retry count exceeded, giving up on sending message: {}", jsonMapper.toPrettyJson(send));
                    } catch (JsonProcessingException ex) {
                        LOG.error(ex.getMessage(), ex);
                    }
                    if (send.getFunction() == Function.KEEP_ALIVE) {
                        try {
                            session.close(new CloseReason(CloseReason.CloseCodes.RESERVED, "A websocket with extension [" + ext + "] " + Function.KEEP_ALIVE + " exceeded retry."));
//                        removeSession(ext);
                        } catch (IOException ex) {
                            LOG.error("Could not close websocket for KEEP_ALIVE", ex);
                        }
                    }
                    return;
                }
                uuid = UUID.randomUUID().toString();
                send.setConfirmCode(uuid);

                //schedule a resend in 3 seconds if a validation isn't received
                Future<?> future = scheduler.schedule(this,
                        LocalDateTime.now().plus(new Duration(websocketConfig.getMessageTimeoutMillis(send.getFunction()))).toDate());
                putScheduledRetries(uuid, future);

                writeMessageSend(ext, session, send);
            } catch (RuntimeException ex) {
                LOG.error("Unexpected exception in SendWithRetryRunner", ex);
            }

        }
    }

    @SpringAware
    private static class SendAndWaitCallable implements Callable<Recieve>, DataSerializable {

        @Autowired
        private Websocket websocket;

        private int ext;
        private Send send;
        private int timeout;
        private transient Lock lock;
        private transient Condition condition;
        private transient Recieve receive;

        private SendAndWaitCallable() {
        }

        public SendAndWaitCallable(int ext, Send send, int timeout) {
            this.ext = ext;
            this.send = send;
            this.timeout = timeout;
        }

        @Override
        public Recieve call() throws Exception {
            final long deadline = System.currentTimeMillis() + timeout;
            String confirmCode = UUID.randomUUID().toString();
            send.setConfirmCode(confirmCode);

            lock = new ReentrantLock();
            condition = lock.newCondition();
            websocket.waitingCallables.put(confirmCode, this);
            websocket.writeMessageSend(ext, send);
            lock.lock();
            try {
                while (websocket.waitingCallables.containsKey(confirmCode)) {
                    long waitTime = deadline - System.currentTimeMillis();
                    if (waitTime <= 0 || !condition.await(waitTime, TimeUnit.MILLISECONDS)) {
                        websocket.waitingCallables.remove(confirmCode);
                        try {
                            String json = websocket.jsonMapper.toPrettyJson(send);
                            throw new TimeoutException("Timeout expired for message: " + json);
                        } catch (JsonProcessingException ex) {
                            throw new TimeoutException("Timeout expired for message: " + send.toJson());
                        }
                    }
                }
                return receive;
            } finally {
                lock.unlock();
            }
        }

        public void setReceive(Recieve receive) {
            lock.lock();
            try {
                this.receive = receive;
                websocket.waitingCallables.remove(send.getConfirmCode());
                condition.signal();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeInt(ext);
            out.writeObject(send);
            out.writeInt(timeout);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            ext = in.readInt();
            send = in.readObject();
            timeout = in.readInt();
        }
    }

}
