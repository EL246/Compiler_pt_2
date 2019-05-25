package com.company.writer;

public enum PushPopSegment {
    CONST("constant"),
    ARG("argument"),
    LOCAL("local"),
    STATIC("static"),
    THIS("this"),
    THAT("that"),
    POINTER("pointer"),
    TEMP("temp");

    private String name;

    PushPopSegment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
