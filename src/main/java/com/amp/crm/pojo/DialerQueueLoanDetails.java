/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.pojo;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.objectbrains.enumerated.CallResponseCode;
import com.amp.crm.db.entity.base.CallResponseAction;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.joda.time.LocalTime;

/**
 *
 * 
 */
public class DialerQueueLoanDetails implements Serializable, DataSerializable{
    
    private Long loanPk;
    private List<CustomerPhoneData> customerPhoneData;
    private Map<CallResponseCode, CallResponseAction> callResponseMap;
    private LocalTime bestTimeToCall;

    public DialerQueueLoanDetails(){
        
    }
    
    public DialerQueueLoanDetails(long loanPk){
        this.loanPk = loanPk;
    }
    
    public DialerQueueLoanDetails(DialerQueueLoanDetails dqDetails){
        this.loanPk = dqDetails.getLoanPk();
        this.bestTimeToCall = dqDetails.getBestTimeToCall();
        this.customerPhoneData = dqDetails.getCustomerPhoneData();
        //this.callResponseMap = dqDetails.getCallResponseMap();
    }
    
    public DialerQueueLoanDetails(long lPk, LocalTime bestTime, List<CustomerPhoneData> bwrData, Map<CallResponseCode, CallResponseAction> map){
        this.loanPk = lPk;
        this.bestTimeToCall = bestTime;
        this.customerPhoneData = bwrData;
        //this.callResponseMap = map;
    }
    
    public long getLoanPk() {
        return loanPk;
    }

    public void setLoanPk(long loanPk) {
        this.loanPk = loanPk;
    }

    public List<CustomerPhoneData> getCustomerPhoneData() {
        return customerPhoneData;
    }

    public void setBorrowerPhoneData(List<CustomerPhoneData> customerPhoneData) {
        this.customerPhoneData = customerPhoneData;
    }

    public Map<CallResponseCode, CallResponseAction> getCallResponseMap() {
        return callResponseMap;
    }

    public void setCallResponseMap(Map<CallResponseCode, CallResponseAction> callResponseMap) {
        this.callResponseMap = callResponseMap;
    }

    public LocalTime getBestTimeToCall() {
        return bestTimeToCall;
    }

    public void setBestTimeToCall(LocalTime bestTimeToCall) {
        this.bestTimeToCall = bestTimeToCall;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(this.loanPk);
        out.writeObject(this.bestTimeToCall);
        out.writeObject(this.customerPhoneData);
        out.writeObject(this.callResponseMap);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.loanPk = in.readObject();
        this.bestTimeToCall = in.readObject();
        this.customerPhoneData = in.readObject();
        this.callResponseMap = in.readObject();
                
    }
    
}
