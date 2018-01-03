/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.restfull;

import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;
import com.objectbrains.svc.iws.AgentWeightPriority;
import com.objectbrains.svc.iws.BiPlaybackData;
import com.objectbrains.svc.iws.CallRoutingOption;
import com.objectbrains.svc.iws.SvcException;
import com.objectbrains.svc.iws.TMSServiceIWS;
import com.objectbrains.tms.db.entity.freeswitch.FreeswitchNode;
import com.objectbrains.tms.db.repository.CdrRepository;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.SetAgentState;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.hazelcast.entity.AgentCall;
import com.objectbrains.tms.pojo.AgentDirectory;
import com.objectbrains.tms.pojo.AgentStatus;
import com.objectbrains.tms.pojo.CallHistory;
import com.objectbrains.tms.pojo.SpyOnCallPojo;
import com.objectbrains.tms.service.AgentCallService;
import com.objectbrains.tms.service.AgentService;
import com.objectbrains.tms.service.AmsService;
import com.objectbrains.tms.service.BiStoreService;
import com.objectbrains.tms.service.CallDetailRecordService;
import com.objectbrains.tms.service.FreeswitchConfiguration;
import com.objectbrains.tms.service.FreeswitchNodeService;
import com.objectbrains.tms.service.FreeswitchService;
import com.objectbrains.tms.service.GCESignedUtility;
import com.objectbrains.tms.service.SpeechToTextService;
import com.objectbrains.tms.service.freeswitch.CallingOutService;
import com.objectbrains.tms.service.freeswitch.PhoneOperationService;
import com.objectbrains.tms.utility.CloudStorage;
import static com.objectbrains.tms.utility.CloudStorage.getBucket;
import com.objectbrains.tms.websocket.Websocket;
import com.objectbrains.tms.websocket.WebsocketCache;
import com.objectbrains.tms.websocket.WebsocketService;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 *
 * @author hsleiman
 */
@Path("/tms-commands/phone-control")
@Produces(MediaType.APPLICATION_JSON)
public class TMSPhoneCommand {
    
    @Autowired
    private AmsService amsService;
    
    @Autowired
    private GCESignedUtility gcssurl;
    
    @Autowired
    private AgentCallService agentCallService;
    
    @Autowired
    private AgentService agentService;
    
    @Autowired
    private PhoneOperationService phoneOperationService;
    
    @Autowired
    private FreeswitchConfiguration configuration;
    
    @Autowired
    private CdrRepository cdrRepository;
    
    @Autowired
    private CallDetailRecordService callDetailRecordService;
    
    @Autowired
    @Lazy
    private Websocket websocket;
    
    @Autowired
    @Lazy
    private WebsocketService websocketService;
    
    @Autowired
    private BiStoreService biStoreService;
    
    @Autowired
    private CallingOutService callingOutService;
    
    @Autowired
    private FreeswitchNodeService freeswitchNodeRepository;
    
    @Autowired
    private SpeechToTextService speechToTextService;
    
    @Autowired
    private WebsocketCache websocketCache;
    
    @Autowired
    private FreeswitchService freeswitchService;
    
    @Autowired
    private TMSServiceIWS tmsIws;
    
    private static final Logger log = LoggerFactory.getLogger(TMSPhoneCommand.class);
    
    @Path("/speech-to-text-is-back/{calluuid}/{confidence}/{confidenceRight}/{confidenceLeft}")
    @GET
    public void speechToTextIsBack(@PathParam("calluuid") String callUUID, @PathParam("confidence") Double confidence, @PathParam("confidenceRight") Double confidenceRight, @PathParam("confidenceLeft") Double confidenceLeft) {
        log.info("Got Speech to text Transcript {}", callUUID);
        speechToTextService.processSpeechToText(callUUID, confidence, confidenceRight, confidenceLeft, 0);
    }
    
    @Path("/speech-to-text-error/{calluuid}")
    @GET
    public void speechToTextError(@PathParam("calluuid") String callUUID) {
        speechToTextService.processSpeechToTextError(callUUID);
    }
    
    @Path("/get-freeswitch-registered-status/{ext}")
    @GET
    public Boolean getIsRegistered(@PathParam("ext") int ext) {
        return freeswitchService.isRegisteredOnFreeswitch(ext);
    }
    
    @Path("/get-active-status/{ext}")
    @GET
    public AgentStatus getPhoneActiveStatus(@PathParam("ext") int ext) {
        return agentService.getAgentStatus(ext);
    }
    
    @Path("/set-active-status/{ext}/{active}")
    @GET
    public void setPhoneActiveStatus(@PathParam("ext") int ext, @PathParam("active") SetAgentState param) {
        agentService.setAgentState(ext, param);
        websocketService.refresh(ext);
    }
    
    @Path("/three-way-call/{calleeExt}/{onCallExt}")
    @GET
    public SpyOnCallPojo threeWayCall(@PathParam("calleeExt") int calleeExt, @PathParam("onCallExt") int onCallExt) throws IOException {
        return phoneOperationService.threeWayCall(calleeExt, onCallExt);
    }
    
    @Path("/eavsdrop-on-call/{calleeExt}/{onCallExt}")
    @GET
    public SpyOnCallPojo eavsdropOnCall(@PathParam("calleeExt") int calleeExt, @PathParam("onCallExt") int onCallExt) throws IOException {
        return phoneOperationService.eavsdropOnCall(calleeExt, onCallExt);
    }
    
    @Path("/eavsdrop-on-screen/{callerExt}/{calleeExt}/{status}")
    @GET
    public boolean eavsdropOnScreen(@PathParam("calleeExt") int calleeExt, @PathParam("callerExt") int callerExt, @PathParam("status") String status) throws IOException {
        
        if (status.equals("start")) {
            websocket.addListener(calleeExt, callerExt);
            agentService.setScreenMonitored(calleeExt, true);
            return true;
        } else {
            websocket.removeListener(calleeExt, callerExt);
            agentService.setScreenMonitored(calleeExt, false);
            return false;
        }
    }
    
    @Path("/whisper-on-call/{calleeExt}/{onCallExt}")
    @GET
    public SpyOnCallPojo whisperOnCall(@PathParam("onCallExt") int onCallExt, @PathParam("calleeExt") int calleeExt) throws IOException {
        return phoneOperationService.whisperOnCall(calleeExt, onCallExt);
    }
    
    @Path("/agent-call-recent/{ext}")
    @GET
    public List<CallHistory> getAgentCallHistory(@PathParam("ext") int ext) {
        return cdrRepository.getAgentCallHistory(ext);
    }
    
    @GET
    @Path("/get-playback-data/{callUUID}")
    public BiPlaybackData getBiPlaybackData(@PathParam("callUUID") String callUUID) {
        return biStoreService.getBiPlaybackData(callUUID);
    }
    
    @GET
    @Path("/get-ams-endpoint")
    public String getAMSendpoint() {
        return configuration.getLoadBalancerHostname();
    }
    
    @Path("/agent-directory")
    @GET
    public List<AgentDirectory> getAgentDirectory() {
        return phoneOperationService.getAgentDirectory();
    }
    
    @Path("/agent/play/prompt/{ext}/{call_uuid}")
    @GET
    public String playPrompt(@PathParam("ext") Integer ext, @PathParam("call_uuid") String calluuid) {
        AgentCall agentCall = agentCallService.getActiveCall(ext);
        callingOutService.PlaceFreeswitchCommandAsyc("uuid_broadcast", agentCall.getAgentFreeswitchUUID() + " /usr/local/freeswitch/sounds/TMS-Sound/ivr/ava/8000/phrase/CREDIT_CARD_AMOUNT_APPLIED_TO_ACCOUNT_1.wav both", configuration.getLocalHostAddress());
        return "uuid_broadcast " + agentCall.getAgentFreeswitchUUID() + " /usr/local/freeswitch/sounds/TMS-Sound/ivr/ava/8000/phrase/CREDIT_CARD_AMOUNT_APPLIED_TO_ACCOUNT_1.wav both";
    }
    
    @Path("/updateSoundFiles")
    @GET
    public void updateSoundFiles() {
        callingOutService.InvokTMSLocal("updateScripts/master", configuration.getFreeswitchIP(FreeswitchContext.agent_dp), "");
    }
    
    @Path("/extension/config/{config}")
    @GET
    public String getExtensionConfig(@PathParam("config") String config) {
        String str = configuration.getExtensionConfig(config);
        log.info("STR: {}", str);
        return str;
    }
    
    @Path("/listBucket")
    @GET
    public void listBucket() {
        String bucketName = "phone-recording";
        
        try {
            // Get metadata about the specified bucket.
            Bucket bucket = getBucket(bucketName);
            log.info("name: " + bucketName);
            log.info("location: " + bucket.getLocation());
            log.info("timeCreated: " + bucket.getTimeCreated());
            log.info("owner: " + bucket.getOwner());

            // List the contents of the bucket.
            List<StorageObject> bucketContents = CloudStorage.listBucket(bucketName);
            if (null == bucketContents) {
                log.info(
                        "There were no objects in the given bucket; try adding some and re-running.");
            }
            for (StorageObject object : bucketContents) {
                log.info(object.getName() + " (" + object.getSize() + " bytes)");
                log.info(object.getMediaLink());
                log.info(object.getSelfLink());
            }
            CloudStorage.uploadStream(bucketName, bucketName, null, bucketName);
//            // Upload a stream to the bucket. This could very well be a file.
//            uploadStream(
//                    TEST_FILENAME, "text/plain",
//                    new ByteArrayInputStream("Test of json storage sample".getBytes()),
//                    bucketName);
//
//            // Now delete the file
//            deleteObject(TEST_FILENAME, bucketName);
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    @Path("/freeswitchNode/create-appx")
    @GET
    public String createAppxSetup() {
        FreeswitchNode freeswitchNode = new FreeswitchNode();
        freeswitchNode.setActive_calls(0);
        freeswitchNode.setContext(FreeswitchContext.findContext("all"));
        freeswitchNode.setFreeSWITCH_IPv4("10.240.0.27");
        freeswitchNode.setHostname("appx.objectbrains.com");
        freeswitchNode.setMax_active_calls(0);
        freeswitchNode.setMax_calls_allowed(100);
        freeswitchNode.setMax_calls_threshold(75);
        freeswitchNode.setPriority(1l);
        String hostname1 = freeswitchNodeRepository.createNewNode(freeswitchNode);
        freeswitchNodeRepository.activateNode(hostname1);
        
        freeswitchNode = new FreeswitchNode();
        freeswitchNode.setActive_calls(0);
        freeswitchNode.setContext(FreeswitchContext.findContext("all"));
        freeswitchNode.setFreeSWITCH_IPv4("10.240.0.13");
        freeswitchNode.setHostname("appx-fs-1.objectbrains.com");
        freeswitchNode.setMax_active_calls(0);
        freeswitchNode.setMax_calls_allowed(100);
        freeswitchNode.setMax_calls_threshold(75);
        freeswitchNode.setPriority(1l);
        hostname1 = freeswitchNodeRepository.createNewNode(freeswitchNode);
        freeswitchNodeRepository.activateNode(hostname1);
        
        freeswitchNode = new FreeswitchNode();
        freeswitchNode.setActive_calls(0);
        freeswitchNode.setContext(FreeswitchContext.findContext("all"));
        freeswitchNode.setFreeSWITCH_IPv4("10.240.0.17");
        freeswitchNode.setHostname("appx-fs-2.objectbrains.com");
        freeswitchNode.setMax_active_calls(0);
        freeswitchNode.setMax_calls_allowed(100);
        freeswitchNode.setMax_calls_threshold(75);
        freeswitchNode.setPriority(1l);
        hostname1 = freeswitchNodeRepository.createNewNode(freeswitchNode);
        freeswitchNodeRepository.activateNode(hostname1);
        
        return freeswitchNodeRepository.dumpNodes();
    }
    
    @Path("/freeswitchNode/create/{context}/{hostname}/{priority}/{ip}")
    @GET
    public String createFreeswitchNode(@PathParam("ip") String ip, @PathParam("context") String context, @PathParam("hostname") String hostname, @PathParam("priority") Long priority) {
        FreeswitchNode freeswitchNode = new FreeswitchNode();
        freeswitchNode.setActive_calls(0);
        freeswitchNode.setContext(FreeswitchContext.findContext(context));
        freeswitchNode.setFreeSWITCH_IPv4(ip);
        freeswitchNode.setHostname(hostname);
        freeswitchNode.setMax_active_calls(0);
        freeswitchNode.setMax_calls_allowed(100);
        freeswitchNode.setMax_calls_threshold(75);
        freeswitchNode.setPriority(priority);
        String hostname1 = freeswitchNodeRepository.createNewNode(freeswitchNode);
        return freeswitchNodeRepository.getNode(hostname1).toJson();
    }
    
    @Path("/freeswitchNode/activate/{hostname}")
    @GET
    public String activateFreeswitchNode(@PathParam("hostname") String hostname) {
        freeswitchNodeRepository.activateNode(hostname);
        return freeswitchNodeRepository.getNode(hostname).toJson();
    }
    
    @Path("/freeswitchNode/deactivate/{hostname}")
    @GET
    public String deactivateFreeswitchNode(@PathParam("hostname") String hostname) {
        freeswitchNodeRepository.deactivateNode(hostname);
        return freeswitchNodeRepository.getNode(hostname).toJson();
    }
    
    @Path("/freeswitchNode/poweroff/{hostname}")
    @GET
    public String powerOffFreeswitchNode(@PathParam("hostname") String hostname) {
        freeswitchNodeRepository.powerOffNode(hostname);
        return freeswitchNodeRepository.getNode(hostname).toJson();
    }
    
    @Path("/freeswitchNode/print")
    @GET
    public String pringFreeswitchNode() {
        return freeswitchNodeRepository.dumpNodes();
    }
    
    @Path("/freeswitchNode/getNextNode/{call_uuid}/{context}")
    @GET
    public String getNextFreeswitchNode(@PathParam("call_uuid") String call_uuid, @PathParam("context") String context) {
        return freeswitchNodeRepository.getNextNodeForContext(call_uuid, FreeswitchContext.findContext(context)).toJson();
    }
    
    @Path("/freeswitchNode/releaseNode/{call_uuid}")
    @GET
    public String releaseFreeswitchNode(@PathParam("call_uuid") String call_uuid) {
        String hostname = freeswitchNodeRepository.releaseNode(call_uuid);
        return freeswitchNodeRepository.getNode(hostname).toJson();
    }
    
    @Path("/dumpNumberToCallForDialerTimeout")
    @GET
    public void dumpNumberToCallForDialerTimeout() {
        callDetailRecordService.dumpNumberToCallForDialerTimeout();
    }
    
    @Path("/ThreadDump")
    @GET
    public String crunchifyGenerateThreadDump() {
        final StringBuilder dump = new StringBuilder();
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        final ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 250);
        for (ThreadInfo threadInfo : threadInfos) {
            dump.append('"');
            dump.append(threadInfo.getThreadName());
            dump.append("\" ");
            final Thread.State state = threadInfo.getThreadState();
            dump.append("\n   java.lang.Thread.State: ");
            dump.append(state);
            final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
            for (final StackTraceElement stackTraceElement : stackTraceElements) {
                dump.append("\n        at ");
                dump.append(stackTraceElement);
            }
            dump.append("\n\n");
        }
        log.info(dump.toString());
        return dump.toString();
    }
    
    @Path("/getSystemStats")
    @GET
    public String getSystemStats() {
        
        int mb = 1024 * 1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        final StringBuilder dump = new StringBuilder();
        log.info("##### Heap utilization statistics [MB] #####");
        dump.append("##### Heap utilization statistics [MB] #####");
        //Print used memory
        log.info("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
        dump.append("Used Memory:").append((runtime.totalMemory() - runtime.freeMemory()) / mb);
        //Print free memory
        log.info("Free Memory:" + runtime.freeMemory() / mb);
        dump.append("Free Memory:").append(runtime.freeMemory() / mb);
        //Print total available memory
        log.info("Total Memory:" + runtime.totalMemory() / mb);
        dump.append("Total Memory:").append(runtime.totalMemory() / mb);
        //Print Maximum available memory
        log.info("Max Memory:" + runtime.maxMemory() / mb);
        dump.append("Max Memory:").append(runtime.maxMemory() / mb);
        //Print Maximum available memory
        log.info("Available Processors:" + runtime.availableProcessors());
        dump.append("Available Processors:").append(runtime.availableProcessors());
        return dump.toString();
    }
    
    @Path("/runGC")
    @GET
    public void runGC() {
        
        int mb = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();
        
        log.info("##### Heap utilization statistics [MB] #####");
        //Print used memory
        log.info("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
        //Print free memory
        log.info("Free Memory:" + runtime.freeMemory() / mb);
        //Print total available memory
        log.info("Total Memory:" + runtime.totalMemory() / mb);
        //Print Maximum available memory
        log.info("Max Memory:" + runtime.maxMemory() / mb);
        //Print Maximum available memory
        log.info("Available Processors:" + runtime.availableProcessors());

        //Getting the runtime reference from system
        long time = System.currentTimeMillis();
        log.info("*******************************");
        try {
            System.gc();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        log.info("******************************* {}sec", ((System.currentTimeMillis() - time) / 1000));
        
        log.info("##### Heap utilization statistics [MB] #####");
        //Print used memory
        log.info("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
        //Print free memory
        log.info("Free Memory:" + runtime.freeMemory() / mb);
        //Print total available memory
        log.info("Total Memory:" + runtime.totalMemory() / mb);
        //Print Maximum available memory
        log.info("Max Memory:" + runtime.maxMemory() / mb);
        //Print Maximum available memory
        log.info("Available Processors:" + runtime.availableProcessors());
        
    }
    
    @Path("/sendPushNotification/{ext}/{msg}")
    @GET
    public void sendPushNotification(@PathParam("ext") Integer ext, @PathParam("msg") String msg) {
        websocketService.sendPushNotification(ext, msg);
        
    }
    
    @Path("/sendPushNotificationToGroup/{agentGroup}/{msg}")
    @GET
    public void sendPushNotificationToGroup(@PathParam("agentGroup") Long agentGroup, @PathParam("msg") String msg) {
        try {
            List<AgentWeightPriority> list = tmsIws.getAgentWeightPriorityListForGroup(agentGroup);
            List<Agent> agents = agentService.getAgents(list, null, CallRoutingOption.ROUND_ROBIN);
            for (int i = 0; i < agents.size(); i++) {
                Agent get = agents.get(i);
                
                websocketService.sendPushNotification(get.getExtension(), msg);
            }
        } catch (SvcException ex) {
            log.error("Exception {}", ex);
        }
        
    }
    
    @Path("/refreshWebsocketCache")
    @GET
    public void refreshWebsocketCache() {
        websocketCache.rebuildAll();
    }
    
    @Path("/refreshAudio")
    @GET
    public String refreshAudio() {
        return configuration.getCompanyInfo() + " - " + FreeswitchConfiguration.getIVRVoice();
    }
}
