/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.pojo;

import java.io.Serializable;

/**
 *
 * @author hsleiman
 */
public class UploadCallRecordingPOJO implements Serializable{
    private String callUUID;
    private String data;
    private String ip;
    private Long duration = 0l;
    
    public UploadCallRecordingPOJO( String callUUID, String ip, String data, Long duration){
        this.callUUID= callUUID;
        this.data = data;
        this.ip = ip;
        this.duration = duration;
    }

    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
    
    

}
