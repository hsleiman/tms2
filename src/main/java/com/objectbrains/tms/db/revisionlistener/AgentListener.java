/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.revisionlistener;

/**
 *
 * @author hsleiman
 */
import com.objectbrains.tms.hazelcast.entity.Agent;
import org.hibernate.envers.RevisionListener;


public class AgentListener implements RevisionListener {

    public void newRevision(Object revisionEntity) {
        Agent revEntity = (Agent) revisionEntity;
        //revEntity.set; 
    }
    
}