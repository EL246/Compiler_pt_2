package com.company;

import com.company.tokens.JackTokenizer;
import com.company.tokens.TokenizerException;

import java.io.File;

class JackAnalyzer {
    private final File jackFile;
    private JackTokenizer jackTokenizer;
    private JackCompilationEngine compilationEngine;

    JackAnalyzer(String filepath) throws TokenizerException {
        this.jackFile = new File(filepath);
        this.jackTokenizer = new JackTokenizer(jackFile);

        String newFileName = jackFile.getName().replace(".jack",".xml");
        System.out.println("output file: " + newFileName);

        this.compilationEngine = new JackCompilationEngine(jackTokenizer, new File(newFileName));
    }

    void run() {
        compilationEngine.handle();
        compilationEngine.output();
    }
}
