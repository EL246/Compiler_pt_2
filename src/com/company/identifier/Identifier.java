package com.company.identifier;

public class Identifier {
    private String type;
    private Category kind;
    private int runningIndex;

    public Identifier(String type, Category kind, int runningIndex) {
        this.type = type;
        this.kind = kind;
        this.runningIndex = runningIndex;
    }

    public Category getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }

    public int getRunningIndex() {
        return runningIndex;
    }
}
