/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message;

import com.google.gson.annotations.Expose;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;

/**
 *
 * 
 */
public class FreeswitchCheck implements DataSerializable {

    @Expose
    private Integer ext;
    @Expose 
    private Boolean isRegisterd;
    @Expose 
    private Boolean extCheck;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(ext);
        out.writeObject(isRegisterd);
        out.writeObject(extCheck);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        ext = in.readObject();
        isRegisterd = in.readObject();
        extCheck = in.readObject();
    }

    public Integer getExt() {
        return ext;
    }

    public void setExt(Integer ext) {
        this.ext = ext;
    }

    public Boolean getIsRegisterd() {
        return isRegisterd;
    }

    public void setIsRegisterd(Boolean isRegisterd) {
        this.isRegisterd = isRegisterd;
    }

    public Boolean getExtCheck() {
        return extCheck;
    }

    public void setExtCheck(Boolean extCheck) {
        this.extCheck = extCheck;
    }



}
