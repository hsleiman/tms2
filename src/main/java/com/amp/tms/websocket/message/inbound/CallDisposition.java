/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message.inbound;

import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.amp.crm.db.entity.disposition.CallDispositionCode;
import java.io.IOException;

/**
 *
 * @author hsleiman
 */
public class CallDisposition implements DataSerializable {

    @Expose
    private Long dispositionPk;

    @Expose
    private String disposition;
    
    @Expose
    private String log;

    public CallDisposition() {
    }

    public CallDisposition(CallDispositionCode code) {
        this.dispositionPk = code.getDispositionId();
        this.disposition = code.getDisposition();
    }

    public Long getDispositionPk() {
        return dispositionPk;
    }

    public void setDispositionPk(Long dispositionPk) {
        this.dispositionPk = dispositionPk;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(dispositionPk);
        out.writeUTF(disposition);
        out.writeUTF(log);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        dispositionPk = in.readObject();
        disposition = in.readUTF();
        log = in.readUTF();
    }

}
