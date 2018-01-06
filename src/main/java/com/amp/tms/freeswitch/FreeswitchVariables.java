/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch;

import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service
public class FreeswitchVariables {

    public static final String call_uuid = "sip_h_X-CALL_UUID";
    public static final String tms_uuid = "sip_h_X-TMS_UUID";
    public static final String tms_order = "sip_h_X-TMS_ORDER";
    public static final String tms_order_next = "sip_h_X-TMS_ORDER_NEXT";
    
    public static final String tms_phoneHasMultipleMatches = "sip_h_X-TMS_PHONE_MULTIPLE_MATCHES";
    
    public static final String tms_next_uuid = "sip_h_X-TMS_NEXT_UUID";
    public static final String tms_transfer = "sip_h_X-TMS-TRANSFER";
    
    public static final String destination_number = "destination_number";
    
    public static final String cdr_uuid = "sip_h_X-CDR_UUID";
    
    public static final String amd_status = "amd_status";
    public static final String amd_result = "amd_result";
    
    public static final String avmd_detect = "avmd_detect";
    public static final String rdnis = "Caller-RDNIS";
    
    public static final String ignore_disposition = "sip_h_X-IGNORE_DISPOSITION";
    public static final String call_direction = "sip_h_X-CALL_DIRECTION";
    public static final String is_tms_dp = "sip_h_X-IS_TMS_DP";
    public static final String tms_callerId = "sip_h_X-TMS_CALLERID";

    public static final String option_selected_id = "sip_h_X-OPTION_SELECTED";
    public static final String option_selected_text = "sip_h_X-OPTION_SELECTED_TEXT";
    public static final String option_text = "sip_h_X-OPTION_TEXT";
    public static final String zip_code_id = "sip_h_X-ZIPCODE";
    public static final String ssn_id = "sip_h_X-SSN";
    public static final String loan_id = "sip_h_X-LOAN_ID";
    public static final String userOverrideCallerId =  "sip_h_X-USER-OVERRIDE-CALLERID";
    public static final String borrower_last_name = "sip_h_X-BORROWER_LAST_NAME";
    public static final String borrower_first_name = "sip_h_X-BORROWER_FIRST_NAME";
    public static final String borrower_phone = "sip_h_X-BORROWER_PHONE";
    public static final String tms_other_phone = "sip_h_X-TMS_OTHER_PHONE";
    
    public static final String is_auto_answer = "sip_h_X-IS_AUTO_ANSWER";
    public static final String popup_type = "sip_h_X-POPUP_TYPE";
    
    
    public static final String ivr_step = "sip_h_X-IVR_STEP";
    
    public static final String ivr_authorized = "sip_h_X-IVR_AUTHORIZED";

    public static final String is_dialer = "sip_h_X-IS_DIALER";
    public static final String dialer_queue_id = "sip_h_X-DIALER_QUEUE_ID";
    
    public static final String agent_group_id = "sip_h_X-AGENT_GROUP_ID";
    public static final String agent_dial_order = "sip_h_X-AGENT_DIAL_ORDER";

    public static final String origination_caller_id_name = "origination_caller_id_name";
    public static final String origination_caller_id_number = "origination_caller_id_number";

    public static final String dialplan_variable_caller_caller_id_name = "Caller-Caller-ID-Name";
    public static final String dialplan_variable_caller_caller_id_number = "Caller-Caller-ID-Number";
    public static final String dialplan_variable_caller_ani = "Caller-ANI";

    public static final String origination_callee_id_name = "origination_callee_id_name";
    public static final String origination_callee_id_number = "origination_callee_id_number";

    public static final String dialplan_variable_caller_destination_number = "Caller-Destination-Number";

    public static final String origination_privacy = "origination_privacy";

    public static final String ignore_early_media = "ignore_early_media";

    public static final String effective_caller_id_number = "effective_caller_id_number";
    public static final String effective_caller_id_name = "effective_caller_id_name";
    
    public static final String recroding_upload_tms = "recroding_upload_tms";

    public static final String context = "Caller-Context";

    public static final String Channel_Call_UUID = "Channel-Call-UUID";
    public static final String Caller_Transfer_Source = "Caller-Transfer-Source";
    public static final String Unique_ID = "Unique-ID";

    public static final String FreeSWITCH_IPv4 = "FreeSWITCH-IPv4";
    public static final String domain = "domain";
    public static final String user = "user";
    public static final String ip = "ip";

    public static final String cdr = "cdr";

    public static final String key_value = "key_value";

    public static final String hangup_after_bridge = "hangup_after_bridge";
    public static final String continue_on_fail = "continue_on_fail";
    public static final String call_timeout = "call_timeout";
    public static final String ringback = "ringback";
    
    public static final String sip_to_user = "sip_to_user";
    public static final String sip_from_user = "sip_from_user";
}
