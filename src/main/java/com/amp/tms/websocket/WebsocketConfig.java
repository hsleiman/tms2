/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket;

import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.tms.enumerated.PromptType;
import com.amp.tms.websocket.message.Function;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public class WebsocketConfig extends ConfigurationUtility {

    private static final String MAX_RETRY_COUNT = "maxRetryCount";
    private static final String MESSAGE_TIMEOUT_MILLIS = "messageTimeoutMillis";
    private static final String DISCONNECT_LOGOFF_DELAY_MILLIS = "disconnectLogoffDelayMillis";
    private static final String ENABLE_LAST_HEARTBEAT = "enable.last.heartbeat";
    private static final String ENABLE_LAST_HEARTBEAT_KEEP_ALIVE = "enable.last.heartbeat.keep.alive";

    private static final String ENABLE_EXT_HEART_CHECK = "enable.ext.heart.check";
    private static final String ENABLE_EXT_HEART_CHECK_DEFULT_RETURN = "enable.ext.heart.check.default.value";
    private static final String ENABLE_EXT_HEART_CHECK_DEFULT_RETURN_WHEN_OFF = "enable.ext.heart.check.default.value.when.off";

    private static final String ENABLE_EXT_HEART_CHECK_TIMEOUT = "enable.ext.heart.check.timeout";

    private static final String ENABLE_PUSH_NOTIFICATION = "enable.push.notification";

    private static final String ENABLE_CALL_START_ON_PHONE_CHECK = "enable.call.start.on.phone.check";
    private static final String CALL_START_ON_PHONE_CHECK_EXPIRE_TIME = "call.start.on.phone.check.expire.time";

    private static final String ENABLE_FREESWITCH_REGISTERED_CHECK = "enable.freeswitch.registered.check";
    private static final String ENABLE_EXT_CHECK_FOR_LOCKED_AGENT = "enable.ext.check.for.locked.agent";
    
    private static final String ENABLE_PLAY_PROMPT_ALL = "enable.play.prompt.all";
    private static final String ENABLE_PLAY_PROMPT_CUSTOM= "enable.play.prompt.";
    
    private static final String GET_FREESWITCH_COMMAND_FOR_PROMPT = "get.freeswitch.command.for.prompt.";
    private static final String GET_PROMPT_PLAY_TYPE = "get.prompt.play.type.";
    
    public static final String GET_MOBILE_API_KEY = "get.mobile.api.key";
    

    public WebsocketConfig(HazelcastService service) {
        super(service);
    }

    public int getMaxRetryCount(Function function) {
        Integer retryCount = getInteger(function + "." + MAX_RETRY_COUNT);
        if (retryCount != null) {
            return retryCount;
        }
        return getInteger(MAX_RETRY_COUNT);
    }

    public long getMessageTimeoutMillis() {
        return getLong(MESSAGE_TIMEOUT_MILLIS);
    }

    public long getMessageTimeoutMillis(Function function) {
        Long ret = getLong(function + "." + MESSAGE_TIMEOUT_MILLIS);
        if (ret != null) {
            return ret;
        }
        return getLong(MESSAGE_TIMEOUT_MILLIS);
    }

    public int getDisconnectLogoffDelayMillis() {
        return getInteger(DISCONNECT_LOGOFF_DELAY_MILLIS);
    }

    public Boolean enableLastHeartbeat() {
        return getBoolean(ENABLE_LAST_HEARTBEAT, Boolean.TRUE);
    }

    public Boolean enableLastHeartbeatForKeepAlive() {
        return getBoolean(ENABLE_LAST_HEARTBEAT_KEEP_ALIVE, Boolean.TRUE);
    }

    public Boolean enableExtHeartCheck() {
        return getBoolean(ENABLE_EXT_HEART_CHECK, true);
    }

    public Boolean enableExtHeartCheckDefaultReturn() {
        return getBoolean(ENABLE_EXT_HEART_CHECK_DEFULT_RETURN, false);
    }

    public Boolean enableExtHeartCheckDefaultReturnWhenOff() {
        return getBoolean(ENABLE_EXT_HEART_CHECK_DEFULT_RETURN_WHEN_OFF, true);
    }

    public Integer enableExtHeartCheckTimeout() {
        return getInteger(ENABLE_EXT_HEART_CHECK_TIMEOUT, 100);
    }

    public Boolean enablePushNotification() {
        return getBoolean(ENABLE_PUSH_NOTIFICATION, true);
    }

    public Boolean enableCallStartOnPhoneCheck() {
        return getBoolean(ENABLE_CALL_START_ON_PHONE_CHECK, true);
    }

    public Integer callStartOnPhoneCheckExpireTime() {
        return getInteger(CALL_START_ON_PHONE_CHECK_EXPIRE_TIME, 30);
    }

    public Boolean enableFreeswitchRegisteredCheck() {
        return getBoolean(ENABLE_FREESWITCH_REGISTERED_CHECK, true);
    }

    public Boolean enableExtCheckForLockedAgent() {
        return getBoolean(ENABLE_EXT_CHECK_FOR_LOCKED_AGENT, true);
    }

    public Integer callTimeoutForInternalCall() {
        return getInteger("call.timeout.for.internal.tranfer.call", 25);
    }

    public String getLoggingServerIPForExt() {
        return getString("get.logging.server.ip.for.ext", "10.253.0.5");
    }
    
    public Boolean enablePlayPromptAll(){
        return getBoolean(ENABLE_PLAY_PROMPT_ALL, true);
    }
    
    public Boolean enablePlayPromptCustom(PromptType promptTypes){
        return getBoolean(ENABLE_PLAY_PROMPT_CUSTOM+promptTypes.name(), true);
    }
    
    public String getFreeswitchCommandForPrompt(PromptType promptTypes){
        return getString(GET_FREESWITCH_COMMAND_FOR_PROMPT+promptTypes.name(), "uuid_broadcast");
    }
    
    public Integer getPromptPlayType(PromptType promptTypes){
        return getInteger(GET_PROMPT_PLAY_TYPE+promptTypes.name(), getPromptPlayTypeDefult());
    }
    
    public Integer getPromptPlayTypeDefult(){
        return getInteger(GET_PROMPT_PLAY_TYPE+"default", 0);
    }
    
    public String getMobileApiKey(){
        return getString(GET_MOBILE_API_KEY, "b0305fec-e61d-4555-acbb-f024bbfc63c4");
    }
    
    public String getDisableMonitor(){
        return getString("exclude.this.ext.for.all.time.recording", "");
    }

}
