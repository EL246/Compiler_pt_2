package com.company.parser;

import com.company.identifier.Category;
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
import java.util.*;

import static com.company.tokens.TokenType.IDENTIFIER;
import static com.company.tokens.TokenType.KEYWORD;

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
    private static Map<Category, PushPopSegment> VAR_TO_SEGMENT;

    private String className;
    private int labelId;

    static {
        OP_ARITH = new HashMap<>();
        OP_ARITH.put('+', ArithmeticCommand.ADD);
        OP_ARITH.put('-', ArithmeticCommand.SUB);
        OP_ARITH.put('&', ArithmeticCommand.AND);
        OP_ARITH.put('|', ArithmeticCommand.OR);
        OP_ARITH.put('<', ArithmeticCommand.LT);
        OP_ARITH.put('>', ArithmeticCommand.GT);
        OP_ARITH.put('=', ArithmeticCommand.EQ);

        VAR_TO_SEGMENT = new HashMap<>();
        VAR_TO_SEGMENT.put(Category.VAR, PushPopSegment.LOCAL);
        VAR_TO_SEGMENT.put(Category.ARG, PushPopSegment.ARG);
        VAR_TO_SEGMENT.put(Category.STATIC, PushPopSegment.STATIC);
        VAR_TO_SEGMENT.put(Category.FIELD, PushPopSegment.THIS);
    }

    public JackCompilationEngine(JackTokenizer jackTokenizer, File output, String vmFileName) {
        this.jackTokenizer = jackTokenizer;
        this.outputFile = output;
        this.symbolTable = new SymbolTable();

        this.vmFileName = vmFileName;
        this.labelId = 0;
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
    }

    private void compileClassVarDec() {
//        static|field type varname
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
        if (subroutineType.equals(Keyword.METHOD)) {
            symbolTable.define("this", className, Category.ARG);
        }
        compileParameterList();
        eatSymbol(')');

        eatSymbol('{');
        while (isVarDec()) {
            compileVarDec();
        }
        final int numLocal = symbolTable.varCount(Category.VAR);
        final String fullSubName = className + "." + subroutineName;
        vmWriter.writeFunction(fullSubName, numLocal);

        if (subroutineType.equals(Keyword.CONSTRUCTOR)) {
            int size = symbolTable.varCount(Category.FIELD);
            vmWriter.writePush(PushPopSegment.CONST, size);
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(PushPopSegment.POINTER, 0);
        } else if (subroutineType.equals(Keyword.METHOD)) {
            vmWriter.writePush(PushPopSegment.ARG, 0);
            vmWriter.writePop(PushPopSegment.POINTER, 0);
        }

        if (isStatement()) {
            compileStatements();
        }
        eatSymbol('}');
    }

    private void compileParameterList() {
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
    }

    private void compileVarDec() {
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
    }

    private void compileStatements() throws IOException {
        while (isStatement()) {
            switch (jackTokenizer.keyWord()) {
                case LET:
                    compileLet();
                    break;
                case IF:
                    compileIf();
                    break;
                case WHILE:
                    compileWhile();
                    break;
                case DO:
                    compileDo();
                    break;
                case RETURN:
                    compileReturn();
                    break;
            }
        }
    }

    private void compileDo() throws IOException {
        eatKeyword(Keyword.DO);
//        checks for subroutine:
        eatSubroutineCall(jackTokenizer.identifier(), true);
        eatSymbol(';');
        vmWriter.writePop(PushPopSegment.TEMP, 0);
    }

    private void compileLet() throws IOException {
        eatKeyword(Keyword.LET);

        String identifier = jackTokenizer.identifier();
//        let statement uses already defined variable
        eatIdentifier(identifier, symbolTable.kindOf(identifier), true, false);

        PushPopSegment segment = getPushPopSegment(identifier);
        int index = symbolTable.indexOf(identifier);
        boolean isArray = false;
        if (jackTokenizer.symbol().equals('[')) {
            vmWriter.writePush(segment, index);
            isArray = true;
            eatSymbol('[');
            compileExpression();
            eatSymbol(']');
            vmWriter.writeArithmetic(ArithmeticCommand.ADD);
        }
        eatSymbol('=');
        compileExpression();
        eatSymbol(';');

        if (isArray) {
            vmWriter.writePop(PushPopSegment.TEMP, 0);
            vmWriter.writePop(PushPopSegment.POINTER, 1);
            vmWriter.writePush(PushPopSegment.TEMP, 0);
            vmWriter.writePop(PushPopSegment.THAT, 0);
        } else {
            vmWriter.writePop(segment, index);
        }
    }

    private void compileWhile() throws IOException {
        String whileLabel = createNewLabel("WHILE-START");
        vmWriter.writeLabel(whileLabel);

        eatKeyword(Keyword.WHILE);
        eatSymbol('(');
        compileExpression();
        eatSymbol(')');

        vmWriter.writeArithmetic(ArithmeticCommand.NOT);
        String exitLabel = createNewLabel("WHILE-END");
        vmWriter.writeIf(exitLabel);

        eatSymbol('{');
        compileStatements();
        eatSymbol('}');

        vmWriter.writeGoto(whileLabel);
        vmWriter.writeLabel(exitLabel);
    }

    private void compileReturn() throws IOException {
        eatKeyword(Keyword.RETURN);
        if (!jackTokenizer.tokenValue().equals(";")) {
            compileExpression();
        } else {
            vmWriter.writePush(PushPopSegment.CONST, 0);
        }
        eatSymbol(';');
        vmWriter.writeReturn();
    }

    private void compileIf() throws IOException {
        eatKeyword(Keyword.IF);
        eatSymbol('(');
        compileExpression();
        eatSymbol(')');

        vmWriter.writeArithmetic(ArithmeticCommand.NOT);
        String elseLabel = createNewLabel("ELSE");
        vmWriter.writeIf(elseLabel);
        String endLabel = createNewLabel("END");

        eatSymbol('{');
        if (isStatement()) {
            compileStatements();
        }
        eatSymbol('}');

        vmWriter.writeGoto(endLabel);

        vmWriter.writeLabel(elseLabel);
        if (isKeyword(Keyword.ELSE)) {
            eatKeyword(Keyword.ELSE);
            eatSymbol('{');
            if (isStatement()) {
                compileStatements();
            }
            eatSymbol('}');
        }

        vmWriter.writeLabel(endLabel);
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
//            TODO: should String.new be called in all cases??
            int length = jackTokenizer.stringVal().length();
            vmWriter.writePush(PushPopSegment.CONST, length);
            vmWriter.writeCall("String.new", 1);
            char[] stringConst = jackTokenizer.stringVal().toCharArray();
            for (char aStringConst : stringConst) {
                vmWriter.writePush(PushPopSegment.CONST, aStringConst);
                vmWriter.writeCall("String.appendChar", 2);
            }
            advanceTokenIfPossible();
        } else if (isKeywordConstant()) {
            handleKeywordConstant();
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

    //    TODO: implement vmwriter for this method
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
//            TODO: extract this code into separate method -- repetitive
            PushPopSegment segment = getPushPopSegment(currentTokenValue);
            int index = symbolTable.indexOf(currentTokenValue);
            vmWriter.writePush(segment, index);
            eatIdentifier(currentTokenValue, symbolTable.kindOf(currentTokenValue), false, true);
        }
    }

    private void defineSymbolTableandEatIdentifier(String type, Category category, String identifier) {
        symbolTable.define(identifier, type, category);
        eatIdentifier(identifier, category, true, true);
    }

    private void eatArray(String tokenValue) throws IOException {
        int index = symbolTable.indexOf(tokenValue);
        PushPopSegment segment = getPushPopSegment(tokenValue);
        vmWriter.writePush(segment, index);
        eatIdentifier(tokenValue, symbolTable.kindOf(tokenValue), false, false);
        eatSymbol('[');
        compileExpression();
        eatSymbol(']');
        vmWriter.writeArithmetic(ArithmeticCommand.ADD);
        vmWriter.writePop(PushPopSegment.POINTER, 1);
        vmWriter.writePush(PushPopSegment.THAT, 0);

    }

    private void eatSubroutineCall(String tokenValue, boolean advanceToken) throws IOException {
        if (advanceToken) {
            advanceTokenIfPossible();
        }
        boolean isMethod = false;
        boolean inCurrentClass = false;
        Integer index = null;

        StringBuilder subroutineName = new StringBuilder();
        if (jackTokenizer.symbol().equals('.')) {

            try {
                index = symbolTable.indexOf(tokenValue);
                if (index != null) {
                    isMethod = true;
                }
            } catch (NullPointerException ignored) {
            }

            String classValue;
            if (isMethod) {
                classValue = symbolTable.typeOf(tokenValue);
            } else {
                classValue = tokenValue;
            }

            subroutineName.append(classValue);
            eatIdentifier(tokenValue, Category.CLASS, false, false);

            subroutineName.append('.');
            eatSymbol('.');

            subroutineName.append(jackTokenizer.identifier());
            eatIdentifier(jackTokenizer.identifier(), Category.SUBROUTINE, true, false);

        } else {
            inCurrentClass = true;
            isMethod = true;
//            this is definitely a method because functions and constructors must be called with the classname

            subroutineName.append(className);
            subroutineName.append('.');
            subroutineName.append(tokenValue);
            eatIdentifier(tokenValue, Category.SUBROUTINE, false, false);
        }

        final String fullName = subroutineName.toString();

        if (isMethod) {
//            make sure it's not a constructor
//            if the variable exists in the table, the variable is not a Class name, but rather an object name
//            otherwise, assuming correctly written code, it is a class, and should be treated as a constructor
//            TODO: refactor
            if (inCurrentClass) {
                vmWriter.writePush(PushPopSegment.POINTER, 0);
            } else {
                PushPopSegment segment = getPushPopSegment(tokenValue);
                vmWriter.writePush(segment, index);
            }
        }


        eatSymbol('(');
        int nArgs = compileExpressionList();
        eatSymbol(')');

        nArgs = isMethod ? nArgs + 1 : nArgs;

        vmWriter.writeCall(fullName, nArgs);
    }

    private void eatIdentifier(String identifier, Category category, boolean advanceToken, boolean definition) {
        if (advanceToken) {
            advanceTokenIfPossible();
        }
    }

    private void eatKeyword(Keyword keyword) {
        if (jackTokenizer.tokenType().equals(KEYWORD) && isKeyword(keyword)) {
            advanceTokenIfPossible();
        } else {
            throw new CompilationException("expected keyword: " + keyword);
        }
    }

    //    TODO: redundant with eatKeyword method, need to refactor
    private void eatSymbol(Character symbol) {
        if (jackTokenizer.tokenType().equals(TokenType.SYMBOL) && jackTokenizer.symbol().equals(symbol)) {
            advanceTokenIfPossible();
        } else {
            throw new CompilationException("expected symbol: " + symbol);
        }
    }

    private void eatType() {
        if (jackTokenizer.tokenType().equals(TokenType.IDENTIFIER) || isTypeKeyword()) {
            String keyword = getTokenType();
            if (jackTokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
                eatIdentifier(jackTokenizer.identifier(), Category.CLASS, true, false);
            } else {
                advanceTokenIfPossible();
            }
        } else {
            throw new CompilationException("need to provide a type, provided " +
                    jackTokenizer.tokenType() + " " + jackTokenizer.tokenValue());
        }
    }

    private void eatStaticOrField() {
        if (jackTokenizer.tokenType().equals(KEYWORD) && (isKeyword(Keyword.STATIC) ||
                isKeyword(Keyword.FIELD))) {
            advanceTokenIfPossible();
        } else {
            throw new CompilationException("need to provide static or field, provided " +
                    jackTokenizer.tokenValue() + " " + jackTokenizer.tokenType());
        }
    }

    private void eatSubroutineDeclaration() {
        if (isKeyword(Keyword.CONSTRUCTOR) | isKeyword(Keyword.FUNCTION)
                | isKeyword(Keyword.METHOD)) {
            advanceTokenIfPossible();
        } else {
            throw new CompilationException("was expecting a subroutine declaration");
        }
    }

    private void eatReturnType() {
        if (isKeyword(Keyword.VOID)) {
            advanceTokenIfPossible();
        } else {
            eatType();
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
        boolean a = jackTokenizer.tokenType().equals(KEYWORD);
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
        return (jackTokenizer.tokenType().equals(KEYWORD)) &&
                (isKeyword(Keyword.INT) ||
                        isKeyword(Keyword.BOOLEAN) ||
                        isKeyword(Keyword.CHAR));
    }

    private boolean existsParameterList() {
        return isTypeKeyword() || jackTokenizer.tokenType().equals(IDENTIFIER);
    }

    private boolean isStatement() {
        return isKeyword(Keyword.LET) || isKeyword(Keyword.IF) ||
                isKeyword(Keyword.WHILE) ||
                isKeyword(Keyword.DO) || isKeyword(Keyword.RETURN);
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
        return jackTokenizer.tokenType().equals(KEYWORD) && (isKeyword(Keyword.TRUE) ||
                isKeyword(Keyword.FALSE) || isKeyword(Keyword.NULL) || isKeyword(Keyword.THIS));
    }

    private void handleKeywordConstant() throws IOException {
        Keyword keyword = jackTokenizer.keyWord();
        switch (keyword) {
            case TRUE:
                vmWriter.writePush(PushPopSegment.CONST, 1);
                vmWriter.writeArithmetic(ArithmeticCommand.NEG);
                break;
            case FALSE:
            case NULL:
                vmWriter.writePush(PushPopSegment.CONST, 0);
                break;
            case THIS:
                vmWriter.writePush(PushPopSegment.POINTER, 0);
                break;
        }
    }

    private String createNewLabel(String label) {
        String newLabel = label + "." + labelId;
        labelId++;
        return newLabel;
    }

    private PushPopSegment getPushPopSegment(String tokenValue) {
        Category category = symbolTable.kindOf(tokenValue);
        return VAR_TO_SEGMENT.get(category);
    }
}
