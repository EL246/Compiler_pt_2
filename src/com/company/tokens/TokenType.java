package com.company.tokens;

public enum TokenType {
    SYMBOL("symbol"),
    KEYWORD("keyword"),
    INTEGER_CONSTANT("integerConstant"),
    STRING_CONSTANT("stringConstant"),
    IDENTIFIER("identifier");

    private String name;

    TokenType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
