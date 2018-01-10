/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.amp.tms.utility.GsonUtility;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */
@Embeddable
public class BiMessage implements Serializable {

    @Expose
    private String img;
    @Expose
    @Column(length = 2100)
    private String url;
    @Expose
    @Embedded
    private ToElement toElement;
    @Expose
    private Integer delay;
    @Expose
    private LocalDateTime timeStamp;
    @Expose
    private Integer ptp;
    @Expose
    private String event;
    @Expose
    private Boolean flagged;
    @Expose
    private String loanFromBi;

    @Expose
    private Long loanFromBiLong;

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ToElement getToElement() {
        return toElement;
    }

    public void setToElement(ToElement toElement) {
        this.toElement = toElement;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Integer getPtp() {
        return ptp;
    }

    public void setPtp(Integer ptp) {
        this.ptp = ptp;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Boolean getFlagged() {
        return flagged;
    }

    public void setFlagged(Boolean flagged) {
        this.flagged = flagged;
    }

    public String getLoanFromBi() {
        return loanFromBi;
    }

    public void setLoanFromBi(String loanFromBi) {
        this.loanFromBi = loanFromBi;
    }

    public Long getLoanFromBiLong() {
        return loanFromBiLong;
    }

    public void setLoanFromBiLong(Long loanFromBiLong) {
        this.loanFromBiLong = loanFromBiLong;
    }

    public String toJson() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }

    @Embeddable
    public static class ToElement implements Serializable {

        @Expose
        private String elementType;
        @Expose
        private String nodeName;
        @Expose
        private String nodeValue;

        public String getElementType() {
            return elementType;
        }

        public void setElementType(String elementType) {
            this.elementType = elementType;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getNodeValue() {
            return nodeValue;
        }

        public void setNodeValue(String nodeValue) {
            this.nodeValue = nodeValue;
        }

        public String toJson() {
            Gson gson = GsonUtility.getGson(true);
            return gson.toJson(this);
        }

    }
}
