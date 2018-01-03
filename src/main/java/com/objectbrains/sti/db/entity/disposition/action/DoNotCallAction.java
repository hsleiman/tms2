/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.objectbrains.sti.db.entity.disposition.action;

import com.objectbrains.sti.constants.CallDispositionActionType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlElement;


@Entity
@DiscriminatorValue(value = CallDispositionActionType.DO_NOT_CALL_ACTION)
public class DoNotCallAction extends CallDispositionAction {

    @XmlElement(required = true)
    private Long dncDurationInSeconds;

    public Long getDncDurationInSeconds() {
        return dncDurationInSeconds;
    }

    public void setDncDurationInSeconds(Long dncDurationInSeconds) {
        this.dncDurationInSeconds = dncDurationInSeconds;
    }    
    
}
