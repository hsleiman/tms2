/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.pojo.report;

import java.util.Date;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public class DialerRunStats {

    private long queue_pk;
    private String queue_name;
    private long run_id;
    private String state;
    private Date start_time;
    private Date end_time;
    private int total_loan_count;
    private int in_progress_loan_count;
    private int remaining_loan_count;
    private int calls_made;
    private int contact_count;
    private int ptp_count;
    private int in_progress_call_count;
    private int scheduled_call_count;

    public long getQueue_pk() {
        return queue_pk;
    }

    public void setQueue_pk(long queue_pk) {
        this.queue_pk = queue_pk;
    }

    public String getQueue_name() {
        return queue_name;
    }

    public void setQueue_name(String queue_name) {
        this.queue_name = queue_name;
    }

    public long getRun_id() {
        return run_id;
    }

    public void setRun_id(long run_id) {
        this.run_id = run_id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public int getTotal_loan_count() {
        return total_loan_count;
    }

    public void setTotal_loan_count(int total_loan_count) {
        this.total_loan_count = total_loan_count;
    }

    public int getIn_progress_loan_count() {
        return in_progress_loan_count;
    }

    public void setIn_progress_loan_count(int in_progress_loan_count) {
        this.in_progress_loan_count = in_progress_loan_count;
    }

    public int getRemaining_loan_count() {
        return remaining_loan_count;
    }

    public void setRemaining_loan_count(int remaining_loan_count) {
        this.remaining_loan_count = remaining_loan_count;
    }

    public int getCalls_made() {
        return calls_made;
    }

    public void setCalls_made(int calls_made) {
        this.calls_made = calls_made;
    }

    public int getContact_count() {
        return contact_count;
    }

    public void setContact_count(int contact_count) {
        this.contact_count = contact_count;
    }

    public int getPtp_count() {
        return ptp_count;
    }

    public void setPtp_count(int ptp_count) {
        this.ptp_count = ptp_count;
    }

    public int getIn_progress_call_count() {
        return in_progress_call_count;
    }

    public void setIn_progress_call_count(int in_progress_call_count) {
        this.in_progress_call_count = in_progress_call_count;
    }

    public int getScheduled_call_count() {
        return scheduled_call_count;
    }

    public void setScheduled_call_count(int scheduled_call_count) {
        this.scheduled_call_count = scheduled_call_count;
    }

}
