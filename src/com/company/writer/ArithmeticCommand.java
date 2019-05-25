package com.company.writer;

public enum ArithmeticCommand {
    ADD("add",'+'),
    SUB("sub",'-'),
    NEG("neg",'-'),
    EQ("eq",'='),
    GT("gt",'>'),
    LT("lt",'<'),
    AND("and",'&'),
    OR("or",'|'),
    NOT("not",'~');

    private String name;
    private char key;

    ArithmeticCommand(String name, char key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public char getKey() {
        return key;
    }
}
