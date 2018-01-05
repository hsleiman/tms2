export enum PhoneSpecialInstruction {
    NONE = 0,
    FOUND_VIA_SKIP_TRACE = 1,
    MANUAL_CALLS_ONLY = 2,
    PRIVACY_MANAGER = 3,
    RINGBACK_TONE = 4
}

export function getPhoneSpecialInstructionLabel(instruction: PhoneSpecialInstruction): string {
    switch (instruction) {
        case PhoneSpecialInstruction.NONE:
            return "None";
        case PhoneSpecialInstruction.FOUND_VIA_SKIP_TRACE:
            return "Found via Skip Trace";
        case PhoneSpecialInstruction.MANUAL_CALLS_ONLY:
            return "Manual Calls Only";
        case PhoneSpecialInstruction.PRIVACY_MANAGER:
            return "Privacy Manager";
        case PhoneSpecialInstruction.RINGBACK_TONE:
            return "Ringback Tone";
        default:
            return "Unknown";
    }
}

export const PhoneSpecialInstructionKeyValueArray = [
    {id:PhoneSpecialInstruction.NONE, label: getPhoneSpecialInstructionLabel(PhoneSpecialInstruction.NONE)},
    {id:PhoneSpecialInstruction.FOUND_VIA_SKIP_TRACE, label: getPhoneSpecialInstructionLabel(PhoneSpecialInstruction.FOUND_VIA_SKIP_TRACE)},
    {id:PhoneSpecialInstruction.MANUAL_CALLS_ONLY, label: getPhoneSpecialInstructionLabel(PhoneSpecialInstruction.MANUAL_CALLS_ONLY)},
    {id:PhoneSpecialInstruction.PRIVACY_MANAGER, label: getPhoneSpecialInstructionLabel(PhoneSpecialInstruction.PRIVACY_MANAGER)},
    {id:PhoneSpecialInstruction.RINGBACK_TONE, label: getPhoneSpecialInstructionLabel(PhoneSpecialInstruction.RINGBACK_TONE)}
];