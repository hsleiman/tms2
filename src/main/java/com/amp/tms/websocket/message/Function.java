/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message;

/**
 *
 * @author hsleiman
 */
public enum Function {

    PreviewDialer,
    Bi,
    BIStream,
    Verified,
    CallUUID,
    SpeechToText,
    Phone,
    SET_AGENT_STATE,
    SET_AGENT_OFFLINE_STATE,
    AGENT_STATUS,
    CHECK_EXT,
    RESET_AGENT_STATUS_TO_IDLE,
    AGENT_STATS,
    SET_AGENT_DIALER_ACTIVE_STATUS,
    Refresh,
    Refresh_SVC,
    FREESWITCH_CHECK,
    Payment,
    PTP,
    KEEP_ALIVE,
    OPERATOR,
    CALL_RECENT,
    PHONE_CHECK,
    ATTACH_LOAN_TO_CALL,
    PHONE_DIRECTORY_UPDATED,
    PHONE_DIRECTORY_START,
    PHONE_DIRECTORY_STOP,
    SET_USER_CALL_DISPOSITION,
    AGENT_GROUP,
    ECHO,
    LOCK_NEXT_AVAILABLE,
    LOCK_NEXT_AVAILABLE_CANCEL,
    LOCK_NEXT_AVAILABLE_TRANSFER_TO_AGENT,
    CALLER_ID_NUMBERS,
    AGENT_STATS_EXPIRED,
    PUSH_NOTIFICATION,
    CHAT,
    GET_DISPOSITIONS,
    Play_PROMOTS,
    CHANGE_FREESWITCH_IP,
    RESTART_EXTENSION,
    LOG_SERVER_IP,
    PLAY_PROMPT,
    RESET_FREESWITCH_IP;
    
//    SESSION_TIMEOUT,
//    FORCE_OFFLINE,
//    LOGOFF;
}
