/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.embeddable;

import com.amp.crm.constants.IncomingCallAgent;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author David
 */
@Embeddable
public class AgentCallOrder {
    
    @Enumerated(EnumType.STRING)
    @XmlElement(required = true)
    private IncomingCallAgent incomingCallAgent;
    @XmlElement(required = true)
    private Boolean inline;
    
    public AgentCallOrder(){
    }
    
    public AgentCallOrder(IncomingCallAgent incomingCallAgent) {
        this.incomingCallAgent = incomingCallAgent;
    }

    public IncomingCallAgent getIncomingCallAgent() {
        return incomingCallAgent;
    }

    public void setIncomingCallAgent(IncomingCallAgent agentCallOrder) {
        this.incomingCallAgent = agentCallOrder;
    }

    public Boolean isInline() {
        return inline;
    }

    public void setInline(Boolean inline) {
        this.inline = inline;
    }

    @Override
    public String toString() {
        return "{" + "incomingCallAgent=" + incomingCallAgent + ", inline=" + inline + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AgentCallOrder other = (AgentCallOrder) obj;
        if (this.incomingCallAgent != other.incomingCallAgent) {
            return false;
        }
        if (!Objects.equals(this.inline, other.inline)) {
            return false;
        }
        return true;
    }
    
    
    
}

