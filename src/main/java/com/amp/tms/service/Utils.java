/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.objectbrains.ams.iws.User;
import com.amp.crm.embeddable.AgentWeightPriority;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.hazelcast.entity.AgentWeightedPriority;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class Utils {

    private Utils() {

    }

    public static Set<Integer> getExtensions(List<User> users) {
        Set<Integer> extensions = new HashSet<>();
        for (User user : users) {
            extensions.add(user.getExtension());
        }
        return extensions;
    }

    public static Set<Integer> getExtensions(Collection<AgentTMS> agents) {
        Set<Integer> extensions = new HashSet<>();
        for (AgentTMS agent : agents) {
            extensions.add(agent.getExtension());
        }
        return extensions;
    }

    public static Map<String, AgentWeightedPriority> convertToMap(List<AgentWeightPriority> awps) {
        Map<String, AgentWeightedPriority> nameToAgentMap = new HashMap<>();
        for (AgentWeightPriority wp : awps) {
//            com.objectbrains.svc.iws.WeightedPriority wp = agentWeightPriority.getWeightedPriority();
            nameToAgentMap.put(wp.getUsername(), wp == null ? null : new AgentWeightedPriority(wp));
        }
        return nameToAgentMap;
    }

}
