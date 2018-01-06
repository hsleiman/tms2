/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.pojo;

import javax.persistence.MappedSuperclass;
import org.joda.time.LocalTime;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
@MappedSuperclass
public class DialerSchedule {

    private int dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
//    private String timeZone;

//    public DateTimeZone getTimeZone() {
//        return DateTimeZone.forID(timeZone);
//    }
//
//    public void setTimeZone(DateTimeZone timezone) {
//        timeZone = timezone.getID();
//    }
    public final void copyFrom(DialerSchedule copy) {
        this.dayOfWeek = copy.dayOfWeek;
        this.startTime = copy.startTime;
        this.endTime = copy.endTime;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
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

}
