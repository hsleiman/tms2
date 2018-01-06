package com.amp.crm.pojo;

import com.amp.crm.embeddable.DialerQueueDetails;

public class DialerQueueGroup {

    private DialerQueueDetails dialerQueueDetails;
    private QueueRunningStatus queueRunningStatus;

    public DialerQueueDetails getDialerQueueDetails() {
        return dialerQueueDetails;
    }

    public void setDialerQueueDetails(DialerQueueDetails dialerQueueDetails) {
        this.dialerQueueDetails = dialerQueueDetails;
    }

    public QueueRunningStatus getQueueRunningStatus() {
        return queueRunningStatus;
    }

    public void setQueueRunningStatus(QueueRunningStatus queueRunningStatus) {
        this.queueRunningStatus = queueRunningStatus;
    }
}
