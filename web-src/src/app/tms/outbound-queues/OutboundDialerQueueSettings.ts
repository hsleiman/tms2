import WeightedPriority from '../inbound-queues/WeightedPriority';

export default class OutboundDialerQueueSettings {
    maxDelayBeforeAgentAnswer: number;
    autoAnswerEnabled: boolean;
    dialerQueuePk: number;
    callRoutingOption: string;
    changeHistory: string;
    roundRobinCutoffPercent: number;
    disableSecondaryAgentsCallRouting: boolean;
    forceVoicemail: boolean; 
    dialerSchedule: string[];
    popupDisplayMode: string;
    idleMaxMinutes: number;
    wrapMaxMinutes: number;
    startTime: string;
    endTime: string;
    weightedPriority: WeightedPriority = new WeightedPriority();
    bestTimeToCall: boolean = false;
}