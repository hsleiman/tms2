/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.disposition.action;

import com.amp.crm.constants.CallDispositionActionType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity
@DiscriminatorValue(value = CallDispositionActionType.TRY_NEXT_PHONE_NUMBER_ACTION)
public class TryNextPhoneNumberAction extends CallDispositionAction {

    public TryNextPhoneNumberAction() {
    }
    
}
