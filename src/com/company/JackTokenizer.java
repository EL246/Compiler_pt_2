package com.company;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

class JackTokenizer {
    private final File jackFile;

    private boolean isOpenLineComment;
    private boolean isOpenBlockComment;
    private boolean isOpenQuote;

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

        this.isOpenBlockComment = false;
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
            String[] lineTokens = s.split(String.format(WITH_DELIMITER,"[\";.,\\[\\](){}+\\-*/&<>=~|]"));
            System.out.println("line: " + Arrays.toString(lineTokens));
            processLine(lineTokens);
        }

    }

    private void processLine(String[] lineTokens) {
        for (String token : lineTokens) {
            token.trim();
        }
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
