/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.objectbrains.ams.iws.User;
import com.objectbrains.svc.iws.AgentWeightPriority;
import com.objectbrains.tms.hazelcast.entity.Agent;
import com.objectbrains.tms.hazelcast.entity.AgentWeightedPriority;
import com.objectbrains.tms.hazelcast.entity.WeightedPriority;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author connorpetty
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

    public static Set<Integer> getExtensions(Collection<Agent> agents) {
        Set<Integer> extensions = new HashSet<>();
        for (Agent agent : agents) {
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
