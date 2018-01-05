import { Injectable } from '@angular/core';
import { OutboundQueueI } from '../outbound-queue/outbound-queue.service';
import { HttpClient } from '../../lib/http-client';
import { Config } from '../../app.config';


@Injectable()
export class OutboundQueuesService {
  private url = Config.Base_URL;
  private tmsUrl = `${Config.Base_URL}/${Config.TMS_URL}`;
  constructor(private httpClient: HttpClient) { }

  private queues = new Array<OutboundQueueI>();

  getOutboundQueues() {
    return this.queues;
  }

  getQueueById(id) {
    return this.queues.find((queue) => {
      return (queue.pk === id);
    });
  }

  createQueue(body) {
    return this.httpClient.post(`${this.tmsUrl}/createDialerQueue`, body, null);
  }

  getAllDialerQueues() {
    return this.httpClient.get(`${this.tmsUrl}/getAllDialerQueues/OUTBOUND`, null);
  }

  getDialerQueues(dialerQueue) {
    return this.httpClient.post(`${this.tmsUrl}/getAllDialerQueues/OUTBOUND`,dialerQueue,  null);
  }
}

export class OutboundQueueResult {
  dialerQueueDetails: OutboundQueueI;
  queueRunningStatus: DialerStatusI;
}

export class DialerStatusI {
  dialerState: boolean;
  queue: number;
  running: boolean;
}
