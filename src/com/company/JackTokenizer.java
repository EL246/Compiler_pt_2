package com.company;

import com.company.comment.BlockComment;
import com.company.tokens.Token;
import com.company.tokens.TokenType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static javax.lang.model.SourceVersion.isIdentifier;

class JackTokenizer {
    private final File jackFile;

    private boolean isOpenLineComment;
    private final static String OPEN_LINE_COMMENT = "//";

    private String nextCharacter = "";
    private boolean skipNextToken;


    private final BlockComment blockComment;

    //    TODO: use enum/object for this?
    private boolean isOpenQuote;
    private final static String QUOTE = "\"";
    //    TODO: make currentQuote StringBuilder instead?
    private String currentQuote = "";
    private boolean quoteClosed = false;

    //    TODO: is it more efficient to not keep a list?
    private LinkedList<Token> tokens;
    private Token pointer;

    private static final List<String> SYMBOLS = Arrays.asList("{", "}", "(", ")", "[", "]", ".",
            ",", ";", "+", "-", "*", "/", "&", "|", "<", ">", "=", "~");

    private static final List<String> KEYWORDS = Arrays.asList("class", "constructor", "function", "method",
            "field", "static", "var", "int", "char", "boolean", "void", "true", "false", "null", "this", "let",
            "do", "if", "else", "while", "return");

    private static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";


    JackTokenizer(File jackFile) throws AnalyzerException {
        this.jackFile = jackFile;
        this.tokens = new LinkedList<>();

        this.blockComment = new BlockComment();

        this.isOpenLineComment = false;
        this.isOpenQuote = false;

        init();
    }

    boolean hasMoreTokens() {
        return !tokens.isEmpty();
    }

    void advance() {
        pointer = tokens.pop();
    }

    TokenType tokenType() {
        return pointer.getTokenType();
    }

//    TODO: should return enum
    String keyWord() {
        return pointer.getValue();
    }

    Character symbol() {
        return pointer.getValue().charAt(0);
    }

    String identifier() {
        return pointer.getValue();
    }

    Integer intVal() {
        return Integer.valueOf(pointer.getValue());
    }

    String stringVal() {
        return pointer.getValue();
    }

    String tokenValue() {
        return pointer.getValue();
    }

    private void init() throws AnalyzerException {


        Scanner scanner;
        try {
            scanner = new Scanner(jackFile);
        } catch (FileNotFoundException e) {
            throw new AnalyzerException("Please specify a valid jack file");
        }

        while (scanner.hasNextLine()) {
            String s = scanner.nextLine().trim();
            String[] lineTokens = s.split(String.format(WITH_DELIMITER, "[\";.,\\[\\](){}+\\-*/&<>=~|\\s+]"));
            System.out.println("line: " + Arrays.toString(lineTokens));
            processLine(lineTokens);
        }

        isOpenLineComment = false;
    }

    private void processLine(String[] lineTokens) throws AnalyzerException {
        int i = 0;
        while (i < lineTokens.length) {
            String token = lineTokens[i];
            updateNextCharacter(lineTokens, i);
            token = checkForOpenCommentsOrQuotes(token);
            createToken(token);
            System.out.println("token: " + token);
            i = handleSkipToken(lineTokens, i);
            quoteClosed = false;
        }
        isOpenLineComment = false;
    }

    private void createToken(String token) throws AnalyzerException {
        if (token.trim().isEmpty()) {
            return;
        }
        TokenType tokenType = getTokenType(token);
        tokens.add(new Token(token,tokenType));
    }

    private TokenType getTokenType(String token) throws AnalyzerException {
        if (isStringConstant(token)) {
            return TokenType.STRING_CONSTANT;
        }
        if (SYMBOLS.contains(token)) {
            return TokenType.SYMBOL;
        }
        if (KEYWORDS.contains(token)) {
            return TokenType.KEYWORD;
        }
        if (isIntegerConstant(token)) {
            return TokenType.INTEGER_CONSTANT;
        }
        if (isIdentifier(token)) {
            return TokenType.IDENTIFIER;
        }
        throw new AnalyzerException("Invalid Token Type");
    }

    private boolean isStringConstant(String token) {
        return quoteClosed;
    }

    private boolean isIntegerConstant(String token) {
        try {
            int integerToken = Integer.parseInt(token);
            if (integerToken >= 0 && integerToken <= 32767) {
                return true;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    private int handleSkipToken(String[] lineTokens, int i) {
        if (skipNextToken && i < lineTokens.length - 1) {
            i = i + 2;
            skipNextToken = false;
        } else {
            i = i + 1;
        }
        return i;
    }

    private void updateNextCharacter(String[] lineTokens, int i) {
        if (i < lineTokens.length - 1) {
            nextCharacter = lineTokens[i + 1];
        } else {
            nextCharacter = "";
        }
    }

    private String checkForOpenCommentsOrQuotes(String token) {
        String result = "";
        if (isOpenQuote) {
            return handleOpenQuote(token);
        }
        if (blockComment.isOpen()) {
            if (blockComment.checkIfCanCloseComment(nextCharacter, token)) {
                skipNextToken = true;
            }
            return result;
        }
        if (isOpenLineComment) {
            return result;
        }
        return checkIfTokenOpensCommentOrBlock(token);
    }

    //    TODO: need to refactor this
    private String checkIfTokenOpensCommentOrBlock(String token) {
        String result = "";
        if (token.trim().equals(QUOTE)) {
            isOpenQuote = true;
            currentQuote = "";
            return result;
        }

        if (blockComment.checkIfOpenComment(nextCharacter, token.trim())) {
            return result;
        }

        String lineCommentTest = token + nextCharacter;
        if (lineCommentTest.equals(OPEN_LINE_COMMENT)) {
            isOpenLineComment = true;
            return result;
        }
// removes white spaces:
        return token.trim();
    }

    private String handleOpenQuote(String token) {
        String result;
        if (token.trim().equals(QUOTE)) {
            isOpenQuote = false;
            result = currentQuote;
            currentQuote = "";
            quoteClosed = true;
            return result;
        }
        currentQuote = currentQuote + token;
        return "";
    }
}
