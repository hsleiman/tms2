export default class InboundQueue {
    dialerQueueDetails: {
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
    };
    queueRunningStatus: {
      queueRunningStatusId: number;
      running: boolean;
      dialerState: number;
    };
  }