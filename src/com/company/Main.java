package com.company;

import java.io.IOException;

class Main {

    public static void main(String[] args) throws AnalyzerException, IOException {
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
