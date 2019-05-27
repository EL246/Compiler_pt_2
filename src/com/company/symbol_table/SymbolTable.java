package com.company.symbol_table;

import com.company.identifier.Category;
import com.company.identifier.Identifier;

import java.util.HashMap;

public class SymbolTable {
    private HashMap<String, Identifier> classTable;
    private HashMap<String, Identifier> subroutineTable;
    private HashMap<Category, Integer> indices;
    private boolean classScope;

    public SymbolTable() {
        this.classTable = new HashMap<>();
        this.subroutineTable = new HashMap<>();
        this.classScope = true;
        this.indices = new HashMap<>();
        indices.put(Category.STATIC, 0);
        indices.put(Category.FIELD, 0);
        indices.put(Category.ARG, 0);
        indices.put(Category.VAR, 0);
    }

    public void startSubroutine() {
        subroutineTable = new HashMap<>();
        this.classScope = false;
        indices.put(Category.ARG, 0);
        indices.put(Category.VAR, 0);
    }

    public void define(String name, String type, Category kind) {
        int index = indices.get(kind);
        indices.put(kind, indices.get(kind) + 1);

        Identifier identifier = new Identifier(type, kind, index);
        HashMap<String,Identifier> map = classScope ? classTable : subroutineTable;
        map.put(name, identifier);
    }

    public int varCount(Category kind) {
        return indices.get(kind);
    }

    public Category kindOf(String name) {
        Identifier identifier = getIdentifier(name);
        return identifier.getKind();
    }

    public String typeOf(String name) {
        Identifier identifier = getIdentifier(name);
        return identifier.getType();

    }

    public Integer indexOf(String name) {
        Identifier identifier = getIdentifier(name);
        return identifier.getRunningIndex();
    }

    private Identifier getIdentifier(String name) {
        Identifier identifier = subroutineTable.get(name);
        if (identifier == null) {
            identifier = classTable.get(name);
        }
        return identifier;
    }
}
