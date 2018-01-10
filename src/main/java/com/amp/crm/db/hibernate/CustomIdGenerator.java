/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.db.hibernate;

import com.amp.crm.db.entity.superentity.SuperEntityCustom;
import java.io.Serializable;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 *
 * 
 */
public class CustomIdGenerator extends SequenceStyleGenerator {

    @Override
    public Serializable generate(SessionImplementor session, Object obj) {
        //Serializable id = session.getEntityPersister(null, obj).getClassMetadata().getIdentifier(obj, session);       
        Serializable id = (long) ((SuperEntityCustom) obj).getPk();
        if (id == null || (long) id <= 0l) {
            id = super.generate(session, obj);
        }
        return id;
    }
   
}
