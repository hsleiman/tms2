/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.joda.time.LocalDateTime;

/**
 *
 * @author David
 */
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
public class QueueMovementPojo {
    
    private long oldQueuePk;
    private long newQueuePk;
    private long oldPortfolio;
    private String oldPortfolioDesc;
    private long newPortfolio;
    private String newPortfolioDesc;
    
    private Integer oldServicingStatus;
    private Integer newServicingStatus;
    
    private LocalDateTime createTimestamp;
    
    private String logComment;
    
    
    public long getOldQueuePk() {
        return oldQueuePk;
    }

    public void setOldQueuePk(long oldQueuePk) {
        this.oldQueuePk = oldQueuePk;
    }

    public long getNewQueuePk() {
        return newQueuePk;
    }

    public void setNewQueuePk(long newQueuePk) {
        this.newQueuePk = newQueuePk;
    }

    public long getOldPortfolio() {
        return oldPortfolio;
    }

    public void setOldPortfolio(long oldPortfolio) {
        this.oldPortfolio = oldPortfolio;
    }

    public String getOldPortfolioDesc() {
        return oldPortfolioDesc;
    }

    public void setOldPortfolioDesc(String oldPortfolioDesc) {
        this.oldPortfolioDesc = oldPortfolioDesc;
    }

    public long getNewPortfolio() {
        return newPortfolio;
    }

    public void setNewPortfolio(long newPortfolio) {
        this.newPortfolio = newPortfolio;
    }

    public String getNewPortfolioDesc() {
        return newPortfolioDesc;
    }

    public void setNewPortfolioDesc(String newPortfolioDesc) {
        this.newPortfolioDesc = newPortfolioDesc;
    }

    public String getLogComment() {
        return logComment;
    }

    public void setLogComment(String logComment) {
        this.logComment = logComment;
    }

    public LocalDateTime getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(LocalDateTime createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Integer getOldServicingStatus() {
        return oldServicingStatus;
    }

    public void setOldServicingStatus(Integer oldServicingStatus) {
        this.oldServicingStatus = oldServicingStatus;
    }

    public Integer getNewServicingStatus() {
        return newServicingStatus;
    }

    public void setNewServicingStatus(Integer newServicingStatus) {
        this.newServicingStatus = newServicingStatus;
    }

    
    
      
}
