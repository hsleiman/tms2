import { Injectable } from '@angular/core';
import { HttpClient } from '../../lib/http-client';
import { Config } from '../../app.config';
import OutboundDialerQueueSettings from '../outbound-queues/OutboundDialerQueueSettings';

@Injectable()
export class OutboundQueueService {
  private baseUrl = Config.Base_URL;

  constructor(private httpClient: HttpClient) {
    
  }

  getOutboundDQSettingsByDQPk(queuePk: number): Promise<OutboundDialerQueueSettings> {
    return this.httpClient.get(`${this.baseUrl}/rest/tmsRestController/getOutboundDQSettingsByDQPk/${queuePk}`, null);
  }

  updateOutboundDQSettings(dqSettings: OutboundDialerQueueSettings): Promise<OutboundDialerQueueSettings> {
    return this.httpClient.post(`${this.baseUrl}/rest/tmsRestController/createOrUpdateOutboundDQSettings`, dqSettings, null);
  }

}

export class OutboundQueueI {
  pk: number;
  queueName: string;
  loanCount: number;
  active?: boolean;
  lastLoanAssignmentTimestamp?: Array<number>;
  dialerQueueType?: string;
  sqlQuery?: string;
  queryPk?: number;
  collectionQueuePk?: number;
  createdBy?: string;
  dialerQueueSourceType?: string;
  destinationNumbers?: Array<number>;
  secondaryGroupPk?: number;
  tableGroupPk?: number;
  criteriaSetPks?: Array<number>;
  groupName?: string;
  groupPk?: number;
  queryString?: string;

}

