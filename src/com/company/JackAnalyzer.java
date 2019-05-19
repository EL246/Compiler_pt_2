package com.company;

import com.company.parser.JackCompilationEngine;
import com.company.tokens.JackTokenizer;
import com.company.tokens.TokenizerException;

import java.io.File;
import java.io.IOException;

class JackAnalyzer {
    private JackCompilationEngine compilationEngine;

    JackAnalyzer(String filepath) throws TokenizerException {
        File jackFile = new File(filepath);
        JackTokenizer jackTokenizer = new JackTokenizer(jackFile);

        String newFileName = jackFile.getName().replace(".jack",".xml");
        System.out.println("output file: " + newFileName);

        this.compilationEngine = new JackCompilationEngine(jackTokenizer, new File(newFileName));
    }

    void run() throws IOException {
        compilationEngine.handle();
    }
}
