package com.company;

import java.io.File;

class JackAnalyzer {
    private File jackFile;
    private JackTokenizer jackTokenizer;
    private JackCompilationEngine compilationEngine;

    JackAnalyzer(String filepath) {
        this.jackFile = new File(filepath);
        this.jackTokenizer = new JackTokenizer();
        this.compilationEngine = new JackCompilationEngine();
    }

    void run() {

    }
}
