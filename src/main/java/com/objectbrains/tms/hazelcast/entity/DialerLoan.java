/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.hazelcast.entity;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import java.io.IOException;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import org.joda.time.LocalDateTime;

/**
 *
 * @author connorpetty
 */
@MappedSuperclass
public class DialerLoan implements DataSerializable {

    @Transient
    private Long statsPk;

    @Transient
    private Long loanPk;

//    @Column(insertable = false, updatable = false)
//    private Long queuePk;
    @Enumerated(EnumType.STRING)
    private State state;

    @Enumerated(EnumType.STRING)
    private CompleteReason completeReason;

    private LocalDateTime completeTime;
    
    public DialerLoan() {
    }

    public DialerLoan(DialerLoan copy) {
        copyFrom(copy);
    }

    public final void copyFrom(DialerLoan copy) {
        this.statsPk = copy.getStatsPk();
        this.loanPk = copy.getLoanPk();
        this.state = copy.state;
        this.completeReason = copy.completeReason;
        this.completeTime = copy.completeTime;
    }

    public Long getStatsPk() {
        return statsPk;
    }

    public void setStatsPk(Long statsPk) {
        this.statsPk = statsPk;
    }

    public Long getLoanPk() {
        return loanPk;
    }

    public void setLoanPk(Long loanPk) {
        this.loanPk = loanPk;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public CompleteReason getCompleteReason() {
        return completeReason;
    }

    public void setCompleteReason(CompleteReason completeReason) {
        this.completeReason = completeReason;
    }

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(statsPk);
        out.writeObject(loanPk);
        State.write(out, state);
        CompleteReason.write(out, completeReason);
        out.writeObject(completeTime);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        statsPk = in.readObject();
        loanPk = in.readObject();
        state = State.read(in);
        completeReason = CompleteReason.read(in);
        completeTime = in.readObject();
    }

    public enum State {

        NOT_READY,
        NEVER_READY,
        READY,
        IN_PROGRESS,
        COMPLETE;

        public static void write(ObjectDataOutput out, State state) throws IOException {
            if (state == null) {
                out.writeInt(-1);
            } else {
                out.writeInt(state.ordinal());
            }
        }

        public static State read(ObjectDataInput in) throws IOException {
            int ordinal = in.readInt();
            if (ordinal == -1) {
                return null;
            }
            return State.values()[ordinal];
        }
    }

    public enum CompleteReason {

        NOT_IN_QUEUE,
        DISPOSITIONED,
        NO_MORE_NUMBERS,
        DIALER_STOPPED;

        public static void write(ObjectDataOutput out, CompleteReason reason) throws IOException {
            if (reason == null) {
                out.writeInt(-1);
            } else {
                out.writeInt(reason.ordinal());
            }
        }

        public static CompleteReason read(ObjectDataInput in) throws IOException {
            int ordinal = in.readInt();
            if (ordinal == -1) {
                return null;
            }
            return CompleteReason.values()[ordinal];
        }
    }
}
