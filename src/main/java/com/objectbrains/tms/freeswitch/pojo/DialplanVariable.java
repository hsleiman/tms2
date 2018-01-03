/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.pojo;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.objectbrains.sti.constants.CallerId;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.utility.GsonUtility;
import javax.ws.rs.FormParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hsleiman
 */
public class DialplanVariable {

    private static final String VARIABLE_PREFIX = "variable_";

    private static final Logger LOG = LoggerFactory.getLogger(DialplanVariable.class);

    @Expose
    @FormParam(FreeswitchVariables.FreeSWITCH_IPv4)
    private String FreeSWITCH_IPv4;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.tms_uuid)
    private String tms_uuid;
    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.tms_order)
    private String tms_order;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.tms_order_next)
    private String tms_order_next;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.tms_transfer)
    private Boolean tms_transfer;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.agent_dial_order)
    private Boolean agentDialOrder;

    @Expose
    @FormParam(FreeswitchVariables.rdnis)
    private String rdnis;	//Redirected Number, the directory number to which the call was last presented.
    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.call_direction)
    private CallDirection callDirection;
    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.is_tms_dp)
    private Boolean tms_dp;
    @Expose
    @FormParam(FreeswitchVariables.context)
    private FreeswitchContext context;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.call_uuid)
    private String call_uuid;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.ignore_disposition)
    private Boolean ignore_disposition;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.loan_id)
    private Long loan_id;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.ivr_authorized)
    private Boolean ivr_authorized;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.userOverrideCallerId)
    private String userOverrideCallerId;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.ssn_id)
    private String ssn;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.option_selected_id)
    private Integer option_selected_id;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.option_selected_text)
    private String option_selected_text;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.option_text)
    private String option_text;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.zip_code_id)
    private String zip_code_id;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.borrower_last_name)
    private String borrower_last_name;
    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.borrower_first_name)
    private String borrower_first_name;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.borrower_phone)
    private String borrower_phone;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.is_auto_answer)
    private Boolean auto_answer;
    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.popup_type)
    private String popup_type;
    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.is_dialer)
    private Boolean dialer;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.dialer_queue_id)
    private Long dialer_queue_id;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.agent_group_id)
    private Long agent_group_id;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.amd_status)
    private String amd_status;

    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.amd_result)
    private String amd_result;

    @Expose
    @FormParam(FreeswitchVariables.avmd_detect)
    private String avmd_detect;

    @Expose
    @FormParam(FreeswitchVariables.origination_privacy)
    private String origination_privacy;
    @Expose
    private String ignore_early_media;
    @Expose
    @FormParam(FreeswitchVariables.effective_caller_id_number)
    private String effective_caller_id_number;
    @Expose
    @FormParam(VARIABLE_PREFIX + FreeswitchVariables.tms_callerId)
    private String tms_caller_id;
    @Expose
    @FormParam(FreeswitchVariables.Unique_ID)
    private String UniqueID;
    @Expose
    @FormParam(FreeswitchVariables.Channel_Call_UUID)
    private String ChannelCallUUID;
    @Expose
    @FormParam(FreeswitchVariables.Caller_Transfer_Source)
    private String CallerTransferSource;

    @Expose
    @FormParam(FreeswitchVariables.dialplan_variable_caller_ani)
    private String caller_ani;
    @Expose
    @FormParam(FreeswitchVariables.dialplan_variable_caller_caller_id_name)
    private String caller_caller_id_name;
    @Expose
    @FormParam(FreeswitchVariables.origination_caller_id_name)
    private String origination_caller_id_name;
    @Expose
    @FormParam(FreeswitchVariables.dialplan_variable_caller_caller_id_number)
    private String caller_caller_id_number;
    @Expose
    @FormParam(FreeswitchVariables.origination_caller_id_number)
    private String origination_caller_id_number;
    @Expose
    @FormParam(FreeswitchVariables.dialplan_variable_caller_destination_number)
    private String caller_destination_number;
    @Expose
    @FormParam(FreeswitchVariables.origination_callee_id_name)
    private String origination_callee_id_name;
    @Expose
    @FormParam(FreeswitchVariables.origination_callee_id_number)
    private String origination_callee_id_number;

    @Expose
    @FormParam(FreeswitchVariables.destination_number)
    private String destination_number;

    public String getFreeSWITCH_IPv4() {
        return FreeSWITCH_IPv4;
    }

    public void setFreeSWITCH_IPv4(String FreeSWITCH_IPv4) {
        this.FreeSWITCH_IPv4 = FreeSWITCH_IPv4;
    }

    public String getAmdStatus() {
        return amd_status;
    }

    public void setAmdStatus(String amd_detect) {
        this.amd_status = amd_detect;
    }

    public String getAmdReslut() {
        return amd_result;
    }

    public void setAmdResult(String amd_result) {
        this.amd_result = amd_result;
    }

    public String getAvmdDetect() {
        return avmd_detect;
    }

    public void setAvmdDetect(String avmd_detect) {
        this.avmd_detect = avmd_detect;
    }

    public String getTmsOrder() {
        if (tms_order == null) {
            return "NA";
        }
        return tms_order;
    }

    public void setTmsOrder(String tms_order) {
        this.tms_order = tms_order;
    }

    public String getTmsOrderNext() {
        if (tms_order_next == null) {
            return null;
        }
        return tms_order_next;
    }

    public void setTmsOrderNext(String tms_order_next) {
        this.tms_order_next = tms_order_next;
    }

    public String getTmsUUID() {
        return tms_uuid;
    }

    public void setTmsUUID(String tms_uuid) {
        this.tms_uuid = tms_uuid;
    }

    public CallDirection getCallDirection() {
        if (callDirection == null) {
            String calleeNumber = getCalleeIdNumber();
            String callerNumber = getCallerIdNumber();
            if (getCallerIdInteger() == null && getCallerIdName().equalsIgnoreCase("unknown")) {
                return CallDirection.INBOUND;
            }
            if (callerNumber == null) {
                return CallDirection.INBOUND;
            }
            if (callerNumber.length() == 4) {
                if (calleeNumber.length() == 4) {
                    return CallDirection.INTERNAL;
                }
                if (calleeNumber.length() > 4) {
                    return CallDirection.OUTBOUND;
                }
                throw new UnsupportedOperationException("callee [" + calleeNumber + "] is < 4 characters in length");
            } else if (callerNumber.length() > 4) {
                if (calleeNumber.length() >= 4) {
                    return CallDirection.INBOUND;
                }
                throw new UnsupportedOperationException("callee [" + calleeNumber + "] is < 4 characters in length");
            }
            throw new UnsupportedOperationException("caller [" + callerNumber + "] is < 4 characters in length");
        }
        return callDirection;
    }

    public void setCallDirection(CallDirection callDirection) {
        this.callDirection = callDirection;
    }

    public Boolean getTmsDP() {
        if (tms_dp == null) {
            return Boolean.FALSE;
        }
        return tms_dp;
    }

    public void setTmsDP(Boolean tms_dp) {
        this.tms_dp = tms_dp;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public Integer getOptionSelectedId() {
        return option_selected_id;
    }

    public void setOptionSelectedId(Integer option_selected_id) {
        this.option_selected_id = option_selected_id;
    }

    public String getOptionSelectedText() {
        return option_selected_text;
    }

    public void setOptionSelectedText(String option_selected_text) {
        this.option_selected_text = option_selected_text;
    }

    public String getOptionText() {
        return option_text;
    }

    public void setOptionText(String option_text) {
        this.option_text = option_text;
    }

    public Long getLoanId() {
        return loan_id;
    }

    public void setLoanId(Long loan_id) {
        this.loan_id = loan_id;
    }

    public String getUserOverrideCallerId() {
        return userOverrideCallerId;
    }

    public void setUserOverrideCallerId(String userOverrideCallerId) {
        this.userOverrideCallerId = userOverrideCallerId;
    }

    public String getBorrowerLastName() {
        return borrower_last_name;
    }

    public void setBorrowerLastName(String borrower_last_name) {
        this.borrower_last_name = borrower_last_name;
    }

    public String getBorrowerFirstName() {
        return borrower_first_name;
    }

    public void setBorrowerFirstName(String borrower_first_name) {
        this.borrower_first_name = borrower_first_name;
    }

    public String getBorrower_phone() {
        return borrower_phone;
    }

    public void setBorrower_phone(String borrower_phone) {
        this.borrower_phone = borrower_phone;
    }

    public Boolean getAutoAnswer() {
        return auto_answer;
    }

    public void setAutoAnswer(Boolean auto_answer) {
        this.auto_answer = auto_answer;
    }

    public String getPopupType() {
        return popup_type;
    }

    public void setPopupType(String popup_type) {
        this.popup_type = popup_type;
    }

    public Boolean getDialer() {
        return dialer;
    }

    public void setDialer(Boolean dialer) {
        this.dialer = dialer;
    }

    public Long getDialerQueuePk() {
        return dialer_queue_id;
    }

    public void setDialerQueuePk(Long dialer_queue_id) {
        this.dialer_queue_id = dialer_queue_id;
    }

    public Long getAgent_group_id() {
        return agent_group_id;
    }

    public void setAgent_group_id(Long agent_group_id) {
        this.agent_group_id = agent_group_id;
    }

    public String getCallerIdName() {
        if (origination_caller_id_name != null) {
            return origination_caller_id_name;
        }
        if (caller_caller_id_name != null) {
            return caller_caller_id_name;
        }
        return caller_ani;
    }

    public String getCallerIdNumber() {
        if (origination_caller_id_number != null) {
            return origination_caller_id_number.replace("+1", "");
        }
        if (caller_caller_id_number != null) {
            return caller_caller_id_number.replace("+1", "");
        }
        if (caller_ani != null) {
            return caller_ani.replace("+1", "");
        }
        return "anonymous";
    }

    public Long getCallerIdLong() {
        try {
            return Long.parseLong(getCallerIdNumber());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public Integer getCallerIdInteger() {
        try {
            return Integer.parseInt(getCallerIdNumber());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public String getCalleeIdName() {
        if (origination_callee_id_name != null) {
            return origination_callee_id_name;
        }
        return caller_destination_number;
    }

    public String getCalleeIdNumber() {
        if (origination_callee_id_number != null) {
            return origination_callee_id_number;
        }
        return caller_destination_number;
    }

    public String getCallerDestinationNumber() {
        if (caller_destination_number == null) {
            return "x";
        }
        return caller_destination_number;
    }

    public Integer getCalleeIdInteger() {
        try {
            return Integer.parseInt(getCalleeIdNumber());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public Long getCalleeIdLong() {
        try {
            return Long.parseLong(getCalleeIdNumber());
        } catch (Exception ex) {
            return null;
        }
    }

    public String getOriginationPrivacy() {
        return origination_privacy;
    }

    public void setOriginationPrivacy(String origination_privacy) {
        this.origination_privacy = origination_privacy;
    }

    public String getIgnoreEarlyMedia() {
        return ignore_early_media;
    }

    public void setIgnoreEarlyMedia(String ignore_early_media) {
        this.ignore_early_media = ignore_early_media;
    }

    public String getEffectiveCallerIdNumber() {
        return effective_caller_id_number;
    }

    public void setEffectiveCallerIdNumber(String effective_caller_id_number) {
        this.effective_caller_id_number = effective_caller_id_number;
    }

    public FreeswitchContext getContext() {
        return context;
    }

    public void setContext(FreeswitchContext context) {
        this.context = context;
    }

    public CallerId getTmsCallerId() {
        try {
            return CallerId.valueOf(tms_caller_id);
        } catch (IllegalArgumentException ex) {
            LOG.warn("tms_caller_id [{}] is not a valid CallerId enum value");
            return null;
        }
    }

    public String getZipCodeId() {
        return zip_code_id;
    }

    public void setZipCodeId(String zip_code_id) {
        this.zip_code_id = zip_code_id;
    }

    public String getUniqueID() {
        return UniqueID;
    }

    public void setUniqueID(String UniqueID) {
        this.UniqueID = UniqueID;
    }

    public String getChannelCallUUID() {
        return ChannelCallUUID;
    }

    public void setChannelCallUUID(String ChannelCallUUID) {
        this.ChannelCallUUID = ChannelCallUUID;
    }

    public String getCall_uuid() {
        return call_uuid;
    }

    public void setCall_uuid(String call_uuid) {
        this.call_uuid = call_uuid;
    }

    public String getCallerTransferSource() {
        return CallerTransferSource;
    }

    public void setCallerTransferSource(String CallerTransferSource) {
        this.CallerTransferSource = CallerTransferSource;
    }

    public String getDestination_number() {
        return destination_number;
    }

    public void setDestination_number(String destination_number) {
        this.destination_number = destination_number;
    }

    public Boolean getIgnoreDisposition() {
        return ignore_disposition;
    }

    public void setIgnoreDisposition(Boolean ignore_disposition) {
        this.ignore_disposition = ignore_disposition;
    }

    public String getRdnis() {
        return rdnis;
    }

    public Integer getRdnisInteger() {
        try {
            return Integer.parseInt(rdnis);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public void setRdnis(String rdnis) {
        this.rdnis = rdnis;
    }

    public String getCallerAni() {
        return caller_ani;
    }

    public Integer getCallerAniInteger() {
        try {
            return Integer.parseInt(caller_ani);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public void setCallerAni(String caller_ani) {
        this.caller_ani = caller_ani;
    }

    public Boolean getTmsTransfer() {
        return tms_transfer;
    }

    public void setTmsTransfer(Boolean tms_transfer) {
        this.tms_transfer = tms_transfer;
    }

    public Boolean getIvr_authorized() {
        return ivr_authorized;
    }

    public void setIvr_authorized(Boolean ivr_authorized) {
        this.ivr_authorized = ivr_authorized;
    }

    public Boolean getAgentDialOrder() {
        return agentDialOrder;
    }

    public void setAgentDialOrder(Boolean agentDialOrder) {
        this.agentDialOrder = agentDialOrder;
    }

    public String toJson() {
        LOG.info("Destination Number passed into the dialplan: " + getDestination_number());
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }

}
