package com.company.parser;

import com.company.tokens.JackTokenizer;
import com.company.tokens.Keyword;
import com.company.tokens.TokenType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JackCompilationEngine {
    private JackTokenizer jackTokenizer;
    private File outputFile;
    private BufferedWriter bufferedWriter;

    private static final List<Character> OPS = Arrays.asList('+', '-', '*', '/', '&', '|', '<', '>', '=');
    private static final List<Character> UNARY_OPS = Arrays.asList('-', '~');

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
        compileParameterList();
        eatSymbol(')');
        compileSubroutineBody(); // need to implement

        XMLWriter.endXMLCategory(ProgramStructure.SUBROUTINE_DEC.getName(), bufferedWriter);
    }

    private void compileSubroutineBody() throws IOException {
        XMLWriter.startXMLCategory(ProgramStructure.SUBROUTINE_BODY.getName(), bufferedWriter);
        eatSymbol('{');
        while (isVarDec()) {
            compileVarDec();
        }
        if (isStatement()) {
            compileStatements();
        }
        eatSymbol('}');
        XMLWriter.endXMLCategory(ProgramStructure.SUBROUTINE_BODY.getName(), bufferedWriter);
    }

    private void compileParameterList() throws IOException {
        XMLWriter.startXMLCategory(ProgramStructure.PARAMETER_LIST.getName(), bufferedWriter);
        if (existsParameterList()) {
            eatType();
            eatIdentifier();
            while (jackTokenizer.symbol().equals(',')) {
                eatSymbol(',');
                eatType();
                eatIdentifier();
            }
        }
        XMLWriter.endXMLCategory(ProgramStructure.PARAMETER_LIST.getName(), bufferedWriter);
    }

    private void compileVarDec() throws IOException {
        XMLWriter.startXMLCategory(ProgramStructure.VAR_DEC.getName(), bufferedWriter);
        eatKeyword(Keyword.VAR);
        eatType();
        eatIdentifier();
        while (jackTokenizer.symbol().equals(',')) {
            eatSymbol(',');
            eatIdentifier();
        }
        eatSymbol(';');
        XMLWriter.endXMLCategory(ProgramStructure.VAR_DEC.getName(), bufferedWriter);
    }

    private void compileStatements() throws IOException {
        XMLWriter.startXMLCategory(ProgramStructure.STATEMENTS.getName(), bufferedWriter);
        while (isStatement()) {
//            refactor to factory or switch statement
            if (isLetStatement()) {
                compileLet();
            } else if (isIfStatement()) {
                compileIf();
            } else if (isWhileStatement()) {
                compileWhile();
            } else if (isDoStatement()) {
                compileDo();
            } else if (isReturnStatement()) {
                compileReturn();
            }
        }
        XMLWriter.endXMLCategory(ProgramStructure.STATEMENTS.getName(), bufferedWriter);
    }

    private void compileDo() throws IOException {
        XMLWriter.startXMLCategory(Statement.DO.getName(), bufferedWriter);
        eatKeyword(Keyword.DO);
//        checks for subroutine:
        eatSubroutineCall(jackTokenizer.identifier(), true);
        eatSymbol(';');
        XMLWriter.endXMLCategory(Statement.DO.getName(), bufferedWriter);

    }

    private void compileLet() throws IOException {
        XMLWriter.startXMLCategory(Statement.LET.getName(), bufferedWriter);
        eatKeyword(Keyword.LET);
        eatIdentifier();
        if (jackTokenizer.symbol().equals('[')) {
            eatSymbol('[');
            compileExpression();
            eatSymbol(']');
        }
        eatSymbol('=');
        compileExpression();
        eatSymbol(';');
        XMLWriter.endXMLCategory(Statement.LET.getName(), bufferedWriter);
    }

    private void compileWhile() throws IOException {
        XMLWriter.startXMLCategory(Statement.WHILE.getName(), bufferedWriter);
        eatKeyword(Keyword.WHILE);
        eatSymbol('(');
        compileExpression();
        eatSymbol(')');
        eatSymbol('{');
        compileStatements();
        eatSymbol('}');
        XMLWriter.endXMLCategory(Statement.WHILE.getName(), bufferedWriter);
    }

    private void compileReturn() throws IOException {
        XMLWriter.startXMLCategory(Statement.RETURN.getName(), bufferedWriter);
        eatKeyword(Keyword.RETURN);
        if (!jackTokenizer.tokenValue().equals(";")) {
            compileExpression();
        }
        eatSymbol(';');

        XMLWriter.endXMLCategory(Statement.RETURN.getName(), bufferedWriter);

    }

    private void compileIf() throws IOException {
        XMLWriter.startXMLCategory(Statement.IF.getName(), bufferedWriter);

        eatKeyword(Keyword.IF);
        eatSymbol('(');
        compileExpression();
        eatSymbol(')');
        eatSymbol('{');
        if (isStatement()) {
            compileStatements();
        }
        eatSymbol('}');

        if (isKeyword(Keyword.ELSE)) {
            eatKeyword(Keyword.ELSE);
            eatSymbol('{');
            if (isStatement()) {
                compileStatements();
            }
            eatSymbol('}');
        }

        XMLWriter.endXMLCategory(Statement.IF.getName(), bufferedWriter);
    }

    private void compileExpression() throws IOException {
        XMLWriter.startXMLCategory(ProgramStructure.EXPRESSION.getName(), bufferedWriter);
//        temporarily only compileIdentifier
        compileTerm();
        while (isOperation()) {
            eatSymbol(jackTokenizer.symbol());
            compileTerm();
        }
        XMLWriter.endXMLCategory(ProgramStructure.EXPRESSION.getName(), bufferedWriter);
    }

    private void compileTerm() throws IOException {
//        assume subroutine for now
        XMLWriter.startXMLCategory(ProgramStructure.TERM.getName(), bufferedWriter);

        if (jackTokenizer.tokenType().equals(TokenType.INTEGER_CONSTANT) ||
                jackTokenizer.tokenType().equals(TokenType.STRING_CONSTANT) ||
                isKeywordConstant()) {
            updateXMLandAdvanceToken(jackTokenizer.tokenType(), jackTokenizer.tokenValue(), true);
        } else if (jackTokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
            checkVarNameInTerm();
        } else if (jackTokenizer.tokenType().equals(TokenType.SYMBOL) && jackTokenizer.symbol().equals('(')) {
            eatSymbol('(');
            compileExpression();
            eatSymbol(')');
        } else if (jackTokenizer.tokenType().equals(TokenType.SYMBOL) && UNARY_OPS.contains(jackTokenizer.symbol())) {
            eatSymbol(jackTokenizer.symbol());
            compileTerm();
        }

        XMLWriter.endXMLCategory(ProgramStructure.TERM.getName(), bufferedWriter);
    }

    private void compileExpressionList() throws IOException {
        XMLWriter.startXMLCategory(ProgramStructure.EXPRESSION_LIST.getName(), bufferedWriter);
        if (!jackTokenizer.tokenValue().equals(")")) {
            compileExpression();
            while (jackTokenizer.tokenValue().equals(",")) {
                eatSymbol(',');
                compileExpression();
            }
        }
        XMLWriter.endXMLCategory(ProgramStructure.EXPRESSION_LIST.getName(), bufferedWriter);
    }

    private void checkVarNameInTerm() throws IOException {
        final String currentTokenValue = jackTokenizer.identifier();

        advanceTokenIfPossible();
        boolean isSymbol = jackTokenizer.tokenType().equals(TokenType.SYMBOL);
        if (isSymbol && (jackTokenizer.symbol().equals('(') || jackTokenizer.symbol().equals('.'))) {
            eatSubroutineCall(currentTokenValue, false);
        } else if (isSymbol && jackTokenizer.symbol().equals('[')) {
            eatArray(currentTokenValue);
        } else {
            updateXMLandAdvanceToken(TokenType.IDENTIFIER, currentTokenValue, false);
        }
    }

    private void eatArray(String tokenValue) throws IOException {
        updateXMLandAdvanceToken(TokenType.IDENTIFIER, tokenValue, false);
        eatSymbol('[');
        compileExpression();
        eatSymbol(']');

    }

    private boolean isKeywordConstant() {
        return jackTokenizer.tokenType().equals(TokenType.KEYWORD) && (isKeyword(Keyword.TRUE) ||
                isKeyword(Keyword.FALSE) || isKeyword(Keyword.NULL) || isKeyword(Keyword.THIS));
    }

    private void eatSubroutineCall(String tokenValue, boolean advanceToken) throws IOException {
        updateXMLandAdvanceToken(TokenType.IDENTIFIER, tokenValue, advanceToken);
        if (jackTokenizer.symbol().equals('.')) {
            eatSymbol('.');
            eatIdentifier();
        }
        eatSymbol('(');
        compileExpressionList();
        eatSymbol(')');
    }

    private boolean isOperation() {
        return OPS.contains(jackTokenizer.symbol());
    }

    private boolean isSubroutineDec() {
        return isKeyword(Keyword.CONSTRUCTOR) ||
                isKeyword(Keyword.FUNCTION) ||
                isKeyword(Keyword.METHOD);
    }

    private void eatIdentifier() throws IOException {
        if (jackTokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
            String identifier = jackTokenizer.identifier();
            updateXMLandAdvanceToken(TokenType.IDENTIFIER, identifier, true);
        }
    }

    private void eatKeyword(Keyword keyword) throws IOException {
        if (jackTokenizer.tokenType().equals(TokenType.KEYWORD) && isKeyword(keyword)) {
            updateXMLandAdvanceToken(TokenType.KEYWORD, keyword.getName(), true);
        } else {
            throw new CompilationException("expected keyword: " + keyword);
        }
    }

    //    TODO: redundant with eatKeyword method, need to refactor
    private void eatSymbol(Character symbol) throws IOException {
        if (jackTokenizer.tokenType().equals(TokenType.SYMBOL) && jackTokenizer.symbol().equals(symbol)) {
            updateXMLandAdvanceToken(TokenType.SYMBOL, symbol.toString(), true);
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
            updateXMLandAdvanceToken(jackTokenizer.tokenType(), keyword, true);
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
            updateXMLandAdvanceToken(jackTokenizer.tokenType(), jackTokenizer.keyWord(), true);
        } else {
            throw new CompilationException("need to provide static or field, provided " +
                    jackTokenizer.tokenValue() + " " + jackTokenizer.tokenType());
        }
    }

    private void eatSubroutineDeclaration() throws IOException {
        if (isKeyword(Keyword.CONSTRUCTOR) | isKeyword(Keyword.FUNCTION)
                | isKeyword(Keyword.METHOD)) {
            updateXMLandAdvanceToken(jackTokenizer.tokenType(), jackTokenizer.keyWord(), true);
        } else {
            throw new CompilationException("was expecting a subroutine declaration");
        }
    }

    private boolean isKeyword(Keyword constructor) {
        return jackTokenizer.keyWord().equals(constructor.getName());
    }

    private void eatReturnType() throws IOException {
        if (isKeyword(Keyword.VOID)) {
            updateXMLandAdvanceToken(jackTokenizer.tokenType(), jackTokenizer.keyWord(), true);
        } else {
            eatType();
        }
    }

    private void updateXMLandAdvanceToken(TokenType tokenType, String s, boolean advanceToken) throws IOException {
        XMLWriter.writeXMLKeyword(tokenType.getName(), s, bufferedWriter);
        if (advanceToken) {
            advanceTokenIfPossible();
        }
    }

    private boolean existsParameterList() {
        return isTypeKeyword();
    }


    private boolean isStatement() {
        return isLetStatement() || isIfStatement() ||
                isWhileStatement() ||
                isDoStatement() || isReturnStatement();
    }

    private boolean isReturnStatement() {
        return isKeyword(Keyword.RETURN);
    }

    private boolean isDoStatement() {
        return isKeyword(Keyword.DO);
    }

    private boolean isWhileStatement() {
        return isKeyword(Keyword.WHILE);
    }

    private boolean isIfStatement() {
        return isKeyword(Keyword.IF);
    }

    private boolean isLetStatement() {
        return isKeyword(Keyword.LET);
    }

    private boolean isVarDec() {
        return isKeyword(Keyword.VAR);
    }
}
