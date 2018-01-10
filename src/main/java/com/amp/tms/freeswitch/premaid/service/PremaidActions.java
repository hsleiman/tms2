/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.freeswitch.premaid.service;

import com.amp.tms.db.entity.freeswitch.TMSDialplan;
import com.amp.tms.enumerated.RecordedPhrases;
import com.amp.tms.freeswitch.FreeswitchVariables;
import com.amp.tms.freeswitch.dialplan.action.AbstractAction;
import com.amp.tms.freeswitch.dialplan.action.Answer;
import com.amp.tms.freeswitch.dialplan.action.Bridge;
import com.amp.tms.freeswitch.dialplan.action.BridgeToSofiaContact;
import com.amp.tms.freeswitch.dialplan.action.Export;
import com.amp.tms.freeswitch.dialplan.action.Hangup;
import com.amp.tms.freeswitch.dialplan.action.Playback;
import com.amp.tms.freeswitch.dialplan.action.Set;
import com.amp.tms.freeswitch.dialplan.action.Sleep;
import com.amp.tms.freeswitch.pojo.DialplanVariable;
import com.amp.tms.hazelcast.entity.AgentTMS;
import com.amp.tms.service.TMSAgentService;
import com.amp.tms.service.FreeswitchConfiguration;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service
public class PremaidActions {

    @Autowired
    private TMSAgentService agentService;
    
    @Autowired
    private FreeswitchConfiguration configuration;
    
    private static final Logger log = LoggerFactory.getLogger(PremaidActions.class);

    public ArrayList<AbstractAction> getLocalExtension(AgentTMS callerAgent, DialplanVariable variable) {
        AgentTMS calleeAgent = agentService.getAgent(variable.getCalleeIdInteger());

        ArrayList<AbstractAction> actions = new ArrayList<>();
        actions.add(new Set("dialed_extension", "$1"));
        actions.add(new Export("dialed_extension", "$1"));
//        actions.add(new BindMetaApp("1 b s execute_extension::dx XML features"));
//        actions.add(new BindMetaApp("2 b s record_session::$${recordings_dir}/${caller_id_number}.${strftime(%Y-%m-%d-%H-%M-%S)}.wav"));
//        actions.add(new BindMetaApp("3 b s execute_extension::cf XML features"));
//        actions.add(new BindMetaApp("4 b s execute_extension::att_xfer XML features"));

        actions.add(new Set(FreeswitchVariables.ringback , "${us-ring}"));
        actions.add(new Set("transfer_ringback", "$${hold_music}"));
        actions.add(new Set(FreeswitchVariables.call_timeout, "20"));
        //actions.add(new Set("sip_exclude_contact","${network_addr}"));
        actions.add(new Set("hangup_after_bridge", "true"));

//        actions.add(new Set("continue_on_fail","NORMAL_TEMPORARY_FAILURE,USER_BUSY,NO_ANSWER,TIMEOUT,NO_ROUTE_DESTINATION"));
        actions.add(new Set("continue_on_fail", "true"));

        if (callerAgent != null) {
            actions.add(Set.create(FreeswitchVariables.origination_caller_id_name, callerAgent.getUserName()));
            actions.add(Set.create(FreeswitchVariables.origination_caller_id_number, callerAgent.getExtension()));
            actions.add(Set.create(FreeswitchVariables.effective_caller_id_number, callerAgent.getExtension()));
        }
//        actions.add(new Hash("insert/${domain_name}-call_return/${dialed_extension}/${caller_id_number}"));
//        actions.add(new Hash("insert/${domain_name}-last_dial_ext/${dialed_extension}/${uuid}"));
//        actions.add(new Set("called_party_callgroup","${user_data(${dialed_extension}@${domain_name} var callgroup)}"));
//        actions.add(new Hash("insert/${domain_name}-last_dial_ext/${called_party_callgroup}/${uuid}"));
//        actions.add(new Hash("insert/${domain_name}-last_dial_ext/global/${uuid}"));
//        actions.add(new Hash("insert/${domain_name}-last_dial/${called_party_callgroup}/${uuid}"));
        
        
        //actions.add(new Bridge("${sofia_contact(agent/$1@" + calleeAgent.getFreeswitchDomain() + ")}"));
        actions.add(new BridgeToSofiaContact("$1", calleeAgent.getFreeswitchDomain()));
        //actions.add(new Bridge("user/${dialed_extension}@${domain_name}"));

        actions.add(new Answer());
        actions.add(new Sleep(1000l));
        actions.add(new Bridge("loopback/app=voicemail:agent " + calleeAgent.getFreeswitchDomain() + " $1"));
        //actions.add(new Voicemail("default "+agent.getFreeswitchDomain()+ " $1"));

        return actions;
    }

    public TMSDialplan getDNC(TMSDialplan dialplan) {

        log.info("Building DNC Dialplan...");
        dialplan.addAction(new Answer());
        dialplan.addAction(new Sleep(1000l));
        dialplan.addAction(new Playback(RecordedPhrases.DNC));
        dialplan.addAction(new Sleep(500l));
        dialplan.addAction(new Hangup("NORMAL_CLEARING"));
        log.info("DNC Dialplan built.");

        return dialplan;
    }
}
