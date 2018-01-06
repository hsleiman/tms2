/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

/**
 * @author David
 */
public class CallHistoryCriteria {

    private long fromDate;
    private long toDate;
    private String callerPhoneNumber;
    private String calleePhoneNumber;
    private Integer callType;//in,Out,null
    private Long accountPk;
    private Boolean dialerCall;
    private int userDisposition;
    private int pageNumber;
    private int pageSize;


    public long getFromDate() {
        return fromDate;
    }

    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }

    public long getToDate() {
        return toDate;
    }

    public void setToDate(long toDate) {
        this.toDate = toDate;
    }

    public String getCallerPhoneNumber() {
        return callerPhoneNumber;
    }

    public void setCallerPhoneNumber(String callerPhoneNumber) {
        this.callerPhoneNumber = callerPhoneNumber;
    }

    public String getCalleePhoneNumber() {
        return calleePhoneNumber;
    }

    public void setCalleePhoneNumber(String calleePhoneNumber) {
        this.calleePhoneNumber = calleePhoneNumber;
    }

    public Integer getCallType() {
        return callType;
    }

    public void setCallType(Integer callType) {
        this.callType = callType;
    }

    public Long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(Long accountPk) {
        this.accountPk = accountPk;
    }

    public Boolean getDialerCall() {
        return dialerCall;
    }

    public void setDialerCall(Boolean dialerCall) {
        this.dialerCall = dialerCall;
    }

    public int getUserDisposition() {
        return userDisposition;
    }

    public void setUserDisposition(int userDisposition) {
        this.userDisposition = userDisposition;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
