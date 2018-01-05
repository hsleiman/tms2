import { PhoneType } from "../enums/PhoneType";
import { PhoneSpecialInstruction } from "../enums/PhoneSpecialInstruction";
import { PhoneSource } from "../enums/PhoneSource";

export class CustomerPhoneVM {
    phonePk: number
    customerPk: number
    accountPk: number
    areaCode: number
    phoneNumber: number
    phoneNumberType: PhoneType
    specialInstr: PhoneSpecialInstruction
    phoneSource: PhoneSource
    phoneLabel: string
    notes: string
    doNotCall: boolean
}