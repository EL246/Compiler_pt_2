package com.company;

import java.io.File;

class JackAnalyzer {
    private final File jackFile;
    private JackTokenizer jackTokenizer;
    private JackCompilationEngine compilationEngine;

    JackAnalyzer(String filepath) throws AnalyzerException {
        this.jackFile = new File(filepath);
        this.jackTokenizer = new JackTokenizer(jackFile);
        this.compilationEngine = new JackCompilationEngine();
    }

    void run() {
        while (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
        }
    }
}
