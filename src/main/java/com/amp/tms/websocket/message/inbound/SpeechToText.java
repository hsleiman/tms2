/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message.inbound;

import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */
public class SpeechToText implements DataSerializable{
    @Expose
    private String call_uuid;
    @Expose
    private String text;
    
//    @Expose
//    private LocalDateTime timestamp;
    
    @Expose
    private Double confidence;

    public String getCall_uuid() {
        return call_uuid;
    }

    public void setCall_uuid(String call_uuid) {
        this.call_uuid = call_uuid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

//    public LocalDateTime getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(LocalDateTime timestamp) {
//        this.timestamp = timestamp;
//    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(call_uuid);
        out.writeUTF(text);
//        out.writeObject(timestamp);
        out.writeObject(confidence);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        call_uuid = in.readUTF();
        text = in.readUTF();
//        timestamp = in.readObject();
        confidence = in.readObject();
    }
    
    
}
