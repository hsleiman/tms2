/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.hibernate;

import com.objectbrains.tms.hazelcast.entity.Agent;
import org.hibernate.envers.RevisionListener;

/**
 *
 * @author hsleiman
 */
public class AgentAuditListener implements RevisionListener {

    public void newRevision(Object revisionEntity) {

        Agent exampleRevEntity = (Agent) revisionEntity;

       // exampleRevEntity.setUsername(identity.getUsername());
    }
}
