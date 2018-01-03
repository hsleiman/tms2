/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.freeswitch.premaid.incoming;

import com.objectbrains.tms.freeswitch.pojo.DialplanVariable;
import com.objectbrains.tms.freeswitch.premaid.DialplanBuilder;

/**
 *
 * @author hsleiman
 */
public class IncomingRSBC extends DialplanBuilder{
    public IncomingRSBC(DialplanVariable variable){
        setVariable(variable);
    }

    @Override
    public void createDialplans() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void buildDialplans() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveDialplans() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
