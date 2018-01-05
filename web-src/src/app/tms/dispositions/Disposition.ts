export default class Disposition {
    abandon: boolean;
    action: {
        actionType: string;
    };
    callBack: boolean;
    cause: string;
    code: string;
    contact: boolean;
    createdBy: string;
    description: string;
    disposition: string;
    dispositionId: number;
    exclusion: boolean;
    followUp: boolean;
    isTmsCode: boolean;
    logType: number;
    ptpRequired: boolean;
    qcode: number;
    refusal: boolean;
    rfdRequired: boolean;
    sipcode: number;
    status: string;
    success: boolean;
}