/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.enumerated;

import org.joda.time.LocalTime;

/**
 *
 * @author hsleiman
 */
public class WorkHours {
    
    private LocalTime start;
    private LocalTime end;
    
    private boolean open = true;
    
    public WorkHours(LocalTime start, LocalTime end){
        this.start = start;
        this.end = end;
        this.open = isWorkingHour(start, end);
    }
    

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
    
    private boolean isWorkingHour(LocalTime start, LocalTime end) {
        boolean working = LocalTime.now().isAfter(start) && LocalTime.now().isBefore(end);
                          
        return working;
    }
    
    public String dump(){
        StringBuffer str = new StringBuffer();
        str.append("Start: ").append(start).append(" End: ").append(end).append(" Open: ").append(open);
        return str.toString();
    }
    
    
    
}
