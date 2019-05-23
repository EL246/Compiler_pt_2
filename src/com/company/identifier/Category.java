package com.company.identifier;

public enum Category {
    VAR("var"),
    ARG("arg"),
    STATIC("static"),
    FIELD("field"),
    CLASS("class"),
    SUBROUTINE("subroutine");

    private String name;

    Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
