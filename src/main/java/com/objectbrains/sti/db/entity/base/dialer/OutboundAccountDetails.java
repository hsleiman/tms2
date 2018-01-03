/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.objectbrains.sti.db.entity.base.dialer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.objectbrains.sti.db.entity.superentity.SuperEntity;
import com.objectbrains.sti.embeddable.OutboundBorrowerData;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.ForeignKey;
import org.joda.time.LocalTime;


@Entity
@Table(schema = "sti")
public class OutboundAccountDetails extends SuperEntity{
    private long accountPk;
    private LocalTime bestTimeToCall;
    
    @XmlTransient
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_dialer_record_pk", referencedColumnName = "pk")
    @ForeignKey(name = "fk_accountDetails_DialerRecord")
    private OutboundDialerRecord outboundDialerRecord;

    
    @ElementCollection(targetClass = OutboundBorrowerData.class, fetch = FetchType.EAGER)
    @CollectionTable(schema = "sti", name = "outbound_bwr_data", joinColumns = @JoinColumn(name = "outbound_account_details_pk"))
    @OrderColumn(name = "order_index")
    private List<OutboundBorrowerData> bwrData = new ArrayList<>(); 

    public long getAccountPk() {
        return accountPk;
    }

    public void setAccountPk(long accountPk) {
        this.accountPk = accountPk;
    }

    public LocalTime getBestTimeToCall() {
        return bestTimeToCall;
    }

    public void setBestTimeToCall(LocalTime bestTimeToCall) {
        this.bestTimeToCall = bestTimeToCall;
    }

    public OutboundDialerRecord getOutboundDialerRecord() {
        return outboundDialerRecord;
    }

    public void setOutboundDialerRecord(OutboundDialerRecord outboundDialerRecord) {
        this.outboundDialerRecord = outboundDialerRecord;
    }

    public List<OutboundBorrowerData> getBwrData() {
        return bwrData;
    }

    public void setBwrData(List<OutboundBorrowerData> bwrData) {
        this.bwrData = bwrData;
    }
    
    public void associateToOutboundRecord(OutboundDialerRecord record){
        setOutboundDialerRecord(record);
        record.getAccountDetails().add(this);
    }
    
}
