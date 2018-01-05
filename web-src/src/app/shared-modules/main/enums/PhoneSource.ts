export enum PhoneSource {
    NONE = 0,
    CUSTOMER_VERBAL = 1,
    EMAIL = 2,
    LETTER = 3,
    SKIP_TRACING = 4
}

export function getPhoneSourceLabel(source: PhoneSource): string {
    switch (source) {
        case PhoneSource.NONE:
            return "None";
        case PhoneSource.CUSTOMER_VERBAL:
            return "Customer Verbal";
        case PhoneSource.EMAIL:
            return "Email";
        case PhoneSource.LETTER:
            return "Letter";
        case PhoneSource.SKIP_TRACING:
            return "Skip Tracing";
        default:
            return "Unknown Source";
    }
}

export const PhoneSourceKeyValueArray = [
    {id:PhoneSource.NONE, label: getPhoneSourceLabel(PhoneSource.NONE)},
    {id:PhoneSource.CUSTOMER_VERBAL, label: getPhoneSourceLabel(PhoneSource.CUSTOMER_VERBAL)},
    {id:PhoneSource.EMAIL, label: getPhoneSourceLabel(PhoneSource.EMAIL)},
    {id:PhoneSource.LETTER, label: getPhoneSourceLabel(PhoneSource.LETTER)},
    {id:PhoneSource.SKIP_TRACING, label: getPhoneSourceLabel(PhoneSource.SKIP_TRACING)}
];