import { CustomerPhoneVM } from "./CustomerPhoneVM";
import { CustomerEmailVM } from "./CustomerEmailVM";

export class CustomerContactInfoVM {
    customerPk: number
    lastContactTimestamp: Date
    phoneNumberUsedInLastContact: number
    callbackDateTime: Date
    dialerLeftMessageTime: Date
    contactType: number
    contactTime: Date
    phones: Array<CustomerPhoneVM>
    emailAddresses: Array<CustomerEmailVM>
}