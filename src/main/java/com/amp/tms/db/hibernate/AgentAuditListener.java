/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.hibernate;

import com.amp.tms.hazelcast.entity.AgentTMS;
import org.hibernate.envers.RevisionListener;

/**
 *
 * 
 */
public class AgentAuditListener implements RevisionListener {

    public void newRevision(Object revisionEntity) {

        AgentTMS exampleRevEntity = (AgentTMS) revisionEntity;

       // exampleRevEntity.setUsername(identity.getUsername());
    }
}
