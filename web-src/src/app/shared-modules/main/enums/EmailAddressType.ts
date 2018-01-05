export enum EmailAddressType {
    PRIMARY_EMAIL = 1,
    NON_PRIMARY_EMAIL = 2,
    BUSINESS_EMAIL = 3
}

export function getEmailAddressTypeLabel(source: EmailAddressType): string {
    switch (source) {
        case EmailAddressType.PRIMARY_EMAIL:
            return "Primary Email";
        case EmailAddressType.NON_PRIMARY_EMAIL:
            return "Non-Primary Email";
        case EmailAddressType.BUSINESS_EMAIL:
            return "Business Email"
        default:
            return "Unknown";
    }
}

export const EmailAddressTypeKeyValueArray = [
    {id: EmailAddressType.PRIMARY_EMAIL, label: getEmailAddressTypeLabel(EmailAddressType.PRIMARY_EMAIL)},
    {id: EmailAddressType.NON_PRIMARY_EMAIL, label: getEmailAddressTypeLabel(EmailAddressType.NON_PRIMARY_EMAIL)},
    {id: EmailAddressType.BUSINESS_EMAIL, label: getEmailAddressTypeLabel(EmailAddressType.BUSINESS_EMAIL)}
];