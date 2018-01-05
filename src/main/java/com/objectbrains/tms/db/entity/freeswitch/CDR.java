/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.entity.freeswitch;

import com.objectbrains.sti.constants.CallerId;
import com.objectbrains.tms.enumerated.CallDirection;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.freeswitch.FreeswitchVariables;
import com.objectbrains.tms.restfull.util.StringToCDRConverter;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;

/**
 *
 * @author hsleiman
 */
@Entity
@Table(schema = "sti")
public class CDR implements Serializable {

    private static final StringToCDRConverter CONVERTER = new StringToCDRConverter();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createTimestamp;

    @PrePersist
    private void onCreate() {
        createTimestamp = LocalDateTime.now();
        if (caller_id_number != null) {
            caller_id_number = caller_id_number.replace("+1", "");
        }
        if (ani != null) {
            ani = ani.replace("+1", "");
        }
    }

    private String username;

    @CdrCallflowProfile
    private String caller_id_name;
    @CdrCallflowProfile
    private String caller_id_number;
    @CdrCallflowProfile
    private String ani;

    @CdrCallflowProfile
    private String callee_id_name;
    @CdrCallflowProfile
    private String callee_id_number;
    @CdrCallflowProfile
    private String destination_number;

    @CdrVariable(value = FreeswitchVariables.effective_caller_id_number)
    private String effective_caller_id_number;

    @CdrVariable(value = FreeswitchVariables.tms_callerId)
    @Enumerated(value = EnumType.STRING)
    private CallerId callerId;

    @CdrVariable(value = FreeswitchVariables.amd_status)
    private String amd_status;

    @CdrVariable(value = FreeswitchVariables.amd_result)
    private String amd_result;

    @CdrVariable(value = FreeswitchVariables.call_uuid)
    private String call_uuid;

    @CdrVariable(value = FreeswitchVariables.cdr_uuid)
    private String cdr_uuid;

    @CdrVariable(value = FreeswitchVariables.call_direction)
    @Enumerated(value = EnumType.STRING)
    private CallDirection callDirection;

    @CdrVariable(value = FreeswitchVariables.loan_id)
    private Long loanId;

    @CdrVariable(value = FreeswitchVariables.recroding_upload_tms)
    @Column(length = 1024)
    private String recrodingUploadTms;

    @CdrVariable(value = FreeswitchVariables.borrower_last_name)
    private String borrowerLastName;

    @CdrVariable(value = FreeswitchVariables.borrower_first_name)
    private String borrowerFirstName;

    @CdrVariable(value = FreeswitchVariables.borrower_phone)
    private String borrowerPhone;

    @CdrVariable(value = FreeswitchVariables.is_auto_answer)
    private Boolean autoAswer;
    @CdrVariable(value = FreeswitchVariables.is_dialer)
    private Boolean dialer;

    @CdrCallflowProfileMin(value = FreeswitchVariables.destination_number)
    private String destination_number_profile_min;

    @CdrVariable(value = FreeswitchVariables.dialer_queue_id)
    @Column(name = "dialer_queue_id")
    private Long dialerQueueId;

    @CdrVariable(value = FreeswitchVariables.agent_group_id)
    @Column(name = "agent_group_id")
    private Long agentGroupId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "dialer_queue_id", updatable = false, insertable = false)
    // private DialerQueue dialerQueue;
    @CdrVariable(value = "mduration")
    private Long duration;
    @CdrVariable
    private Long answermsec;
    @CdrVariable
    private Long waitmsec;
    @CdrAttribute
    private Long progressusec;

    @CdrAttribute
    private String switchname;

    public String getAmd_status() {
        return amd_status;
    }

    public void setAmd_status(String amd_status) {
        this.amd_status = amd_status;
    }

    public String getAmd_result() {
        return amd_result;
    }

    public void setAmd_result(String amd_result) {
        this.amd_result = amd_result;
    }
    
    @CdrVariable
    private String sip_local_network_addr;
    
    @CdrVariable
    private String sip_call_id;

    @CdrVariable
    private double rtp_audio_in_jitter_min_variance;
    @CdrVariable
    private double rtp_audio_in_jitter_max_variance;
    @CdrVariable
    private double rtp_audio_in_jitter_loss_rate;
    @CdrVariable
    private double rtp_audio_in_jitter_burst_rate;
    @CdrVariable
    private double rtp_audio_in_mean_interval;
    @CdrVariable
    private double rtp_audio_in_flaw_total;
    @CdrVariable
    private double rtp_audio_in_quality_percentage;
    @CdrVariable
    private double rtp_audio_in_mos;
    @CdrVariable
    private double rtp_audio_out_raw_bytes;
    @CdrVariable
    private double rtp_audio_out_media_bytes;
    @CdrVariable
    private double rtp_audio_out_packet_count;
    @CdrVariable
    private double rtp_audio_out_media_packet_count;
    @CdrVariable
    private double rtp_audio_out_skip_packet_count;
    @CdrVariable
    private double rtp_audio_out_dtmf_packet_count;
    @CdrVariable
    private double rtp_audio_out_cng_packet_count;
    @CdrVariable
    private double rtp_audio_rtcp_packet_count;
    @CdrVariable
    private double rtp_audio_rtcp_octet_count;

    @CdrCallflowProfile
    private String network_addr;
    @CdrVariable(value = FreeswitchVariables.tms_uuid)
    private String tms_uuid;
    @CdrVariable(value = FreeswitchVariables.tms_order)
    private String orderPower;

    @Enumerated(EnumType.STRING)
    @CdrCallflowProfile
    private FreeswitchContext context;

    @CdrVariable
    private Long start_uepoch; //Call start time in epoch microseconds.

    @CdrVariable
    private Long answer_uepoch; //Call answer time in epoch microseconds.

    @CdrVariable
    private Long end_uepoch; //Call end time in epoch microseconds.

    @CdrVariable
    private String sip_hangup_disposition; //This variable contains the value of who sent the SIP BYE message. see https://wiki.freeswitch.org/wiki/Variable_sip_hangup_disposition

    @CdrVariable
    private String last_bridge_hangup_cause;
    @CdrVariable
    private String sip_invite_failure_status;
    @CdrVariable
    private String sip_invite_failure_phrase;

    @CdrVariable
    private String bridge_hangup_cause; //https://wiki.freeswitch.org/wiki/Hangup_Causes

    @CdrVariable
    private String endpoint_disposition;

    @CdrVariable
    private Integer hangup_cause_q850; //https://wiki.freeswitch.org/wiki/Hangup_Causes

    @CdrVariable
    private String hangup_cause;

    @CdrVariable
    private Long progressmsec; // The amount of time in milliseconds between the sip invite and the sip 180 message. 180 Ringing Destination user agent received INVITE, and is alerting user of call.

    @CdrVariable
    private Long progress_mediamsec; //The amount of time in milliseconds between the sip invite and the sip 183 message. 183 Session in Progress

    @CdrVariable
    private String originate_disposition;

    @CdrVariable
    private String originating_leg_uuid;
    @CdrVariable
    private String ent_originate_aleg_uuid;

    @CdrVariable(value = FreeswitchVariables.ivr_step)
    private Integer ivr_step;

    @Column(length = 10485760)
    private String xml;

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

//    public DialerQueue getDialerQueue() {
//        return dialerQueue;
//    }
//
//    public void setDialerQueue(DialerQueue dialerQueue) {
//        this.dialerQueue = dialerQueue;
//    }
    public LocalDateTime getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(LocalDateTime createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public double getRtp_audio_in_jitter_min_variance() {
        return rtp_audio_in_jitter_min_variance;
    }

    public void setRtp_audio_in_jitter_min_variance(double rtp_audio_in_jitter_min_variance) {
        this.rtp_audio_in_jitter_min_variance = rtp_audio_in_jitter_min_variance;
    }

    public double getRtp_audio_in_jitter_max_variance() {
        return rtp_audio_in_jitter_max_variance;
    }

    public void setRtp_audio_in_jitter_max_variance(double rtp_audio_in_jitter_max_variance) {
        this.rtp_audio_in_jitter_max_variance = rtp_audio_in_jitter_max_variance;
    }

    public double getRtp_audio_in_jitter_loss_rate() {
        return rtp_audio_in_jitter_loss_rate;
    }

    public void setRtp_audio_in_jitter_loss_rate(double rtp_audio_in_jitter_loss_rate) {
        this.rtp_audio_in_jitter_loss_rate = rtp_audio_in_jitter_loss_rate;
    }

    public double getRtp_audio_in_jitter_burst_rate() {
        return rtp_audio_in_jitter_burst_rate;
    }

    public void setRtp_audio_in_jitter_burst_rate(double rtp_audio_in_jitter_burst_rate) {
        this.rtp_audio_in_jitter_burst_rate = rtp_audio_in_jitter_burst_rate;
    }

    public double getRtp_audio_in_mean_interval() {
        return rtp_audio_in_mean_interval;
    }

    public void setRtp_audio_in_mean_interval(double rtp_audio_in_mean_interval) {
        this.rtp_audio_in_mean_interval = rtp_audio_in_mean_interval;
    }

    public double getRtp_audio_in_flaw_total() {
        return rtp_audio_in_flaw_total;
    }

    public void setRtp_audio_in_flaw_total(double rtp_audio_in_flaw_total) {
        this.rtp_audio_in_flaw_total = rtp_audio_in_flaw_total;
    }

    public double getRtp_audio_in_quality_percentage() {
        return rtp_audio_in_quality_percentage;
    }

    public void setRtp_audio_in_quality_percentage(double rtp_audio_in_quality_percentage) {
        this.rtp_audio_in_quality_percentage = rtp_audio_in_quality_percentage;
    }

    public double getRtp_audio_in_mos() {
        return rtp_audio_in_mos;
    }

    public void setRtp_audio_in_mos(double rtp_audio_in_mos) {
        this.rtp_audio_in_mos = rtp_audio_in_mos;
    }

    public double getRtp_audio_out_raw_bytes() {
        return rtp_audio_out_raw_bytes;
    }

    public void setRtp_audio_out_raw_bytes(double rtp_audio_out_raw_bytes) {
        this.rtp_audio_out_raw_bytes = rtp_audio_out_raw_bytes;
    }

    public double getRtp_audio_out_media_bytes() {
        return rtp_audio_out_media_bytes;
    }

    public void setRtp_audio_out_media_bytes(double rtp_audio_out_media_bytes) {
        this.rtp_audio_out_media_bytes = rtp_audio_out_media_bytes;
    }

    public double getRtp_audio_out_packet_count() {
        return rtp_audio_out_packet_count;
    }

    public void setRtp_audio_out_packet_count(double rtp_audio_out_packet_count) {
        this.rtp_audio_out_packet_count = rtp_audio_out_packet_count;
    }

    public double getRtp_audio_out_media_packet_count() {
        return rtp_audio_out_media_packet_count;
    }

    public void setRtp_audio_out_media_packet_count(double rtp_audio_out_media_packet_count) {
        this.rtp_audio_out_media_packet_count = rtp_audio_out_media_packet_count;
    }

    public double getRtp_audio_out_skip_packet_count() {
        return rtp_audio_out_skip_packet_count;
    }

    public void setRtp_audio_out_skip_packet_count(double rtp_audio_out_skip_packet_count) {
        this.rtp_audio_out_skip_packet_count = rtp_audio_out_skip_packet_count;
    }

    public double getRtp_audio_out_dtmf_packet_count() {
        return rtp_audio_out_dtmf_packet_count;
    }

    public void setRtp_audio_out_dtmf_packet_count(double rtp_audio_out_dtmf_packet_count) {
        this.rtp_audio_out_dtmf_packet_count = rtp_audio_out_dtmf_packet_count;
    }

    public double getRtp_audio_out_cng_packet_count() {
        return rtp_audio_out_cng_packet_count;
    }

    public void setRtp_audio_out_cng_packet_count(double rtp_audio_out_cng_packet_count) {
        this.rtp_audio_out_cng_packet_count = rtp_audio_out_cng_packet_count;
    }

    public double getRtp_audio_rtcp_packet_count() {
        return rtp_audio_rtcp_packet_count;
    }

    public void setRtp_audio_rtcp_packet_count(double rtp_audio_rtcp_packet_count) {
        this.rtp_audio_rtcp_packet_count = rtp_audio_rtcp_packet_count;
    }

    public double getRtp_audio_rtcp_octet_count() {
        return rtp_audio_rtcp_octet_count;
    }

    public void setRtp_audio_rtcp_octet_count(double rtp_audio_rtcp_octet_count) {
        this.rtp_audio_rtcp_octet_count = rtp_audio_rtcp_octet_count;
    }

    public Long getStart_uepoch() {
        return start_uepoch;
    }

    public LocalDateTime getStartTime() {
        if (start_uepoch == null) {
            return null;
        }
        return new LocalDateTime(start_uepoch / 1000);
    }

    public void setStart_uepoch(Long start_uepoch) {
        this.start_uepoch = start_uepoch;
    }

    public Long getAnswer_uepoch() {
        return answer_uepoch;
    }

    public LocalDateTime getAnswerTime() {
        if (answer_uepoch == null) {
            return null;
        }
        return new LocalDateTime(answer_uepoch / 1000);
    }

    public void setAnswer_uepoch(Long answer_uepoch) {
        this.answer_uepoch = answer_uepoch;
    }

    public Long getEnd_uepoch() {
        return end_uepoch;
    }

    public LocalDateTime getEndTime() {
        if (end_uepoch == null) {
            return null;
        }
        return new LocalDateTime(end_uepoch / 1000);
    }

    public void setEnd_uepoch(Long end_uepoch) {
        this.end_uepoch = end_uepoch;
    }

    public String getSip_hangup_disposition() {
        return sip_hangup_disposition;
    }

    public void setSip_hangup_disposition(String sip_hangup_disposition) {
        this.sip_hangup_disposition = sip_hangup_disposition;
    }

    public String getLast_bridge_hangup_cause() {
        return last_bridge_hangup_cause;
    }

    public void setLast_bridge_hangup_cause(String last_bridge_hangup_cause) {
        this.last_bridge_hangup_cause = last_bridge_hangup_cause;
    }

    public String getSip_invite_failure_status() {
        return sip_invite_failure_status;
    }

    public void setSip_invite_failure_status(String sip_invite_failure_status) {
        this.sip_invite_failure_status = sip_invite_failure_status;
    }

    public String getSip_invite_failure_phrase() {
        return sip_invite_failure_phrase;
    }

    public void setSip_invite_failure_phrase(String sip_invite_failure_phrase) {
        this.sip_invite_failure_phrase = sip_invite_failure_phrase;
    }

    public String getBridge_hangup_cause() {
        return bridge_hangup_cause;
    }

    public void setBridge_hangup_cause(String bridge_hangup_cause) {
        this.bridge_hangup_cause = bridge_hangup_cause;
    }

    public String getEndpoint_disposition() {
        return endpoint_disposition;
    }

    public void setEndpoint_disposition(String endpoint_disposition) {
        this.endpoint_disposition = endpoint_disposition;
    }

    public Integer getHangup_cause_q850() {
        return hangup_cause_q850;
    }

    public void setHangup_cause_q850(Integer hangup_cause_q850) {
        this.hangup_cause_q850 = hangup_cause_q850;
    }

    public String getHangup_cause() {
        return hangup_cause;
    }

    public void setHangup_cause(String hangup_cause) {
        this.hangup_cause = hangup_cause;
    }

    public Long getProgressmsec() {
        return progressmsec;
    }

    public void setProgressmsec(Long progressmsec) {
        this.progressmsec = progressmsec;
    }

    public Long getProgress_mediamsec() {
        return progress_mediamsec;
    }

    public void setProgress_mediamsec(Long progress_mediamsec) {
        this.progress_mediamsec = progress_mediamsec;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCaller_id_name() {
        return caller_id_name;
    }

    public void setCaller_id_name(String caller_id_name) {
        this.caller_id_name = caller_id_name;
    }

    public String getCaller_id_number() {
        if (caller_id_number == null) {
            return getAni();
        }
        return caller_id_number.replace("+1", "");
    }

    public void setCaller_id_number(String caller_id_number) {
        this.caller_id_number = caller_id_number;
    }

    public String getAni() {
        if (ani != null) {
            return ani.replace("+1", "");
        } else {
            return ani;
        }
    }

    public void setAni(String ani) {

        this.ani = ani;

    }

    public String getCallee_id_name() {
        return callee_id_name;
    }

    public void setCallee_id_name(String callee_id_name) {
        this.callee_id_name = callee_id_name;
    }

    public String getCallee_id_number() {
        if (destination_number == null) {
            return callee_id_number;
        }
        return destination_number;
    }

    public void setCallee_id_number(String callee_id_number) {
        this.callee_id_number = callee_id_number;
    }

    public String getDestination_number() {
        return destination_number;
    }

    public void setDestination_number(String destination_number) {
        this.destination_number = destination_number;
    }

    public String getEffective_caller_id_number() {
        return effective_caller_id_number;
    }

    public void setEffective_caller_id_number(String effective_caller_id_number) {
        this.effective_caller_id_number = effective_caller_id_number;
    }

    public CallerId getCallerId() {
        return callerId;
    }

    public void setCallerId(CallerId callerId) {
        this.callerId = callerId;
    }

    public String getCall_uuid() {
        return call_uuid;
    }

    public void setCall_uuid(String call_uuid) {
        this.call_uuid = call_uuid;
    }

    public String getCdr_uuid() {
        return cdr_uuid;
    }

    public void setCdr_uuid(String cdr_uuid) {
        this.cdr_uuid = cdr_uuid;
    }

    public CallDirection getCallDirection() {
        return callDirection;
    }

    public void setCallDirection(CallDirection callDirection) {
        this.callDirection = callDirection;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public String getBorrowerLastName() {
        return borrowerLastName;
    }

    public void setBorrowerLastName(String borrowerLastName) {
        this.borrowerLastName = borrowerLastName;
    }

    public String getBorrowerFirstName() {
        return borrowerFirstName;
    }

    public void setBorrowerFirstName(String borrowerFirstName) {
        this.borrowerFirstName = borrowerFirstName;
    }

    public String getBorrowerPhone() {
        return borrowerPhone;
    }

    public void setBorrowerPhone(String borrowerPhone) {
        this.borrowerPhone = borrowerPhone;
    }

    public Boolean getAutoAswer() {
        return autoAswer;
    }

    public void setAutoAswer(Boolean autoAswer) {
        this.autoAswer = autoAswer;
    }

    public Boolean getDialer() {
        return dialer;
    }

    public void setDialer(Boolean dialer) {
        this.dialer = dialer;
    }

    public Long getDialerQueueId() {
        return dialerQueueId;
    }

    public void setDialerQueueId(Long dialerQueueId) {
        this.dialerQueueId = dialerQueueId;
    }

    public Long getAgentGroupId() {
        return agentGroupId;
    }

    public void setAgentGroupId(Long agentGroupId) {
        this.agentGroupId = agentGroupId;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getAnswermsec() {
        return answermsec;
    }

    public void setAnswermsec(Long answermsec) {
        this.answermsec = answermsec;
    }

    public Long getWaitmsec() {
        return waitmsec;
    }

    public void setWaitmsec(Long waitmsec) {
        this.waitmsec = waitmsec;
    }

    public String getSwitchname() {
        return switchname;
    }

    public void setSwitchname(String switchname) {
        this.switchname = switchname;
    }

    public String getNetwork_addr() {
        return network_addr;
    }

    public void setNetwork_addr(String network_addr) {
        this.network_addr = network_addr;
    }

    public String getTms_uuid() {
        return tms_uuid;
    }

    public void setTms_uuid(String tms_uuid) {
        this.tms_uuid = tms_uuid;
    }

    public String getOrderPower() {
        return orderPower;
    }

    public void setOrderPower(String orderPower) {
        this.orderPower = orderPower;
    }

    public FreeswitchContext getContext() {
        return context;
    }

    public void setContext(FreeswitchContext context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "CDR{" + "pk=" + pk + ", createTimestamp=" + createTimestamp + ", username=" + username + ", caller_id_name=" + caller_id_name + ", caller_id_number=" + caller_id_number + ", callee_id_name=" + callee_id_name + ", callee_id_number=" + callee_id_number + ", effective_caller_id_number=" + effective_caller_id_number + ", callerId=" + callerId + ", call_uuid=" + call_uuid + ", callDirection=" + callDirection + ", loanId=" + loanId + ", borrowerLastName=" + borrowerLastName + ", borrowerFirstName=" + borrowerFirstName + ", autoAswer=" + autoAswer + ", dialer=" + dialer + ", dialerQueueId=" + dialerQueueId + ", duration=" + duration + ", answermsec=" + answermsec + ", waitmsec=" + waitmsec + ", switchname=" + switchname + ", rtp_audio_in_jitter_min_variance=" + rtp_audio_in_jitter_min_variance + ", rtp_audio_in_jitter_max_variance=" + rtp_audio_in_jitter_max_variance + ", rtp_audio_in_jitter_loss_rate=" + rtp_audio_in_jitter_loss_rate + ", rtp_audio_in_jitter_burst_rate=" + rtp_audio_in_jitter_burst_rate + ", rtp_audio_in_mean_interval=" + rtp_audio_in_mean_interval + ", rtp_audio_in_flaw_total=" + rtp_audio_in_flaw_total + ", rtp_audio_in_quality_percentage=" + rtp_audio_in_quality_percentage + ", rtp_audio_in_mos=" + rtp_audio_in_mos + ", rtp_audio_out_raw_bytes=" + rtp_audio_out_raw_bytes + ", rtp_audio_out_media_bytes=" + rtp_audio_out_media_bytes + ", rtp_audio_out_packet_count=" + rtp_audio_out_packet_count + ", rtp_audio_out_media_packet_count=" + rtp_audio_out_media_packet_count + ", rtp_audio_out_skip_packet_count=" + rtp_audio_out_skip_packet_count + ", rtp_audio_out_dtmf_packet_count=" + rtp_audio_out_dtmf_packet_count + ", rtp_audio_out_cng_packet_count=" + rtp_audio_out_cng_packet_count + ", rtp_audio_rtcp_packet_count=" + rtp_audio_rtcp_packet_count + ", rtp_audio_rtcp_octet_count=" + rtp_audio_rtcp_octet_count + ", network_addr=" + network_addr + ", tms_uuid=" + tms_uuid + ", orderPower=" + orderPower + ", context=" + context + ", start_uepoch=" + start_uepoch + ", answer_uepoch=" + answer_uepoch + ", end_uepoch=" + end_uepoch + ", sip_hangup_disposition=" + sip_hangup_disposition + ", bridge_hangup_cause=" + bridge_hangup_cause + ", hangup_cause_q850=" + hangup_cause_q850 + ", progressmsec=" + progressmsec + ", progress_mediamsec=" + progress_mediamsec + '}';
    }

    public String getOriginate_disposition() {
        return originate_disposition;
    }

    public void setOriginate_disposition(String originate_disposition) {
        this.originate_disposition = originate_disposition;
    }

    public String getOriginating_leg_uuid() {
        return originating_leg_uuid;
    }

    public void setOriginating_leg_uuid(String originating_leg_uuid) {
        this.originating_leg_uuid = originating_leg_uuid;
    }

    public String getEnt_originate_aleg_uuid() {
        return ent_originate_aleg_uuid;
    }

    public void setEnt_originate_aleg_uuid(String ent_originate_aleg_uuid) {
        this.ent_originate_aleg_uuid = ent_originate_aleg_uuid;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public Long getProgressusec() {
        return progressusec;
    }

    public void setProgressusec(Long progressusec) {
        this.progressusec = progressusec;
    }

    public String getDestination_number_profile_min() {
        return destination_number_profile_min;
    }

    public void setDestination_number_profile_min(String destination_number_profile_min) {
        this.destination_number_profile_min = destination_number_profile_min;
    }

    public Integer getIvr_step() {
        return ivr_step;
    }

    public void setIvr_step(Integer ivr_step) {
        this.ivr_step = ivr_step;
    }

    public static final CDR valueOf(String value) {
        return CONVERTER.convert(value);
    }

    public String getRecrodingUploadTms() {
        return recrodingUploadTms;
    }

    public void setRecrodingUploadTms(String recrodingUploadTms) {
        this.recrodingUploadTms = recrodingUploadTms;
    }

    public String getSip_local_network_addr() {
        return sip_local_network_addr;
    }

    public void setSip_local_network_addr(String sip_local_network_addr) {
        this.sip_local_network_addr = sip_local_network_addr;
    }

    public String getSip_call_id() {
        return sip_call_id;
    }

    public void setSip_call_id(String sip_call_id) {
        this.sip_call_id = sip_call_id;
    }
    
    
}
