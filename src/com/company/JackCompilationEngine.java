package com.company;

import com.company.tokens.JackTokenizer;

import java.io.File;

class JackCompilationEngine {
    private JackTokenizer jackTokenizer;
    private File outputFile;

    JackCompilationEngine(JackTokenizer jackTokenizer, File output) {
        this.jackTokenizer = jackTokenizer;
        this.outputFile = output;
    }

    void handle() {
    }

    void output() {
    }

    void compileClass() {

    }

    void compileClassVarDec() {}

    void compileSubroutine() {}

    void compileParameterList(){}

    void compileVarDec() {}

    void compileStatements() {}

    void compileDo() {}

    void compileLet() {}

    void compileWhile() {}

    void compileReturn() {}

    void compileIf() {}

    void compileExpression(){}

    void compileTerm () {}

    void compileExpressionList() {}
}
