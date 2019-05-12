package com.company;

public class Main {

    public static void main(String[] args) {
        String filepath = args[0];
        JackAnalyzer jackAnalyzer = new JackAnalyzer(filepath);
        jackAnalyzer.run();
    }
}
