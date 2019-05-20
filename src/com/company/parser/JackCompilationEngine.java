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
        try {
            this.bufferedWriter = createOutputFile();
            compileClass();
        } finally {
            bufferedWriter.close();
        }
    }

    private BufferedWriter createOutputFile() throws IOException {
        return new BufferedWriter(new FileWriter(outputFile));
    }

    private void compileClass() throws IOException {
//        'class' classname { classVarDec* subroutineDec* }
        advanceTokenIfPossible();
        XMLWriter.startXMLCategory(ProgramStructure.CLASS.getName(), bufferedWriter);
        eatKeyword(Keyword.CLASS);
        eatIdentifier();
        eatSymbol('{');

        while (isClassVarDec()) {
            compileClassVarDec();
        }
        while (isSubroutineDec()) {
            compileSubroutine();
        }

        eatSymbol('}');
        XMLWriter.endXMLCategory("class", bufferedWriter);
    }

    private void compileClassVarDec() throws IOException {
//        static|field type varname
        XMLWriter.startXMLCategory(ProgramStructure.CLASS_VAR_DEC.getName(), bufferedWriter);

        eatStaticOrField();
        eatType();
        eatIdentifier();

//        String symbol = jackTokenizer.symbol();
        while (jackTokenizer.symbol().equals(',')) {
            eatSymbol(',');
            eatIdentifier();
        }

        eatSymbol(';');
        XMLWriter.endXMLCategory(ProgramStructure.CLASS_VAR_DEC.getName(), bufferedWriter);

    }

    private void compileSubroutine() throws IOException {
        XMLWriter.startXMLCategory(ProgramStructure.SUBROUTINE_DEC.getName(), bufferedWriter);

        eatSubroutineDeclaration();
        eatReturnType();
        eatIdentifier();
        eatSymbol('(');
        compileParameterList(); // need to implement
        eatSymbol(')');
        compileSubroutineBody(); // need to implement

        XMLWriter.endXMLCategory(ProgramStructure.SUBROUTINE_DEC.getName(), bufferedWriter);
    }

    private void compileSubroutineBody() {
    }

    private void compileParameterList() {
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

    private boolean isSubroutineDec() {
        return isKeyword(Keyword.CONSTRUCTOR) ||
                isKeyword(Keyword.FUNCTION) ||
                isKeyword(Keyword.METHOD);
    }

    private void eatIdentifier() throws IOException {
        if (jackTokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
            String identifier = jackTokenizer.identifier();
            updateXMLandAdvanceToken(TokenType.IDENTIFIER, identifier);
        }
    }

    private void eatKeyword(Keyword keyword) throws IOException {
        if (jackTokenizer.tokenType().equals(TokenType.KEYWORD) && isKeyword(keyword)) {
            updateXMLandAdvanceToken(TokenType.KEYWORD, keyword.getName());
        } else {
            throw new CompilationException("expected keyword: " + keyword);
        }
    }

    //    TODO: redundant with eatKeyword method, need to refactor
    private void eatSymbol(Character symbol) throws IOException {
        if (jackTokenizer.tokenType().equals(TokenType.SYMBOL) && jackTokenizer.symbol().equals(symbol)) {
            updateXMLandAdvanceToken(TokenType.SYMBOL, symbol.toString());
        } else {
            throw new CompilationException("expected symbol: " + symbol);
        }
    }

    private void advanceTokenIfPossible() {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            System.out.println("advancing token: " + jackTokenizer.tokenValue());
//        } else {
//            throw new CompilationException("Not enough tokens");
        }
    }

    private boolean isClassVarDec() {
        boolean a = jackTokenizer.tokenType().equals(TokenType.KEYWORD);
        boolean b = isKeyword(Keyword.STATIC) ||
                isKeyword(Keyword.FIELD);
        boolean c = a & b;
        System.out.println("is char dec? " + c);
//        System.out.println("char keyword is: " + jackTokenizer.tokenValue() + " " + jackTokenizer.tokenType());
        return b;
    }

    private void eatType() throws IOException {
        if (jackTokenizer.tokenType().equals(TokenType.IDENTIFIER) || isTypeKeyword()) {
            String keyword = jackTokenizer.tokenType().equals(TokenType.IDENTIFIER) ?
                    jackTokenizer.identifier() : jackTokenizer.keyWord();
            updateXMLandAdvanceToken(jackTokenizer.tokenType(), keyword);
        } else {
            throw new CompilationException("need to provide a type, provided " +
                    jackTokenizer.tokenType() + " " + jackTokenizer.tokenValue());
        }
    }

    private boolean isTypeKeyword() {
        return (jackTokenizer.tokenType().equals(TokenType.KEYWORD)) &&
                (isKeyword(Keyword.INT) ||
                        isKeyword(Keyword.BOOLEAN) ||
                        isKeyword(Keyword.CHAR));
    }

    private void eatStaticOrField() throws IOException {
        if (jackTokenizer.tokenType().equals(TokenType.KEYWORD) && (isKeyword(Keyword.STATIC) ||
                isKeyword(Keyword.FIELD))) {
            updateXMLandAdvanceToken(jackTokenizer.tokenType(), jackTokenizer.keyWord());
        } else {
            throw new CompilationException("need to provide static or field, provided " +
                    jackTokenizer.tokenValue() + " " + jackTokenizer.tokenType());
        }
    }


    private void eatSubroutineDeclaration() throws IOException {
        if (isKeyword(Keyword.CONSTRUCTOR) | isKeyword(Keyword.FUNCTION)
                | isKeyword(Keyword.METHOD)) {
            updateXMLandAdvanceToken(jackTokenizer.tokenType(), jackTokenizer.keyWord());
        } else {
            throw new CompilationException("was expecting a subroutine declaration");
        }
    }

    private boolean isKeyword(Keyword constructor) {
        return jackTokenizer.keyWord().equals(constructor.getName());
    }

    private void eatReturnType() throws IOException {
        if (isKeyword(Keyword.VOID)) {
            updateXMLandAdvanceToken(jackTokenizer.tokenType(), jackTokenizer.keyWord());
        } else {
            eatType();
        }
    }

    private void updateXMLandAdvanceToken(TokenType tokenType, String s) throws IOException {
        XMLWriter.writeXMLKeyword(tokenType.getName(), s, bufferedWriter);
        advanceTokenIfPossible();
    }
}
