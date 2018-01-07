/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

/**
 *
 * @author Hoang, J, Bishistha
 */
public class DialerException extends Exception {

    private final long queuePk;
    private Long dialerPk = null;

    public DialerException(long queuePk) {
        this(queuePk, (Long) null);
    }

    public DialerException(long queuePk, long dialerPk) {
        this(queuePk, (Long) dialerPk);
    }

    private DialerException(long queuePk, Long dialerPk) {
        super();
        this.queuePk = queuePk;
        this.dialerPk = dialerPk;
    }

    public DialerException(Dialer dialer, String message) {
        this(dialer.getQueuePk(), dialer.getDialerPk(), message);
    }

    public DialerException(long queuePk, String message) {
        this(queuePk, (Long) null, message);
    }

    public DialerException(long queuePk, long dialerPk, String message) {
        this(queuePk, (Long) dialerPk, message);
    }

    private DialerException(long queuePk, Long dialerPk, String message) {
        super(message);
        this.queuePk = queuePk;
        this.dialerPk = dialerPk;
    }

    public DialerException(Dialer dialer, Throwable cause) {
        this(dialer.getQueuePk(), dialer.getDialerPk(), cause);
    }

    public DialerException(long queuePk, Throwable cause) {
        this(queuePk, (Long) null, cause);
    }

    public DialerException(long queuePk, long dialerPk, Throwable cause) {
        this(queuePk, (Long) dialerPk, cause);
    }

    private DialerException(long queuePk, Long dialerPk, Throwable cause) {
        super(cause);
        this.queuePk = queuePk;
        this.dialerPk = dialerPk;
    }

    public DialerException(Dialer dialer, String message, Throwable cause) {
        this(dialer.getQueuePk(), dialer.getDialerPk(), message, cause);
    }

    public DialerException(long queuePk, String message, Throwable cause) {
        this(queuePk, (Long) null, message, cause);
    }

    public DialerException(long queuePk, long dialerPk, String message, Throwable cause) {
        this(queuePk, (Long) dialerPk, message, cause);
    }

    private DialerException(long queuePk, Long dialerPk, String message, Throwable cause) {
        super(message, cause);
        this.queuePk = queuePk;
        this.dialerPk = dialerPk;
    }

    public long getQueuePk() {
        return queuePk;
    }

    public Long getDialerPk() {
        return dialerPk;
    }

}
