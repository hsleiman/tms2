/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.disposition;

import com.objectbrains.enumerated.CallDispositionStatus;
import com.objectbrains.sti.db.entity.disposition.action.CallDispositionAction;
import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.apache.commons.lang3.builder.ToStringBuilder;


@NamedQueries({
   @NamedQuery(
            name = "CallDispositionCode.LocateAll",
            query = "SELECT c FROM CallDispositionCode c ORDER BY c.disposition"),
    @NamedQuery(
            name = "CallDispositionCode.LocateByDisposition",
            query = "SELECT c FROM CallDispositionCode c WHERE LOWER(TRIM(c.disposition)) = LOWER(TRIM(:disposition))"
    ),
    @NamedQuery(
            name = "CallDispositionCode.LocateByQCode",
            query = "SELECT c FROM CallDispositionCode c WHERE qCode = :qCode"
    )
})
@Entity
@Table(schema = "sti")
public class CallDispositionCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dispositionId;
    @Column(nullable = false, unique = true)
    private String disposition;
    @Column(nullable = false, unique = true)
    private String code;
    private boolean abandon = false;
    private boolean contact = false;
    private boolean followUp = false;
    private boolean callBack = false;
    private boolean success = false;
    private boolean refusal = false;
    private boolean exclusion = false;
    private String createdBy;
    private boolean rfdRequired = false;
    private boolean ptpRequired = false;
    private Integer logType;
    @Enumerated(EnumType.STRING)
    private CallDispositionStatus status = CallDispositionStatus.ACTIVE;
    private Boolean isCode = false;
    private Integer qCode;
    @Column(name="sip_code")
    private Integer SIPCode;
    private String cause;
    @Column(length = 4000)
    private String description;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "action_pk", referencedColumnName = "pk")
    private CallDispositionAction action;

    public CallDispositionCode() {
    }

    public CallDispositionCode(Long dispositionId, String disposition, String code) {
        this.dispositionId = dispositionId;
        this.disposition = disposition;
        this.code = code;
    }

    public CallDispositionAction getAction() {
        return action;
    }

    public void setAction(CallDispositionAction action) {
        this.action = action;
    }

    public Long getDispositionId() {
        return dispositionId;
    }

    public void setDispositionId(Long dispositionId) {
        this.dispositionId = dispositionId;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isAbandon() {
        return abandon;
    }

    public void setAbandon(boolean abandon) {
        this.abandon = abandon;
    }

    public boolean isContact() {
        return contact;
    }

    public void setContact(boolean contact) {
        this.contact = contact;
    }

    public boolean isFollowUp() {
        return followUp;
    }

    public void setFollowUp(boolean followUp) {
        this.followUp = followUp;
    }

    public boolean isCallBack() {
        return callBack;
    }

    public void setCallBack(boolean callBack) {
        this.callBack = callBack;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isRefusal() {
        return refusal;
    }

    public void setRefusal(boolean refusal) {
        this.refusal = refusal;
    }

    public boolean isExclusion() {
        return exclusion;
    }

    public void setExclusion(boolean exclusion) {
        this.exclusion = exclusion;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isRfdRequired() {
        return rfdRequired;
    }

    public void setRfdRequired(boolean rfdRequired) {
        this.rfdRequired = rfdRequired;
    }

    public boolean isPtpRequired() {
        return ptpRequired;
    }

    public void setPtpRequired(boolean ptpRequired) {
        this.ptpRequired = ptpRequired;
    }

    public Integer getLogType() {
        return logType;
    }

    public void setLogType(Integer logType) {
        this.logType = logType;
    }

    public CallDispositionStatus getStatus() {
        return status;
    }

    public void setStatus(CallDispositionStatus status) {
        this.status = status;
    }

    public Boolean getIsCode() {
        return isCode;
    }

    public void setIsCode(Boolean isCode) {
        this.isCode = isCode;
    }

    public Integer getqCode() {
        return qCode;
    }

    public void setqCode(Integer qCode) {
        this.qCode = qCode;
    }

    public Integer getSIPCode() {
        return SIPCode;
    }

    public void setSIPCode(Integer SIPCode) {
        this.SIPCode = SIPCode;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.dispositionId);
        hash = 23 * hash + Objects.hashCode(this.disposition);
        hash = 23 * hash + Objects.hashCode(this.code);
        hash = 23 * hash + (this.abandon ? 1 : 0);
        hash = 23 * hash + (this.contact ? 1 : 0);
        hash = 23 * hash + (this.followUp ? 1 : 0);
        hash = 23 * hash + (this.callBack ? 1 : 0);
        hash = 23 * hash + (this.success ? 1 : 0);
        hash = 23 * hash + (this.refusal ? 1 : 0);
        hash = 23 * hash + (this.exclusion ? 1 : 0);
        hash = 23 * hash + (this.rfdRequired ? 1 : 0);
        hash = 23 * hash + (this.ptpRequired ? 1 : 0);
        hash = 23 * hash + Objects.hashCode(this.logType);
        hash = 23 * hash + Objects.hashCode(this.status);
        hash = 23 * hash + Objects.hashCode(this.action);
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
        final CallDispositionCode other = (CallDispositionCode) obj;
        if (!Objects.equals(this.dispositionId, other.dispositionId)) {
            return false;
        }
        if (!Objects.equals(this.disposition, other.disposition)) {
            return false;
        }
        if (!Objects.equals(this.code, other.code)) {
            return false;
        }
        if (this.abandon != other.abandon) {
            return false;
        }
        if (this.contact != other.contact) {
            return false;
        }
        if (this.followUp != other.followUp) {
            return false;
        }
        if (this.callBack != other.callBack) {
            return false;
        }
        if (this.success != other.success) {
            return false;
        }
        if (this.refusal != other.refusal) {
            return false;
        }
        if (this.exclusion != other.exclusion) {
            return false;
        }
        if (this.rfdRequired != other.rfdRequired) {
            return false;
        }
        if (this.ptpRequired != other.ptpRequired) {
            return false;
        }
        if (!Objects.equals(this.logType, other.logType)) {
            return false;
        }
        if (this.status != other.status) {
            return false;
        }
        if (!Objects.equals(this.action, other.action)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
