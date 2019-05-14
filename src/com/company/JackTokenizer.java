package com.company;

import com.company.comment.BlockComment;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

class JackTokenizer {
    private final File jackFile;

    private boolean isOpenLineComment;
    private final static String OPEN_LINE_COMMENT = "//";

    private String nextCharacter = "";
    private boolean skipNextToken;


    private final BlockComment blockComment;

    private boolean isOpenQuote;
    private final static String QUOTE = "\"";
    //    TODO: make currentQuote StringBuilder instead?
    private String currentQuote = "";

    //    TODO: is it more efficient to not keep a list?
    private List<JackToken> tokens;

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

    private void init() throws AnalyzerException {


        Scanner scanner;
        try {
            scanner = new Scanner(jackFile);
        } catch (FileNotFoundException e) {
            throw new AnalyzerException("Please specify a valid jack file");
        }

        while (scanner.hasNextLine()) {
            String s = scanner.nextLine().trim();
            String[] lineTokens = s.split(String.format(WITH_DELIMITER, "[\";.,\\[\\](){}+\\-*/&<>=~|]"));
            System.out.println("line: " + Arrays.toString(lineTokens));
            processLine(lineTokens);
        }

        isOpenLineComment = false;
    }

    private void processLine(String[] lineTokens) {
        int i = 0;
        while (i < lineTokens.length) {
            String token = lineTokens[i];
            updateNextCharacter(lineTokens, i);
            token = checkForOpenCommentsOrQuotes(token);
            System.out.println("token: " + token);
            if (skipNextToken && i < lineTokens.length - 1) {
                i = i + 2;
                skipNextToken = false;
            } else {
                i = i + 1;
            }
        }
        isOpenLineComment = false;
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
            return result;
        }
        currentQuote = currentQuote + token;
        return "";
    }

    boolean hasMoreTokens() {
        throw new NotImplementedException();
    }

    void advance() {
        throw new NotImplementedException();
    }

    void tokenType() {
        throw new NotImplementedException();
    }

    void keyWord() {
        throw new NotImplementedException();
    }

    Character symbol() {
        throw new NotImplementedException();
    }

    String identifier() {
        throw new NotImplementedException();
    }

    Integer intVal() {
        throw new NotImplementedException();
    }

    String stringVal() {
        throw new NotImplementedException();
    }
}
