/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.dialer;

import com.amp.crm.db.entity.superentity.SuperEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.joda.time.LocalDateTime;


@Entity
@Table(schema = "crm", name = "bi_message")
public class BIMessage extends SuperEntity {

    private Integer extension;
    @Column(name = "call_uuid")
    private String callUUID;

    private String imgUrl;
    @Column(length = 4000)
    private String url;
    private Integer delay;
    private LocalDateTime timestamp;
    private Integer ptp;
    private String event;
    private Boolean flagged;
    private Long callLoanPk;
    private String biAccountPk;
    private Long urlAccountPk;
    private String elementType;
    private String nodeName;
    private String nodeValue;

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
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

    public Long getCallLoanPk() {
        return callLoanPk;
    }

    public void setCallLoanPk(Long callLoanPk) {
        this.callLoanPk = callLoanPk;
    }

    public String getBiAccountPk() {
        return biAccountPk;
    }

    public void setBiAccountPk(String biAccountPk) {
        this.biAccountPk = biAccountPk;
    }

    public Long getUrlLoanPk() {
        return urlAccountPk;
    }

    public void setUrlLoanPk(Long urlLoanPk) {
        this.urlAccountPk = urlLoanPk;
    }

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

    public Integer getExtension() {
        return extension;
    }

    public void setExtension(Integer extension) {
        this.extension = extension;
    }

    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }

}
