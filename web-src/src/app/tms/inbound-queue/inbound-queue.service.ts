import { Injectable } from '@angular/core';

@Injectable()
export class InboundQueueService {

  constructor() { }

}

export class InboundQueueQueueI {
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
