export enum PhoneType {
    HOME_PHONE_1 = 0,
    WORK_PHONE = 1,
    MOBILE_PHONE = 2,
    HOME_PHONE_2 = 3,
    PRIMARY_PHONE = 5,
    NON_PRIMARY_PHONE = 6,
    BUSINESS_PHONE = 10,
    GOOGLE_VOICE = 15,
    OTHER_PHONE = 20,
    REFERENCE = 99
}

export function getPhoneTypeLabel(type: PhoneType): string {
    switch (type) {
        case PhoneType.HOME_PHONE_1:
            return "Home Phone";
        case PhoneType.WORK_PHONE:
            return "Work Phone";
        case PhoneType.MOBILE_PHONE:
            return "Mobile Phone";
        case PhoneType.HOME_PHONE_2:
            return "Other Home Phone";
        case PhoneType.PRIMARY_PHONE:
            return "Primary Phone";
        case PhoneType.BUSINESS_PHONE:
            return "Business Phone";
        case PhoneType.GOOGLE_VOICE:
            return "Google Voice";
        case PhoneType.OTHER_PHONE:
            return "Other Phone";
        case PhoneType.REFERENCE:
            return "Reference";
        default:
            return "Unknown";
    }
}

export const PhoneTypeKeyValueArray = [
    {id: PhoneType.HOME_PHONE_1, label: getPhoneTypeLabel(PhoneType.HOME_PHONE_1)},
    {id: PhoneType.WORK_PHONE, label: getPhoneTypeLabel(PhoneType.WORK_PHONE)},
    {id: PhoneType.MOBILE_PHONE, label: getPhoneTypeLabel(PhoneType.MOBILE_PHONE)},
    {id: PhoneType.HOME_PHONE_2, label: getPhoneTypeLabel(PhoneType.HOME_PHONE_2)},
    {id: PhoneType.PRIMARY_PHONE, label: getPhoneTypeLabel(PhoneType.PRIMARY_PHONE)},
    {id: PhoneType.BUSINESS_PHONE, label: getPhoneTypeLabel(PhoneType.BUSINESS_PHONE)},
    {id: PhoneType.GOOGLE_VOICE, label: getPhoneTypeLabel(PhoneType.GOOGLE_VOICE)},
    {id: PhoneType.OTHER_PHONE, label: getPhoneTypeLabel(PhoneType.OTHER_PHONE)},
    {id: PhoneType.REFERENCE, label: getPhoneTypeLabel(PhoneType.REFERENCE)}
];