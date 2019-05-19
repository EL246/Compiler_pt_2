package com.company.parser;

public enum ProgramStructure {
    CLASS("class"),
    CLASS_VAR_DEC("classVarDec"),
    SUBROUTINE_DEC("subroutineDec"),
    PARAMETER_LIST("parameterList"),
    SUBROUTINE_BODY("subroutineBody"),
    VAR_DEC("varDec"),
    STATEMENTS("statements");

    private String name;

    ProgramStructure(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
