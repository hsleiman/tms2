/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.entity.base.payment;

import com.objectbrains.sti.db.entity.superentity.SuperEntitySequence;
import com.objectbrains.sti.embeddable.EftPaymentBasicData;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrePersist;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.joda.time.LocalDateTime;

/**
 *
 * @author raine.cabal
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class EFTPayment extends SuperEntitySequence {

    @Embedded
    private EftPaymentBasicData eftPaymentBasicData = new EftPaymentBasicData();

    public EftPaymentBasicData getEftPaymentBasicData() {
        return eftPaymentBasicData;
    }

    public void setEftPaymentBasicData(EftPaymentBasicData eftPaymentBasicData) {
        this.eftPaymentBasicData = eftPaymentBasicData;
    }

    @PrePersist
    private void onCreate() {
        if (this.getEftPaymentBasicData().getPaymentTime() == null) {
            this.getEftPaymentBasicData().setPaymentTime(LocalDateTime.now());
        }
    }
    
    public void updatePaymentData(EftPaymentBasicData newPayment){
        EftPaymentBasicData orig = this.getEftPaymentBasicData();
        newPayment.setPaymentTime(orig.getPaymentTime());
        this.setEftPaymentBasicData(newPayment);
    }

}
