/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.disposition.action;

import com.amp.crm.constants.CallDispositionActionType;
import com.amp.crm.db.entity.superentity.SuperEntity;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlSeeAlso;


@Entity
@Table(schema = "crm")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "action_type", discriminatorType = DiscriminatorType.STRING)
@XmlSeeAlso({RetryCallAction.class, DoNotCallAction.class, TryNextPhoneNumberAction.class, MarkLoanAsCompletedAction.class, DoNotCallPhoneAction.class})
public abstract class CallDispositionAction  extends SuperEntity {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, insertable = false, updatable = false)
    private CallDispositionActionType actionType;

    public CallDispositionActionType getActionType() {
        return actionType;
    }

    public void setActionType(CallDispositionActionType actionType) {
        this.actionType = actionType;
    }
 
}
