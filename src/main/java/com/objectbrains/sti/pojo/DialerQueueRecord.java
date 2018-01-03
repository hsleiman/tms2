package com.objectbrains.sti.pojo;

import com.objectbrains.sti.db.entity.base.dialer.DialerQueueSettings;
import com.objectbrains.sti.db.entity.disposition.CallDispositionGroup;
import com.objectbrains.sti.embeddable.AgentWeightPriority;
import com.objectbrains.sti.embeddable.WeightedPriority;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class DialerQueueRecord<T extends DialerQueueSettings> {
    
    private long dqPk;
    private List<AgentWeightPriority> agentWeightPriorityList = new ArrayList<>();
    private WeightedPriority weightedPriority;
    private CallDispositionGroup callDispositionGroup;

    public DialerQueueRecord(long dqPk) {
        this.dqPk = dqPk;
    }
    
    public DialerQueueRecord() {}
    
    public abstract T getDialerQueueSettings();

    public abstract void setDialerQueueSettings(T dialerQueueSettings);
    
    public long getDqPk() {
        return dqPk;
    }

    public void setDqPk(long dqPk) {
        this.dqPk = dqPk;
    }

    public List<AgentWeightPriority> getAgentWeightPriorityList() {
        return agentWeightPriorityList;
    }

    public void setAgentWeightPriorityList(List<AgentWeightPriority> agentWeightPriorityList) {
        this.agentWeightPriorityList = agentWeightPriorityList;
    } 

    public WeightedPriority getWeightedPriority() {
        return weightedPriority;
    }

    public void setWeightedPriority(WeightedPriority weightedPriority) {
        this.weightedPriority = weightedPriority;
    }

    public CallDispositionGroup getCallDispositionGroup() {
        return callDispositionGroup;
    }

    public void setCallDispositionGroup(CallDispositionGroup callDispositionGroup) {
        this.callDispositionGroup = callDispositionGroup;
    }
    
    @Override
    public String toString(){
        return ToStringBuilder.reflectionToString(this);
    }
    
}
