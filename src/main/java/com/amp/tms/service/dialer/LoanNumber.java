/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 *
 * @author Hoang, J, Bishistha
 */
@MappedSuperclass
public class LoanNumber implements DataSerializable {

    @Column(name = "loan_pk", insertable = false, updatable = false)
    private Long loanPk;
    private Integer numberIndex;

    public LoanNumber() {
    }

    public LoanNumber(Long loanPk, Integer numberIndex) {
        this.loanPk = loanPk;
        this.numberIndex = numberIndex;
    }

    public LoanNumber(LoanNumber copy) {
        copyFrom(copy);
    }

    public final void copyFrom(LoanNumber copy) {
        this.loanPk = copy.loanPk;
        this.numberIndex = copy.numberIndex;
    }

    public Long getLoanPk() {
        return loanPk;
    }

    public void setLoanPk(Long loanPk) {
        this.loanPk = loanPk;
    }

    public Integer getNumberIndex() {
        return numberIndex;
    }

    public void setNumberIndex(Integer numberIndex) {
        this.numberIndex = numberIndex;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(loanPk);
        out.writeObject(numberIndex);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        loanPk = in.readObject();
        numberIndex = in.readObject();
    }

}
