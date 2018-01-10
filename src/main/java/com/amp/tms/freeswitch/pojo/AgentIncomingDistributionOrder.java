/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.pojo;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.amp.crm.constants.PopupDisplayMode;
import com.amp.crm.db.entity.base.dialer.InboundDialerQueueSettings;
import com.amp.crm.pojo.TMSCallDetails;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.hazelcast.entity.AgentWeightedPriority;
import com.amp.tms.pojo.BorrowerInfo;
import com.amp.tms.utility.GsonUtility;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 
 */
public class AgentIncomingDistributionOrder implements Serializable {

    private int transaction = LocalDateTime.now().getMillisOfDay();

    private static final Logger LOG = LoggerFactory.getLogger(AgentIncomingDistributionOrder.class);

    @Expose
    private Integer defaultExtension = null;

    private List<AgentOrder> agentOrders = new ArrayList<>();

    @Expose
    private Boolean isAutoAnswer;
    @Expose
    private PopupDisplayMode popupDisplayMode;
    @Expose
    private BorrowerInfo borrowerInfo;
    @Expose
    private Long maxDelayBeforeAgentAnswer;
    @Expose
    private InboundDialerQueueSettings settings;
    @Expose
    private boolean directLine = false;
    @Expose
    private boolean foundMultiRecords = false;

    @Expose
    private String multiLine = "";

    @Expose
    private String incomingCallOrderSelected = "";

    @Expose
    private Long dialerQueuePK;

    @Expose
    private String dialerQueueName;

    @Expose
    private TMSCallDetails callDetails;

    private String logOfCallOrder = "";

    public AgentIncomingDistributionOrder() {
    }

    private AgentOrder getAgentOrder(int extension) {
        for (AgentOrder agent : agentOrders) {
            if (extension == agent.agent.getExtension()) {
                return agent;
            }
        }
        return null;
    }

    public void addAgent(AgentTMS agent, Boolean multiLine, String incomingCallOrderSelected) {
        addAgent(agent, multiLine, null, incomingCallOrderSelected);
    }

    public void addAgent(AgentTMS agent, Boolean multiLine, Long groupPk, String incomingCallOrderSelected) {
        AgentOrder order = getAgentOrder(agent.getExtension());
        if (order != null) {
            return;
        }
        addLogOfCallOrder(agent.getExtension(), multiLine, incomingCallOrderSelected);
        agentOrders.add(new AgentOrder(agent, multiLine, groupPk));
    }

    public void addAgentOnTop(AgentTMS agent, Boolean multiLine, String incomingCallOrderSelected) {
        AgentOrder order = getAgentOrder(agent.getExtension());
        if (order != null) {
            agentOrders.remove(order);
        }
        addLogOfCallOrder(agent.getExtension(), multiLine, incomingCallOrderSelected);
        agentOrders.add(0, new AgentOrder(agent, multiLine, null));
    }

    public boolean isDirectLine() {
        return directLine;
    }

    public void setDirectLine(boolean directLine) {
        this.directLine = directLine;
    }

    public Boolean getIsInlineForAgent(Integer ext) {
        AgentOrder agent = getAgentOrder(ext);
        if (agent != null) {
            return agent.multiline;
        }
        LOG.info("Counld not find agent in ado agent to multiline map {} ", ext);
        return false;
    }

    public Long getGroupPkForAgent(Integer ext) {
        AgentOrder order = getAgentOrder(ext);
        if (order != null) {
            return order.groupPk;
        }
        return null;
    }

    public void addAgents(List<AgentTMS> agents, Boolean multiLine, String incomingCallOrderSelected, Map<String, AgentWeightedPriority> awpMap) {
        for (AgentTMS agent : agents) {
            AgentWeightedPriority awp = awpMap.get(agent.getUserName());
            Long groupPk;
            if (awp != null) {
                groupPk = awp.getGroupPk();
            } else {
                LOG.warn("Unable to find AgentWeightedPriority for user [{}], in list {}", agent.getUserName(), awpMap.keySet());
                groupPk = null;
            }
            addAgent(agent, multiLine, groupPk, incomingCallOrderSelected);
        }
    }

    public Integer getDefaultExtension() {
        return defaultExtension;
    }

    public void setDefaultExtension(Integer defaultExtension) {
        this.defaultExtension = defaultExtension;
    }

    public List<AgentTMS> getAgents() {
        List<AgentTMS> agents = new ArrayList<>();
        for (AgentOrder agentOrder : agentOrders) {
            agents.add(agentOrder.agent);
        }
        return agents;
    }

    public Boolean getIsAutoAnswer() {
        if (isAutoAnswer == null) {
            return Boolean.FALSE;
        }
        return isAutoAnswer;
    }

    public void setIsAutoAnswer(Boolean isAutoAnswer) {
        this.isAutoAnswer = isAutoAnswer;
    }

    public PopupDisplayMode getPopupDisplayMode() {
        return popupDisplayMode;
    }

    public void setPopupDisplayMode(PopupDisplayMode popupDisplayMode) {
        this.popupDisplayMode = popupDisplayMode;
    }

    public BorrowerInfo getBorrowerInfo() {
        if (borrowerInfo == null) {
            borrowerInfo = new BorrowerInfo();
        }
        return borrowerInfo;
    }

    public void setBorrowerInfo(BorrowerInfo borrowerInfo) {
        this.borrowerInfo = borrowerInfo;
    }

    public Long getMaxDelayBeforeAgentAnswer() {
        return maxDelayBeforeAgentAnswer;
    }

    public void setMaxDelayBeforeAgentAnswer(Long maxDelayBeforeAgentAnswer) {
        this.maxDelayBeforeAgentAnswer = maxDelayBeforeAgentAnswer;
    }

    public InboundDialerQueueSettings getSettings() {
        return settings;
    }

    public void setSettings(InboundDialerQueueSettings settings) {
        this.settings = settings;
    }

    public TMSCallDetails getCallDetails() {
        return callDetails;
    }

    public void setCallDetails(TMSCallDetails callDetails) {
        this.callDetails = callDetails;
    }

    public boolean isFoundMultiRecords() {
        return foundMultiRecords;
    }

    public void setFoundMultiRecords(boolean foundMultiRecords) {
        this.foundMultiRecords = foundMultiRecords;
    }

    public Long getDialerQueuePK() {
        return dialerQueuePK;
    }

    public void setDialerQueuePK(Long dialerQueuePK) {
        this.dialerQueuePK = dialerQueuePK;
    }

    public String getDialerQueueName() {
        return dialerQueueName;
    }

    public void setDialerQueueName(String dialerQueueName) {
        this.dialerQueueName = dialerQueueName;
    }

    public String getMultiLine() {
        return multiLine;
    }

    public void setMultiLine(String multiLine) {
        if (this.multiLine.equals("")) {
            this.multiLine = multiLine;
        } else {
            this.multiLine = this.multiLine + "," + multiLine;
        }
    }

    public String getIncomingCallOrderSelected() {
        return incomingCallOrderSelected;
    }

    public void setIncomingCallOrderSelected(String incomingCallOrderSelected) {
        if (this.incomingCallOrderSelected.equals("")) {
            this.incomingCallOrderSelected = incomingCallOrderSelected;
        } else {
            this.incomingCallOrderSelected = this.incomingCallOrderSelected + "," + incomingCallOrderSelected;
        }

    }

    public String getLogOfCallOrder() {
        return logOfCallOrder;
    }

    public void setLogOfCallOrder(String logOfCallOrder) {
        this.logOfCallOrder = logOfCallOrder;
    }

    public void addLogOfCallOrder(int ext, boolean multiLine, String incomingCallOrderSelected) {
        StringBuilder xml = new StringBuilder();
        xml.append("<item ");
        xml.append("tx=\"");
        xml.append(transaction);
        xml.append("\" ext=\"");
        xml.append(ext);
        xml.append("\" multi=\"");
        xml.append(multiLine);
        xml.append("\" icos=\"");
        xml.append(incomingCallOrderSelected);
        xml.append("\" />");
        if (this.logOfCallOrder.equals("")) {
            this.logOfCallOrder = xml.toString();
        } else {
            this.logOfCallOrder = this.logOfCallOrder + xml.toString();
        }
    }

    public String toJson() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }

    private static class AgentOrder implements Serializable {

        private AgentTMS agent;
        private Boolean multiline;
        private Long groupPk;

        public AgentOrder() {
        }

        public AgentOrder(AgentTMS agent, Boolean multiline, Long groupPk) {
            this.agent = agent;
            this.multiline = multiline;
            this.groupPk = groupPk;
        }

    }

}
