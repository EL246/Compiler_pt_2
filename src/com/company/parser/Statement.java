package com.company.parser;

public enum Statement {
    LET("letStatement"),
    IF("ifStatement"),
    WHILE("whileStatement"),
    DO("doStatement"),
    RETURN("returnStatement");

    private String name;

    Statement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
