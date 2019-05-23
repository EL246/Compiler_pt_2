package com.company.identifier;

public enum IdentifierParam {
    NAME("name"),
    CATEGORY("category"),
    RUNNING_INDEX("runningIndex"),
    DEFINED("defined");

    private String name;

    IdentifierParam(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
