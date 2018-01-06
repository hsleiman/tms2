/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import java.util.Objects;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlElement;
import org.joda.time.LocalTime;



@Embeddable
public class DialerSchedule {
    
    @XmlElement(required = true)
    //@Column(name="day_of_week", insertable = false, updatable = false)
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    
    public DialerSchedule(){
    }
    
    public DialerSchedule(Integer dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.dayOfWeek);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DialerSchedule other = (DialerSchedule) obj;
        if (this.dayOfWeek != other.dayOfWeek) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "dayOfWeek=" + dayOfWeek + ", startTime=" + startTime + ", endTime=" + endTime+", ";
    }
    
}
