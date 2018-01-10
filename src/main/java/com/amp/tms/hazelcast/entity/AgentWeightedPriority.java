/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.hazelcast.entity;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.amp.crm.embeddable.AgentWeightPriority;
import com.amp.crm.pojo.QueueAgentWeightPriority;
import com.amp.tms.enumerated.DialerType;
import java.io.IOException;
import java.util.Objects;
import javax.persistence.MappedSuperclass;

/**
 *
 * 
 */
@MappedSuperclass
public class AgentWeightedPriority extends WeightedPriority {

    private Boolean leader;
    private Boolean allowAfterHours;
    private Long groupPk;
    private Boolean primaryGroup;
    private DialerType dialerType;
    private Boolean isRunning;
    private Boolean hasWaitingCalls;

    public AgentWeightedPriority() {
    }

    public AgentWeightedPriority(Integer priority, Double weight, Boolean leader, Boolean allowAfterHours, Long groupPk, Boolean primaryGroup) {
        super(priority, weight);
        this.leader = leader;
        this.allowAfterHours = allowAfterHours;
        this.groupPk = groupPk;
        this.primaryGroup = primaryGroup;
    }

    public AgentWeightedPriority(AgentWeightedPriority awp) {
        copyFrom(awp);
    }

    public AgentWeightedPriority(AgentWeightPriority awp) {
        super(awp.getWeightedPriority());
        this.leader = awp.isLeader();
        this.allowAfterHours = awp.getAllowAfterHours();
        this.groupPk = awp.getGroupPk();
        this.primaryGroup = awp.isIsPrimaryGroup();
    }

    public AgentWeightedPriority(QueueAgentWeightPriority qawp) {
        super(qawp.getWeightedPriority());
        this.leader = qawp.getLeader();
        this.allowAfterHours = qawp.getAllowAfterHours();
        this.groupPk = qawp.getGroupPk();
        this.primaryGroup = qawp.getIsPrimary();
    }

    public final void copyFrom(AgentWeightedPriority awp) {
        super.copyFrom(awp);
        leader = awp.leader;
        allowAfterHours = awp.allowAfterHours;
        groupPk = awp.groupPk;
        primaryGroup = awp.primaryGroup;
        dialerType = awp.dialerType;
        isRunning = awp.isRunning;
        hasWaitingCalls = awp.hasWaitingCalls;
    }

    public Boolean getLeader() {
        return leader;
    }

    public void setLeader(Boolean leader) {
        this.leader = leader;
    }

    public Boolean getAllowAfterHours() {
        return allowAfterHours;
    }

    public void setAllowAfterHours(Boolean allowAfterHours) {
        this.allowAfterHours = allowAfterHours;
    }

    public Long getGroupPk() {
        return groupPk;
    }

    public void setGroupPk(Long groupPk) {
        this.groupPk = groupPk;
    }

    public Boolean getPrimaryGroup() {
        return primaryGroup;
    }

    public void setPrimaryGroup(Boolean primaryGroup) {
        this.primaryGroup = primaryGroup;
    }

    public DialerType getDialerType() {
        return dialerType;
    }

    public void setDialerType(DialerType dialerType) {
        this.dialerType = dialerType;
    }

    public Boolean getIsRunning() {
        return isRunning;
    }

    public void setIsRunning(Boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean isRunning() {
        return isRunning != null && isRunning;
    }

    public Boolean getHasWaitingCalls() {
        return hasWaitingCalls;
    }

    public boolean hasWaitingCalls() {
        return hasWaitingCalls != null && hasWaitingCalls;
    }

    public void setHasWaitingCalls(Boolean hasWaitingCalls) {
        this.hasWaitingCalls = hasWaitingCalls;
    }

    @Override
    public boolean valueEquals(Object obj) {
        if (!super.valueEquals(obj)) {
            return false;
        }
        final AgentWeightedPriority other = (AgentWeightedPriority) obj;
        if (!Objects.equals(this.leader, other.leader)) {
            return false;
        }
        if (!Objects.equals(this.allowAfterHours, other.allowAfterHours)) {
            return false;
        }
        if (!Objects.equals(this.groupPk, other.groupPk)) {
            return false;
        }
        if (!Objects.equals(this.primaryGroup, other.primaryGroup)) {
            return false;
        }
        if (!Objects.equals(this.dialerType, other.dialerType)) {
            return false;
        }
        if (!Objects.equals(this.isRunning, other.isRunning)) {
            return false;
        }
        if (!Objects.equals(this.hasWaitingCalls, other.hasWaitingCalls)) {
            return false;
        }
        return true;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        int booleans = 0;
        if (leader != null) {
            if (leader) {
                booleans |= 1 << 0;
            } else {
                booleans |= 1 << 1;
            }
        }
        if (allowAfterHours != null) {
            if (allowAfterHours) {
                booleans |= 1 << 2;
            } else {
                booleans |= 1 << 3;
            }
        }
        if (primaryGroup != null) {
            if (primaryGroup) {
                booleans |= 1 << 4;
            } else {
                booleans |= 1 << 5;
            }
        }
        if (isRunning != null) {
            if (isRunning) {
                booleans |= 1 << 6;
            } else {
                booleans |= 1 << 7;
            }
        }
        if (hasWaitingCalls != null) {
            if (hasWaitingCalls) {
                booleans |= 1 << 8;
            } else {
                booleans |= 1 << 9;
            }
        }
        booleans |= (groupPk != null) ? 1 << 10 : 0;
        out.writeShort(booleans);
        if (groupPk != null) {
            out.writeLong(groupPk);
        }
        DialerType.write(out, dialerType);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        short booleans = in.readShort();
        switch (3 & booleans) {
            case 0:
                leader = null;
                break;
            case 1:
                leader = true;
                break;
            case 2:
                leader = false;
                break;
        }
        switch (3 & (booleans >> 2)) {
            case 0:
                allowAfterHours = null;
                break;
            case 1:
                allowAfterHours = true;
                break;
            case 2:
                allowAfterHours = false;
                break;
        }
        switch (3 & (booleans >> 4)) {
            case 0:
                primaryGroup = null;
                break;
            case 1:
                primaryGroup = true;
                break;
            case 2:
                primaryGroup = false;
                break;
        }
        switch (3 & (booleans >> 6)) {
            case 0:
                isRunning = null;
                break;
            case 1:
                isRunning = true;
                break;
            case 2:
                isRunning = false;
                break;
        }
        switch (3 & (booleans >> 8)) {
            case 0:
                hasWaitingCalls = null;
                break;
            case 1:
                hasWaitingCalls = true;
                break;
            case 2:
                hasWaitingCalls = false;
                break;
        }
        boolean groupPkNotNull = ((1 << 10) & booleans) > 0;
        if (groupPkNotNull) {
            groupPk = in.readLong();
        } else {
            groupPk = null;
        }
        dialerType = DialerType.read(in);
    }

}
