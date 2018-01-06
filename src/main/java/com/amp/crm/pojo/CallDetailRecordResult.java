package com.amp.crm.pojo;

import com.amp.crm.db.entity.base.dialer.CallDetailRecord;

import java.util.ArrayList;
import java.util.List;

public class CallDetailRecordResult {
    private long totalRecordCount;
    private List<CallDetailRecord> callDetailRecordList = new ArrayList<>();

    public long getTotalRecordCount() {
        return totalRecordCount;
    }

    public void setTotalRecordCount(long totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    public List<CallDetailRecord> getCallDetailRecordList() {
        return callDetailRecordList;
    }

    public void setCallDetailRecordList(List<CallDetailRecord> callDetailRecordList) {
        this.callDetailRecordList = callDetailRecordList;
    }
}
