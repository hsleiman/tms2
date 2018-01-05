/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.entity.freeswitch;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.objectbrains.tms.enumerated.FreeswitchContext;
import com.objectbrains.tms.enumerated.FreeswitchNodeStatus;
import com.objectbrains.tms.utility.GsonUtility;
import java.io.IOException;
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
public class FreeswitchNode implements DataSerializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Expose
    private Long pk;

    @Expose
    private String hostname;
    @Expose
    private String FreeSWITCH_IPv4;
    @Expose
    private Long priority;

    @Expose
    @Enumerated(value = EnumType.STRING)
    private FreeswitchContext context;

    @Expose
    private boolean all_contexts = false;

    @Expose
    private int active_users;

    @Expose
    private int active_calls;
    @Expose
    private int max_active_calls;

    @Expose
    private LocalDateTime max_active_calls_timestamp;

    @Expose
    private int min_active_calls;

    @Expose
    private int max_calls_allowed;

    @Expose
    private int max_calls_threshold;

    @Expose
    @Enumerated(value = EnumType.STRING)
    private FreeswitchNodeStatus status;

    @Expose
    @Column(updatable = false, nullable = false)
    private LocalDateTime createTimestamp;

    @Expose
    private LocalDateTime updateTimestamp;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(pk);
        out.writeUTF(hostname);
        out.writeUTF(FreeSWITCH_IPv4);
        out.writeObject(priority);
        out.writeObject(context);
        out.writeBoolean(all_contexts);
        out.writeInt(active_users);
        out.writeInt(active_calls);
        out.writeObject(max_active_calls_timestamp);
        out.writeInt(min_active_calls);
        out.writeInt(max_calls_allowed);
        out.writeInt(max_calls_threshold);
        out.writeObject(status);
        out.writeObject(createTimestamp);
        out.writeObject(updateTimestamp);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        pk = in.readObject();
        hostname = in.readUTF();
        FreeSWITCH_IPv4 = in.readUTF();
        priority = in.readObject();
        context = in.readObject();
        all_contexts = in.readBoolean();
        active_users = in.readInt();
        active_calls = in.readInt();
        max_active_calls_timestamp = in.readObject();
        min_active_calls = in.readInt();
        max_calls_allowed = in.readInt();
        max_calls_threshold = in.readInt();
        status = in.readObject();
        createTimestamp = in.readObject();
        updateTimestamp = in.readObject();
    }

    @PrePersist
    public void onCreate() {
        createTimestamp = LocalDateTime.now();
    }

    public LocalDateTime getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(LocalDateTime createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String Hostname) {
        this.hostname = Hostname;
    }

    public String getFreeSWITCH_IPv4() {
        return FreeSWITCH_IPv4;
    }

    public void setFreeSWITCH_IPv4(String FreeSWITCH_IPv4) {
        this.FreeSWITCH_IPv4 = FreeSWITCH_IPv4;
    }

    public FreeswitchContext getContext() {
        return context;
    }

    public void setContext(FreeswitchContext context) {
        this.context = context;
    }

    public int getActive_calls() {
        return active_calls;
    }

    public void setActive_calls(int active_calls) {
        this.active_calls = active_calls;
    }

    public int getMax_active_calls() {
        return max_active_calls;
    }

    public void setMax_active_calls(int max_active_calls) {
        this.max_active_calls = max_active_calls;
    }

    public LocalDateTime getMax_active_calls_timestamp() {
        return max_active_calls_timestamp;
    }

    public void setMax_active_calls_timestamp(LocalDateTime max_active_calls_timestamp) {
        this.max_active_calls_timestamp = max_active_calls_timestamp;
    }

    public int getMin_active_calls() {
        return min_active_calls;
    }

    public void setMin_active_calls(int min_active_calls) {
        this.min_active_calls = min_active_calls;
    }

    public int getMax_calls_allowed() {
        return max_calls_allowed;
    }

    public void setMax_calls_allowed(int max_calls_allowed) {
        this.max_calls_allowed = max_calls_allowed;
    }

    public int getMax_calls_threshold() {
        return max_calls_threshold;
    }

    public void setMax_calls_threshold(int max_calls_threshold) {
        this.max_calls_threshold = max_calls_threshold;
    }

    public FreeswitchNodeStatus getStatus() {
        return status;
    }

    public void setStatus(FreeswitchNodeStatus status) {
        this.status = status;
    }

    public LocalDateTime getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(LocalDateTime updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public Long getPriority() {
        return priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }

    public void increaseCallCount() {
        this.active_calls++;
        this.updateTimestamp = LocalDateTime.now();
        if (this.active_calls >= this.max_active_calls) {
            this.max_active_calls = this.active_calls;
            this.max_active_calls_timestamp = LocalDateTime.now();
        }
    }

    public void increaseUserCount() {
        this.active_users++;
    }

    public void decreaseUserCount() {
        this.active_users--;
    }

    public void decreaseCallCount() {
        this.active_calls--;
        this.updateTimestamp = LocalDateTime.now();
    }

    public boolean isAll_contexts() {
        return all_contexts;
    }

    public void setAll_contexts(boolean all_contexts) {
        this.all_contexts = all_contexts;
    }

    public int getActive_users() {
        return active_users;
    }

    public void setActive_users(int active_users) {
        this.active_users = active_users;
    }

    public String toJson() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }

}
