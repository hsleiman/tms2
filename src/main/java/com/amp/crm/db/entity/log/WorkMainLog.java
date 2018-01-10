/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.log;

import com.amp.crm.db.entity.superentity.SuperEntitySequence;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 *
 * 
 */
@Entity
@Table(schema = "crm", name = "work_main_log")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
//@DiscriminatorColumn(name = "logRange", discriminatorType = DiscriminatorType.INTEGER)
//@DiscriminatorValue("100")
public class WorkMainLog extends SuperEntitySequence {

    @Column(length = 25000)
    private String description;
    private String agentUsername;

    private long accountPk;

    private boolean priority;
    private LocalDate expireDate;

    private int logType;

    private boolean clear;
    private String clearedBy;
    private LocalDateTime clearedOn;

    private boolean reviewed;
    private String reviewedBy;
    private LocalDateTime reviewedOn;

    private boolean deleted;
    private Boolean isHidden;
    @Column(name = "call_uuid")
    private String callUUID;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(long accountPk) {
        this.accountPk = accountPk;
    }

    public String getAgentUsername() {
        return agentUsername;
    }

    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

    public int getLogType() {
        return logType;
    }

    public void setLogType(int logType) {
        this.logType = logType;
    }

    public boolean isClear() {
        return clear;
    }

    public void setClear(boolean clear) {
        if (clear == true) {
            setClearedBy(getAgentUsername());
            setClearedOn(new LocalDateTime());
        }
        this.clear = clear;
    }

    public String getClearedBy() {
        return clearedBy;
    }

    public void setClearedBy(String clearedBy) {
        this.clearedBy = clearedBy;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        if (reviewed == true) {
            setReviewedBy(getAgentUsername());
            setReviewedOn(new LocalDateTime());
        }
        this.reviewed = reviewed;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getClearedOn() {
        return clearedOn;
    }

    public void setClearedOn(LocalDateTime clearedOn) {
        this.clearedOn = clearedOn;
    }

    public LocalDateTime getReviewedOn() {
        return reviewedOn;
    }

    public void setReviewedOn(LocalDateTime reviewedOn) {
        this.reviewedOn = reviewedOn;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean isHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }
    
    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.getCreatedTime());
        hash = 67 * hash + Objects.hashCode(this.description);
        hash = 67 * hash + (int) (this.accountPk ^ (this.accountPk >>> 32));
        hash = 67 * hash + (this.priority ? 1 : 0);
        hash = 67 * hash + Objects.hashCode(this.expireDate);
        hash = 67 * hash + this.logType;
        hash = 67 * hash + (this.clear ? 1 : 0);
        hash = 67 * hash + (this.reviewed ? 1 : 0);
        hash = 67 * hash + (this.deleted ? 1 : 0);
        hash = 67 * hash + Objects.hashCode(this.isHidden);
        return hash;
    }
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WorkMainLog other = (WorkMainLog) obj;
        if (!Objects.equals(this.getCreatedTime(), other.getCreatedTime())) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (this.accountPk != other.accountPk) {
            return false;
        }
        if (this.priority != other.priority) {
            return false;
        }
        if (!Objects.equals(this.expireDate, other.expireDate)) {
            return false;
        }
        if (this.logType != other.logType) {
            return false;
        }
        if (this.clear != other.clear) {
            return false;
        }
        if (this.reviewed != other.reviewed) {
            return false;
        }
        if (this.deleted != other.deleted) {
            return false;
        }
        if (!Objects.equals(this.isHidden, other.isHidden)) {
            return false;
        }
        return true;
    }

    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }

}

