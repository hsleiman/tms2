package com.objectbrains.sti.constants;

public enum QualityAssuranceQuestionType {
    MULTIPLE_CHOICE_SINGLE_ANSWER("MULTIPLE_CHOICE_SINGLE_ANSWER"),
    MULTIPLE_CHOICE_MULTI_SELECT("MULTIPLE_CHOICE_MULTI_SELECT"),
    TEXT("TEXT");

    String string;

    QualityAssuranceQuestionType(String name) {
        string = name;
    }

    @Override
    public String toString() {
        return string;
    }
}
