/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.pojo;

import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.objectbrains.ams.iws.User;
import com.amp.tms.enumerated.AgentState;
import com.amp.tms.enumerated.SetAgentState;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.hazelcast.entity.AgentCall;
import com.amp.tms.hazelcast.entity.AgentStats;
import java.io.IOException;
import java.io.Serializable;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */
public class AgentDirectory implements DataSerializable, Serializable {

    @Expose
    private Integer ext;
    @Expose
    private String firstName;
    @Expose
    private String lastName;
    @Expose
    private String username;
    @Expose
    private LocalDateTime lastAccessTime;
    @Expose
    private AgentState agentState;
    @Expose
    private String borrowerFirstName;
    @Expose
    private String borrowerLastName;
    @Expose
    private Long loanId;

    public AgentDirectory() {

    }

    public AgentDirectory(AgentTMS agent, User user, AgentStats agentStats, AgentCall activeCall) {
        this.ext = user.getExtension();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.username = user.getUserName();
        this.lastAccessTime = user.getLastAccessTime();
        if (agentStats == null || agentStats.getState() == null) {
            this.agentState = AgentState.OFFLINE;
        } else {
            SetAgentState setAgentState = agent.getStatusExt();
            this.agentState = setAgentState != null ? setAgentState.getAgentState() : agentStats.getState();
        }
        if (activeCall != null && activeCall.getBorrowerInfo() != null) {
            BorrowerInfo borrowerInfo = activeCall.getBorrowerInfo();
            this.borrowerFirstName = borrowerInfo.getBorrowerFirstName();
            this.borrowerLastName = borrowerInfo.getBorrowerLastName();
            this.loanId = borrowerInfo.getLoanId();
        }
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(ext);
        out.writeUTF(firstName);
        out.writeUTF(lastName);
        out.writeUTF(username);
        out.writeObject(lastAccessTime);
        out.writeObject(agentState);
        out.writeUTF(borrowerFirstName);
        out.writeUTF(borrowerLastName);
        out.writeObject(loanId);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        ext = in.readObject();
        firstName = in.readUTF();
        lastName = in.readUTF();
        username = in.readUTF();
        lastAccessTime = in.readObject();
        agentState = in.readObject();
        borrowerFirstName = in.readUTF();
        borrowerLastName = in.readUTF();
        loanId = in.readObject();
    }

    public AgentState getAgentState() {
        return agentState;
    }

    public void setAgentState(AgentState agentState) {
        this.agentState = agentState;
    }

    public String getBorrowerFirstName() {
        return borrowerFirstName;
    }

    public void setBorrowerFirstName(String borrowerFirstName) {
        this.borrowerFirstName = borrowerFirstName;
    }

    public String getBorrowerLastName() {
        return borrowerLastName;
    }

    public void setBorrowerLastName(String borrowerLastName) {
        this.borrowerLastName = borrowerLastName;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Integer getExt() {
        return ext;
    }

    public void setExt(Integer ext) {
        this.ext = ext;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

}
