package com.company.parser;

import com.company.identifier.Category;
import com.company.identifier.IdentifierParam;
import com.company.symbol_table.SymbolTable;
import com.company.tokens.JackTokenizer;
import com.company.tokens.Keyword;
import com.company.tokens.TokenType;
import com.company.writer.ArithmeticCommand;
import com.company.writer.PushPopSegment;
import com.company.writer.VMWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JackCompilationEngine {
    private JackTokenizer jackTokenizer;
    private File outputFile;
    private BufferedWriter bufferedWriter;
    private SymbolTable symbolTable;

    private String vmFileName;
    private VMWriter vmWriter;

    private static final List<Character> OPS = Arrays.asList('+', '-', '*', '/', '&', '|', '<', '>', '=');
    private static final List<Character> UNARY_OPS = Arrays.asList('-', '~');
    private static Map<Character, ArithmeticCommand> OP_ARITH;

    private String className;
    private boolean updateXML = false;

    static {
        OP_ARITH = new HashMap<>();
        OP_ARITH.put('+', ArithmeticCommand.ADD);
        OP_ARITH.put('-', ArithmeticCommand.SUB);
        OP_ARITH.put('&', ArithmeticCommand.AND);
        OP_ARITH.put('|', ArithmeticCommand.OR);
        OP_ARITH.put('<', ArithmeticCommand.LT);
        OP_ARITH.put('>', ArithmeticCommand.GT);
        OP_ARITH.put('=', ArithmeticCommand.EQ);
    }

    public JackCompilationEngine(JackTokenizer jackTokenizer, File output, String vmFileName) {
        this.jackTokenizer = jackTokenizer;
        this.outputFile = output;
        this.symbolTable = new SymbolTable();

        this.vmFileName = vmFileName;
    }

    public void handle() throws IOException {
        try {
            this.bufferedWriter = createOutputFile();
            this.vmWriter = new VMWriter(vmFileName);
            compileClass();
        } finally {
            bufferedWriter.close();
            vmWriter.close();
        }
    }

    private BufferedWriter createOutputFile() throws IOException {
        return new BufferedWriter(new FileWriter(outputFile));
    }

    private void compileClass() throws IOException {
//        'class' classname { classVarDec* subroutineDec* }
        advanceTokenIfPossible();
        eatKeyword(Keyword.CLASS);

//        this does not get added to symboltable
        this.className = jackTokenizer.identifier();
        eatIdentifier(jackTokenizer.identifier(), Category.CLASS, true, true);
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

        final Category category = isKeyword(Keyword.STATIC) ? Category.STATIC : Category.FIELD;
        eatStaticOrField();

        final String type = getTokenType();
        eatType();

        final String identifier = jackTokenizer.identifier();
        defineSymbolTableandEatIdentifier(type, category, identifier);

//        String symbol = jackTokenizer.symbol();
        while (jackTokenizer.symbol().equals(',')) {
            eatSymbol(',');

            defineSymbolTableandEatIdentifier(type, category, jackTokenizer.identifier());
        }

        eatSymbol(';');
        XMLWriter.endXMLCategory(ProgramStructure.CLASS_VAR_DEC.getName(), bufferedWriter);

    }

    private void compileSubroutine() throws IOException {
        symbolTable.startSubroutine();

        final Keyword subroutineType = jackTokenizer.keyWord();
        eatSubroutineDeclaration();
        eatReturnType();
//        subroutine name does not get defined
        final String subroutineName = jackTokenizer.identifier();
        eatIdentifier(jackTokenizer.identifier(), Category.SUBROUTINE, true, true);
        eatSymbol('(');
        compileParameterList();
        eatSymbol(')');

        eatSymbol('{');
        while (isVarDec()) {
            compileVarDec();
        }
        final int numLocal = symbolTable.varCount(Category.VAR);
        final String fullSubName = className + "." + subroutineName;
        vmWriter.writeFunction(fullSubName, numLocal);

        if (isStatement()) {
            compileStatements();
        }
        eatSymbol('}');
    }

    private void compileParameterList() throws IOException {
        XMLWriter.startXMLCategory(ProgramStructure.PARAMETER_LIST.getName(), bufferedWriter);
        if (existsParameterList()) {
            String type = getTokenType();
            eatType();

            final Category category = Category.ARG;
            defineSymbolTableandEatIdentifier(type, category, jackTokenizer.identifier());
            while (jackTokenizer.symbol().equals(',')) {
                eatSymbol(',');

                type = getTokenType();
                eatType();

                defineSymbolTableandEatIdentifier(type, category, jackTokenizer.identifier());
            }
        }
        XMLWriter.endXMLCategory(ProgramStructure.PARAMETER_LIST.getName(), bufferedWriter);
    }

    private void compileVarDec() throws IOException {
        XMLWriter.startXMLCategory(ProgramStructure.VAR_DEC.getName(), bufferedWriter);

        final Category category = Category.VAR;
        eatKeyword(Keyword.VAR);

        String type = getTokenType();
        eatType();

        defineSymbolTableandEatIdentifier(type, category, jackTokenizer.identifier());
        while (jackTokenizer.symbol().equals(',')) {
            eatSymbol(',');

            defineSymbolTableandEatIdentifier(type, category, jackTokenizer.identifier());
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
        eatKeyword(Keyword.DO);
//        checks for subroutine:
        eatSubroutineCall(jackTokenizer.identifier(), true);
        eatSymbol(';');

    }

    private void compileLet() throws IOException {
        XMLWriter.startXMLCategory(Statement.LET.getName(), bufferedWriter);
        eatKeyword(Keyword.LET);

        String identifier = jackTokenizer.identifier();
//        let statement uses already defined variable
        eatIdentifier(identifier, symbolTable.kindOf(identifier), true, false);

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
        eatKeyword(Keyword.RETURN);
        if (!jackTokenizer.tokenValue().equals(";")) {
            compileExpression();
        } else {
            vmWriter.writePush(PushPopSegment.CONST,0);
        }
        eatSymbol(';');
        vmWriter.writeReturn();
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
//        temporarily only compileIdentifier
        compileTerm();
        while (isOperation()) {
            char operation = jackTokenizer.symbol();
            eatSymbol(operation);
            compileTerm();

            ArithmeticCommand arithmeticCommand = OP_ARITH.get(operation);
            if (arithmeticCommand != null) {
                vmWriter.writeArithmetic(arithmeticCommand);
            } else if (operation == '*') {
                vmWriter.writeCall("Math.multiply", 2);
            } else if (operation == '/') {
                vmWriter.writeCall("Math.divide", 2);
            }
        }
    }

    private void compileTerm() throws IOException {
//        assume subroutine for now
        if (jackTokenizer.tokenType().equals(TokenType.INTEGER_CONSTANT)) {
            vmWriter.writePush(PushPopSegment.CONST, jackTokenizer.intVal());
            advanceTokenIfPossible();
        } else if (jackTokenizer.tokenType().equals(TokenType.STRING_CONSTANT)) {
            char[] stringConst = jackTokenizer.stringVal().toCharArray();
            if (stringConst.length > 0) {
                vmWriter.writePush(PushPopSegment.CONST, stringConst[0]);
            }
            for (int i = 1; i < stringConst.length; i++) {
                vmWriter.writePush(PushPopSegment.CONST, stringConst[i]);
                vmWriter.writeCall("String.appendChar", 2);
            }
            advanceTokenIfPossible();
//            TODO: implement vmwriter for below statements
        } else if (isKeywordConstant()) {
            advanceTokenIfPossible();
        } else if (jackTokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
            checkVarNameInTerm();
        } else if (jackTokenizer.tokenType().equals(TokenType.SYMBOL) && jackTokenizer.symbol().equals('(')) {
            eatSymbol('(');
            compileExpression();
            eatSymbol(')');
        } else if (jackTokenizer.tokenType().equals(TokenType.SYMBOL) && UNARY_OPS.contains(jackTokenizer.symbol())) {
            char unaryOp = jackTokenizer.symbol();
            eatSymbol(unaryOp);
            compileTerm();
            ArithmeticCommand unaryOpCommand = unaryOp == '-' ? ArithmeticCommand.NEG : ArithmeticCommand.NOT;
            vmWriter.writeArithmetic(unaryOpCommand);
        }
    }

    private int compileExpressionList() throws IOException {
        int nArgs = 0;
        if (!jackTokenizer.tokenValue().equals(")")) {
            compileExpression();
            nArgs++;
            while (jackTokenizer.tokenValue().equals(",")) {
                eatSymbol(',');
                compileExpression();
                nArgs++;
            }
        }
        return nArgs;
    }

    private void checkVarNameInTerm() throws IOException {
//        symbol table should not be updated because this variable should already exist in the symbol table
        final String currentTokenValue = jackTokenizer.identifier();

        advanceTokenIfPossible();
        boolean isSymbol = jackTokenizer.tokenType().equals(TokenType.SYMBOL);
        if (isSymbol && (jackTokenizer.symbol().equals('(') || jackTokenizer.symbol().equals('.'))) {
            eatSubroutineCall(currentTokenValue, false);
        } else if (isSymbol && jackTokenizer.symbol().equals('[')) {
            eatArray(currentTokenValue);
        } else {
            eatIdentifier(currentTokenValue, symbolTable.kindOf(currentTokenValue), false, true);
        }
    }

    private void defineSymbolTableandEatIdentifier(String type, Category category, String identifier) throws IOException {
        symbolTable.define(identifier, type, category);
        eatIdentifier(identifier, category, true, true);
    }

    private void eatArray(String tokenValue) throws IOException {
        eatIdentifier(tokenValue, symbolTable.kindOf(tokenValue), false, false);
        eatSymbol('[');
        compileExpression();
        eatSymbol(']');

    }

    private void eatSubroutineCall(String tokenValue, boolean advanceToken) throws IOException {
        if (advanceToken) {
            advanceTokenIfPossible();
        }
        StringBuilder subroutineName = new StringBuilder();
        if (jackTokenizer.symbol().equals('.')) {
            subroutineName.append(tokenValue);
            eatIdentifier(tokenValue, Category.CLASS, false, false);

            subroutineName.append('.');
            eatSymbol('.');

            subroutineName.append(jackTokenizer.identifier());
            eatIdentifier(jackTokenizer.identifier(), Category.SUBROUTINE, true, false);
        } else {
            subroutineName.append(tokenValue);
            eatIdentifier(tokenValue, Category.SUBROUTINE, false, false);
        }

        final String fullName = subroutineName.toString();

        eatSymbol('(');
        final int nArgs = compileExpressionList();
        eatSymbol(')');

        vmWriter.writeCall(fullName, nArgs);
    }

    private void eatIdentifier(String identifier, Category category, boolean advanceToken, boolean definition) throws IOException {
//        if (jackTokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
//        String identifier = jackTokenizer.identifier();
        XMLWriter.startXMLCategory(TokenType.IDENTIFIER.getName(), bufferedWriter);
        XMLWriter.writeXMLKeyword(IdentifierParam.NAME.getName(), identifier, bufferedWriter);
        XMLWriter.writeXMLKeyword(IdentifierParam.CATEGORY.getName(), category.getName(), bufferedWriter);
        int index;
        try {
            index = symbolTable.indexOf(identifier);
        } catch (NullPointerException e) {
            index = 0;
        }
        XMLWriter.writeXMLKeyword(IdentifierParam.RUNNING_INDEX.getName(), String.valueOf(index), bufferedWriter);
        XMLWriter.writeXMLKeyword(IdentifierParam.DEFINED.getName(), String.valueOf(definition), bufferedWriter);

        if (advanceToken) {
            advanceTokenIfPossible();
        }
        XMLWriter.endXMLCategory(TokenType.IDENTIFIER.getName(), bufferedWriter);
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

    private void eatType() throws IOException {
        if (jackTokenizer.tokenType().equals(TokenType.IDENTIFIER) || isTypeKeyword()) {
            String keyword = getTokenType();
            if (jackTokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
                eatIdentifier(jackTokenizer.identifier(), Category.CLASS, true, false);
            } else {
//            TODO: need to save identifier info of a class? if the identifier is a class name...
                updateXMLandAdvanceToken(jackTokenizer.tokenType(), keyword, true);
            }
        } else {
            throw new CompilationException("need to provide a type, provided " +
                    jackTokenizer.tokenType() + " " + jackTokenizer.tokenValue());
        }
    }

    private void eatStaticOrField() throws IOException {
        if (jackTokenizer.tokenType().equals(TokenType.KEYWORD) && (isKeyword(Keyword.STATIC) ||
                isKeyword(Keyword.FIELD))) {
            updateXMLandAdvanceToken(jackTokenizer.tokenType(), jackTokenizer.keyWord().toString(), true);
        } else {
            throw new CompilationException("need to provide static or field, provided " +
                    jackTokenizer.tokenValue() + " " + jackTokenizer.tokenType());
        }
    }

    private void eatSubroutineDeclaration() throws IOException {
        if (isKeyword(Keyword.CONSTRUCTOR) | isKeyword(Keyword.FUNCTION)
                | isKeyword(Keyword.METHOD)) {
            updateXMLandAdvanceToken(jackTokenizer.tokenType(), jackTokenizer.keyWord().toString(), true);
        } else {
            throw new CompilationException("was expecting a subroutine declaration");
        }
    }

    private void eatReturnType() throws IOException {
        if (isKeyword(Keyword.VOID)) {
            updateXMLandAdvanceToken(jackTokenizer.tokenType(), jackTokenizer.keyWord().toString(), true);
        } else {
            eatType();
        }
    }

    private void updateXMLandAdvanceToken(TokenType tokenType, String s, boolean advanceToken) throws IOException {
        if (updateXML) {
            XMLWriter.writeXMLKeyword(tokenType.getName(), s, bufferedWriter);
        }
        if (advanceToken) {
            advanceTokenIfPossible();
        }
    }

    private void advanceTokenIfPossible() {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            System.out.println("advancing token: " + jackTokenizer.tokenValue());
        }
    }

    private String getTokenType() {
        return jackTokenizer.tokenType().equals(TokenType.IDENTIFIER) ?
                jackTokenizer.identifier() : jackTokenizer.keyWord().toString();
    }

    private boolean isClassVarDec() {
        boolean a = jackTokenizer.tokenType().equals(TokenType.KEYWORD);
        boolean b = isKeyword(Keyword.STATIC) ||
                isKeyword(Keyword.FIELD);
        return a & b;
    }

    private boolean isKeyword(Keyword constructor) {
        try {
            return jackTokenizer.keyWord().equals(constructor);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTypeKeyword() {
        return (jackTokenizer.tokenType().equals(TokenType.KEYWORD)) &&
                (isKeyword(Keyword.INT) ||
                        isKeyword(Keyword.BOOLEAN) ||
                        isKeyword(Keyword.CHAR));
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

    private boolean isOperation() {
        return OPS.contains(jackTokenizer.symbol());
    }

    private boolean isSubroutineDec() {
        return isKeyword(Keyword.CONSTRUCTOR) ||
                isKeyword(Keyword.FUNCTION) ||
                isKeyword(Keyword.METHOD);
    }

    private boolean isKeywordConstant() {
        return jackTokenizer.tokenType().equals(TokenType.KEYWORD) && (isKeyword(Keyword.TRUE) ||
                isKeyword(Keyword.FALSE) || isKeyword(Keyword.NULL) || isKeyword(Keyword.THIS));
    }

}
