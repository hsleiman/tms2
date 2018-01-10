/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message.outbound;

import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.tms.pojo.CallHistory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

/**
 *
 * 
 */
public class CheckExt implements DataSerializable {

    @Expose
    private int time;
    
    @Expose
    private boolean good = false;
    
    @Expose
    private String other;

    private CheckExt() {
    }

    public CheckExt(LocalTime time) {
        this.time = time.getMillisOfDay();
    }
    
    public LocalTime getLocalTime(){
        return new LocalTime(time);
    }

    public boolean isGood() {
        return good;
    }

    public void setGood(boolean good) {
        this.good = good;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
    
    

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(time);
        out.writeBoolean(good);
        out.writeUTF(other);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        time = in.readInt();
        good = in.readBoolean();
        other = in.readUTF();
    }

}
