import { Injectable } from '@angular/core';
import { Config } from '../../app.config';
import { HttpClient } from './../../lib/http-client';
import InboundDialerQueueSettings from './InboundDialerQueueSettings';
import InboundQueue from './InboundQueue';

@Injectable()
export class InboundQueuesService {

  private baseUrl = Config.Base_URL;

  constructor(private httpClient: HttpClient) {
    
  }

  getAllDialerQueues(dialerQueueType: string): Promise<Array<InboundQueue>> {
    return this.httpClient.get(`${this.baseUrl}/rest/tmsRestController/getAllDialerQueues/${dialerQueueType}`, null);
  }

  getInboundDQSettingsByDQPk(queuePk: number): Promise<InboundDialerQueueSettings> {
    return this.httpClient.get(`${this.baseUrl}/rest/tmsRestController/getInboundDQSettingsByDQPk/${queuePk}`, null);
  }

  updateInboundDQSettings(dqSettings: InboundDialerQueueSettings): Promise<InboundDialerQueueSettings> {
    return this.httpClient.post(`${this.baseUrl}/rest/tmsRestController/createOrUpdateInboundDQSettings`, dqSettings, null);
  }

  createQueue(data): Promise<InboundQueue> {
    return this.httpClient.post(`${this.baseUrl}/rest/tmsRestController/createDialerQueue`, data, null);
  }

  updateQueue(dialerQueueDetails): Promise<InboundQueue>  {
    return this.httpClient.post(`${this.baseUrl}/rest/tmsRestController/updateDialerQueue`, dialerQueueDetails, null);
  }

  DeleteQueue(queuePk: number) {
    return this.httpClient.delete(`${this.baseUrl}/rest/tmsRestController/deleteDialerQueue/${queuePk}`, null);
  }

}
