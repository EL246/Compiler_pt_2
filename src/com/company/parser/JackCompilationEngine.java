package com.company.parser;

import com.company.tokens.JackTokenizer;
import com.company.tokens.Keyword;
import com.company.tokens.TokenType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JackCompilationEngine {
    private JackTokenizer jackTokenizer;
    private File outputFile;
    private BufferedWriter bufferedWriter;

    public JackCompilationEngine(JackTokenizer jackTokenizer, File output) {
        this.jackTokenizer = jackTokenizer;
        this.outputFile = output;

    }

    public void handle() throws IOException {
        this.bufferedWriter = createOutputFile();
        compileClass();
        bufferedWriter.close();
    }

    private BufferedWriter createOutputFile() throws IOException {
        return new BufferedWriter(new FileWriter(outputFile));
    }

    private void compileClass() throws IOException {
//        'class' classname { classVarDec* subroutineDec* }
        XMLWriter.startXMLCategory(ProgramStructure.CLASS.getName(), bufferedWriter);
        eatKeyword(TokenType.KEYWORD, Keyword.CLASS.getName());
        eatIdentifier();
        eatKeyword(TokenType.SYMBOL, "{");
        compileClassVarDec(); // multiple times
        compileSubroutine(); // multiple times
        eatKeyword(TokenType.SYMBOL, "}");
        XMLWriter.endXMLCategory("class", bufferedWriter);
    }

    private void eatIdentifier() throws IOException {
        jackTokenizer.advance();
        if (jackTokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
            String identifier = jackTokenizer.identifier();
            XMLWriter.writeXMLKeyword(TokenType.IDENTIFIER.getName(), identifier, bufferedWriter);
        }
    }

    private void eatKeyword(TokenType tokenType, String keyword) throws IOException {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
        }
        if (jackTokenizer.tokenType().equals(tokenType) && jackTokenizer.keyWord().equals(keyword)) {
            XMLWriter.writeXMLKeyword(tokenType.getName(), keyword, bufferedWriter);
        } else {
            throw new CompilationException("expected class keyword");
        }
    }

    private void compileClassVarDec() {
    }

    private void compileSubroutine() {
    }

    void compileParameterList() {
    }

    void compileVarDec() {
    }

    void compileStatements() {
    }

    void compileDo() {
    }

    void compileLet() {
    }

    void compileWhile() {
    }

    void compileReturn() {
    }

    void compileIf() {
    }

    void compileExpression() {
    }

    void compileTerm() {
    }

    void compileExpressionList() {
    }
}
