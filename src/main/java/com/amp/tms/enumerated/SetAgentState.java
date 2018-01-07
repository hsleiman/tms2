/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.enumerated;

/**
 *
 * @author Hoang, J, Bishistha
 */
public enum SetAgentState {

    OFFLINE(AgentState.OFFLINE),
    SESSION_TIMEOUT(AgentState.OFFLINE),
    LOGOFF(AgentState.OFFLINE),
    FORCE_OFFLINE(AgentState.FORCE),
    FORCE(AgentState.FORCE),
    PREVIEW(AgentState.PREVIEW),
    IDLE(AgentState.IDLE),
    RINGING(AgentState.ONCALL),
    ONCALL(AgentState.ONCALL),
    ON_CALL(AgentState.ONCALL),
    HOLD(AgentState.HOLD),
    WRAP(AgentState.WRAP),
    MEETING(AgentState.MEETING),
    BREAK(AgentState.BREAK),
    LUNCH(AgentState.LUNCH);
    private AgentState agentState;

    private SetAgentState(AgentState agentState) {
        this.agentState = agentState;
    }

    public AgentState getAgentState() {
        return agentState;
    }

}
