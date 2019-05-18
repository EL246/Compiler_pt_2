package com.company;

import com.company.tokens.TokenizerException;

class Main {

    public static void main(String[] args) throws TokenizerException, AnalyzerException {
        String filepath;
        try {
            filepath = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new AnalyzerException("Please specify a filename");
        }
        JackAnalyzer jackAnalyzer = new JackAnalyzer(filepath);
        jackAnalyzer.run();
    }
}
