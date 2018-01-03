/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.objectbrains.sti.embeddable;

import com.objectbrains.sti.db.entity.agent.Agent;
import com.objectbrains.sti.db.entity.agent.DialerGroup;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class AgentDialerGroupInformation {
    
    private Agent agent;
    private List<DialerGroup> dialerGroups = new ArrayList<>(0);

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public List<DialerGroup> getDialerGroups() {
        return dialerGroups;
    }

    public void setDialerGroups(List<DialerGroup> dialerGroups) {
        this.dialerGroups = dialerGroups;
    }

    
    
    
    
    
}
