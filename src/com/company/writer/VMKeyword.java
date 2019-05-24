package com.company.writer;

public enum VMKeyword {
    FUNCTION("function");

    private String name;

    VMKeyword(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
