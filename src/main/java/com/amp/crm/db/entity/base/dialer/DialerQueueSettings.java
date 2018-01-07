/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.base.dialer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.amp.crm.common.LocalDateTimeDeserializer;
import com.amp.crm.common.LocalDateTimeSerializer;
import com.amp.crm.common.LocalTimeDeserializer;
import com.amp.crm.common.LocalTimeSerializer;
import com.amp.crm.embeddable.DialerSchedule;
import com.amp.crm.constants.PopupDisplayMode;
import com.amp.crm.embeddable.WeightedPriority;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

/**
 *
 * @author Hoang, J, Bishistha
 */
@NamedQueries({
    @NamedQuery(
            name = "DialerQueueSettings.LocateAll",
            query = "SELECT s FROM DialerQueueSettings s"
    )
})
@Entity
@Table(schema = "sti")
//@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
//@AuditTable(value = "dialer_setting_history", schema = "svc")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class DialerQueueSettings {

    @Id
    private long dialerQueuePk;

    @XmlTransient
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId("dialerQueuePk")
    @JoinColumn(name = "dialer_queue_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_dq_settings_dialer_queue")
    private DialerQueue dialerQueue;
    @JsonIgnore
    private LocalDateTime creationTime;
    private PopupDisplayMode popupDisplayMode;
    @XmlElement(required = true)
    private Boolean autoAnswerEnabled = Boolean.FALSE;
    @Embedded
    private WeightedPriority weightedPriority;
    private Integer idleMaxMinutes;
    private Integer wrapMaxMinutes;
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime startTime;
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime endTime;
    @Column(columnDefinition = "text")
    private String changeHistory = "";
//    @Audited(targetAuditMode = RelationTargetAuditMode.AUDITED)
//    @AuditJoinTable(schema = "svc", name = "dialer_schedule_history", inverseJoinColumns = @JoinColumn(name = "dialer_queue_pk"))
    @ElementCollection(targetClass = DialerSchedule.class, fetch = FetchType.EAGER)
    @CollectionTable(schema = "sti", name = "dialer_schedule", joinColumns = @JoinColumn(name = "dialer_queue_pk"))
    @OrderBy("dayOfWeek ASC")
    private Set<DialerSchedule> dialerSchedule = new LinkedHashSet<>();
    
    @PrePersist
    private void onCreate() {
        this.creationTime = LocalDateTime.now();
    }
    
    public void associateSettingsToQueue(DialerQueue dialerQueue){
        this.setDialerQueue(dialerQueue);
        setDialerQueueForSettings(dialerQueue);
    }

    public abstract void setDialerQueueForSettings(DialerQueue queue);
    
    
    public long getDialerQueuePk() {
        return dialerQueuePk;
    }

    public void setDialerQueuePk(long dialerQueuePk) {
        this.dialerQueuePk = dialerQueuePk;
    }

    public DialerQueue getDialerQueue() {
        return dialerQueue;
    }

    public void setDialerQueue(DialerQueue dialerQueue) {
        this.dialerQueue = dialerQueue;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }
   
    public PopupDisplayMode getPopupDisplayMode() {
        return popupDisplayMode;
    }

    public void setPopupDisplayMode(PopupDisplayMode popupDisplayMode) {
        this.popupDisplayMode = popupDisplayMode;
    }

    public Boolean isAutoAnswerEnabled() {
        return autoAnswerEnabled;
    }

    public void setAutoAnswerEnabled(Boolean autoAnswerEnabled) {
        this.autoAnswerEnabled = autoAnswerEnabled;
    }

    public WeightedPriority getWeightedPriority() {
        if (weightedPriority == null) weightedPriority = new WeightedPriority();
        return weightedPriority;
    }

    public void setWeightedPriority(WeightedPriority weightedPriority) {
        this.weightedPriority = weightedPriority;
    }

    public Integer getIdleMaxMinutes() {
        return idleMaxMinutes;
    }

    public void setIdleMaxMinutes(Integer idleMaxMinutes) {
        this.idleMaxMinutes = idleMaxMinutes;
    }

    public Integer getWrapMaxMinutes() {
        return wrapMaxMinutes;
    }

    public void setWrapMaxMinutes(Integer wrapMaxMinutes) {
        this.wrapMaxMinutes = wrapMaxMinutes;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Set<DialerSchedule> getDialerSchedule() {
        return dialerSchedule;
    }
    
    public void setDialerSchedule(Set<DialerSchedule> dialerSchedule) {
        this.dialerSchedule = dialerSchedule;
    }

    public String getChangeHistory() {
        return changeHistory;
    }

    public void setChangeHistory(String changeHistory) {
        this.changeHistory = changeHistory;
    }

    public String toStringForHistory() {
                return "dialerQueuePk=" + dialerQueuePk  
                        + ", popupDisplayMode=" + popupDisplayMode 
                        + ", autoAnswerEnabled=" + autoAnswerEnabled 
                        + ", priority=" + weightedPriority.getPriority() 
                        + ", weight=" + weightedPriority.getWeight() 
                        + ", idleMaxMinutes=" + idleMaxMinutes 
                        + ", wrapMaxMinutes=" + wrapMaxMinutes 
                        + ", startTime=" + startTime 
                        + ", endTime=" + endTime 
                        + ", dialerSchedule=" + dialerSchedule.toString();
    }

    
    public String difference(Object obj) {
        if (obj == null) {
            return "New settings are null";
        }
        if (getClass() != obj.getClass()) {
            return "ClassName mismatch";
        }
        StringBuilder sb = new StringBuilder();
        final DialerQueueSettings other = (DialerQueueSettings) obj;
        if (this.popupDisplayMode != other.popupDisplayMode) {
            sb.append("\nPopupDisplayMode [oldValue : ").append(this.popupDisplayMode).append("; newValue : ").append(other.popupDisplayMode).append("]");
        }
        if (!Objects.equals(this.autoAnswerEnabled, other.autoAnswerEnabled)) {
            sb.append("\nAutoAnswerEnabled [oldValue : ").append(this.autoAnswerEnabled).append("; newValue : ").append(other.autoAnswerEnabled).append("]");
        }
        if (!Objects.equals(this.weightedPriority, other.weightedPriority)) {
            sb.append("\nWeightedPriority [oldValue : ").append(this.weightedPriority).append("; newValue : ").append(other.weightedPriority).append("]");
        }
        if (!Objects.equals(this.idleMaxMinutes, other.idleMaxMinutes)) {
            sb.append("\nIdleMaxMinutes [oldValue : ").append(this.idleMaxMinutes).append("; newValue : ").append(other.idleMaxMinutes).append("]");
        }
        if (!Objects.equals(this.wrapMaxMinutes, other.wrapMaxMinutes)) {
            sb.append("\nWrapMaxMinutes [oldValue : ").append(this.wrapMaxMinutes).append("; newValue : ").append(other.wrapMaxMinutes).append("]");
        }
        Iterator<DialerSchedule> iter = this.dialerSchedule.iterator();
        for(DialerSchedule ds : other.dialerSchedule){   
            DialerSchedule oldDS = iter.hasNext() ? iter.next() : null;
            if(oldDS != null && ds.getDayOfWeek() != null && oldDS.getDayOfWeek() != null && Objects.equals(ds.getDayOfWeek(), oldDS.getDayOfWeek()) && (ds.getStartTime() != oldDS.getStartTime() || ds.getEndTime() != oldDS.getEndTime().plusHours(12))){
                sb.append("\n DialerSchedule ==> DayOfWeek : ").append(ds.getDayOfWeek()).append(" StartTime [oldValue : ").append(oldDS.getStartTime()).append("; newValue : ").append(ds.getStartTime()).append("]")
                        .append(" EndTime [oldValue : ").append(oldDS.getEndTime()).append("; newValue : ").append(ds.getEndTime()).append("]");
            }else{
                sb.append("\n DialerSchedule ==> DayOfWeek : ").append(ds.getDayOfWeek()).append(" StartTime [oldValue : null").append("; newValue : ").append(ds.getStartTime()).append("]")
                        .append(" EndTime [oldValue : null ").append("; newValue : ").append(ds.getEndTime()).append("]");
            }
            
        }
        return sb.toString();
    }
    

}

