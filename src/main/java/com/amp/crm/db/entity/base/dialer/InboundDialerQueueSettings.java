/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.dialer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amp.crm.constants.CallRoutingOption;
import com.amp.crm.embeddable.AgentCallOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Entity
@Table(schema = "crm")
//@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
//@AuditTable(value = "dialer_ib_setting_history", schema = "svc")

public class InboundDialerQueueSettings extends DialerQueueSettings {

    private Long maxDelayBeforeAgentAnswer;

    @ElementCollection(targetClass = AgentCallOrder.class, fetch = FetchType.EAGER)
    @CollectionTable(schema = "crm", name = "agent_call_order", joinColumns = @JoinColumn(name = "dialer_queue_pk"))
    @OrderColumn(name = "order_index")
    private List<AgentCallOrder> agentCallOrder = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private CallRoutingOption callRoutingOption;

    private Double roundRobinCutoffPercent;
    private Boolean disableSecondaryAgentsCallRouting;
    private Boolean forceVoicemail;
    
    public Long getMaxDelayBeforeAgentAnswer() {
        return maxDelayBeforeAgentAnswer;
    }

    public void setMaxDelayBeforeAgentAnswer(Long maxDelayBeforeAgentAnswer) {
        this.maxDelayBeforeAgentAnswer = maxDelayBeforeAgentAnswer;
    }

    public List<AgentCallOrder> getAgentCallOrder() {
        return agentCallOrder;
    }

    public void setAgentCallOrder(List<AgentCallOrder> agentCallOrder) {
        this.agentCallOrder = agentCallOrder;
    }

    public CallRoutingOption getCallRoutingOption() {
        return callRoutingOption;
    }

    public void setCallRoutingOption(CallRoutingOption callRoutingOption) {
        this.callRoutingOption = callRoutingOption;
    }

    public Double getRoundRobinCutoffPercent() {
        return roundRobinCutoffPercent;
    }

    public void setRoundRobinCutoffPercent(Double roundRobinCutoffPercent) {
        this.roundRobinCutoffPercent = roundRobinCutoffPercent;
    }

    public Boolean getDisableSecondaryAgentsCallRouting() {
        return disableSecondaryAgentsCallRouting;
    }

    public void setDisableSecondaryAgentsCallRouting(Boolean disableSecondaryAgentsCallRouting) {
        this.disableSecondaryAgentsCallRouting = disableSecondaryAgentsCallRouting;
    }

    public Boolean isForceVoicemail() {
        return forceVoicemail;
    }

    public void setForceVoicemail(Boolean forceVoicemail) {
        this.forceVoicemail = forceVoicemail;
    }
    
    @Override
    public void setDialerQueueForSettings(DialerQueue queue) {
        ((InboundDialerQueue)queue).setDialerQueueSettings(this);
    }
    
    @Override
    public String toStringForHistory() {
        return "dialerQueuePk=" + getDialerQueuePk()  
                + ", popupDisplayMode=" + getPopupDisplayMode() 
                + ", autoAnswerEnabled=" + isAutoAnswerEnabled() 
                + ", priority=" + getWeightedPriority().getPriority() 
                + ", weight=" + getWeightedPriority().getWeight() 
                + ", idleMaxMinutes=" + getIdleMaxMinutes() 
                + ", wrapMaxMinutes=" + getWrapMaxMinutes() 
                + ", startTime=" + getStartTime() 
                + ", endTime=" + getEndTime() 
                + ", " + getDialerSchedule().toString() //not being audited
                + ", maxDelayBeforeAgentAnswer=" + maxDelayBeforeAgentAnswer 
                + ", agentCallOrder=" + agentCallOrder.toString() //not being audited
                + ", callRoutingOption=" + callRoutingOption 
                + ", roundRobinCutoffPercent=" + roundRobinCutoffPercent 
                + ", disableSecondaryAgentsCallRouting=" + disableSecondaryAgentsCallRouting;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    public String difference(Object obj) {
        if (obj == null) {
            return "New settings are null";
        }
        if (getClass() != obj.getClass()) {
            return "ClassName differs";
        }
        StringBuilder sb = new StringBuilder();
        final InboundDialerQueueSettings other = (InboundDialerQueueSettings) obj;
        sb.append(super.difference(obj));
        if (!Objects.equals(this.maxDelayBeforeAgentAnswer, other.maxDelayBeforeAgentAnswer)) {
            sb.append("\nMaxDelayBeforeAgentAnswer [oldValue : ").append(this.maxDelayBeforeAgentAnswer).append("; newValue : ").append(other.maxDelayBeforeAgentAnswer).append("]");
        }
        if (!Objects.equals(this.agentCallOrder, other.agentCallOrder)) {
            sb.append("\nAgentCallOrder [oldValue : ").append(this.agentCallOrder).append("; newValue : ").append(other.agentCallOrder).append("]");
        }
        if (this.callRoutingOption != other.callRoutingOption) {
            sb.append("\nCallRoutingOption [oldValue : ").append(this.callRoutingOption).append("; newValue : ").append(other.callRoutingOption).append("]");
        }
        if (!Objects.equals(this.roundRobinCutoffPercent, other.roundRobinCutoffPercent)) {
            sb.append("\nRoundRobinCutoffPercent [oldValue : ").append(this.roundRobinCutoffPercent).append("; newValue : ").append(other.roundRobinCutoffPercent).append("]");
        }
        if (!Objects.equals(this.forceVoicemail, other.forceVoicemail)) {
            sb.append("\nForceVoicemail [oldValue : ").append(this.forceVoicemail).append("; newValue : ").append(other.forceVoicemail).append("]");
        }
        if (!Objects.equals(this.disableSecondaryAgentsCallRouting, other.disableSecondaryAgentsCallRouting)) {
            sb.append("\nDisableSecondaryAgentsCallRouting [oldValue : ").append(this.disableSecondaryAgentsCallRouting).append("; newValue : ").append(other.disableSecondaryAgentsCallRouting).append("]");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return String.valueOf(this);
        }
    }
    

}

