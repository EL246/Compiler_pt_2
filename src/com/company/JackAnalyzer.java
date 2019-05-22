package com.company;

import com.company.parser.JackCompilationEngine;
import com.company.tokens.JackTokenizer;
import com.company.tokens.TokenizerException;
import com.company.tokens.TokenizerTest;

import java.io.File;
import java.io.IOException;

class JackAnalyzer {
    private JackCompilationEngine compilationEngine;
    private String filePath;

    JackAnalyzer(String filepath) {
        this.filePath = filepath;
    }

    void run() throws IOException, TokenizerException {
        getJackFiles(filePath);
    }

    private void getJackFiles(String filePath) throws IOException, TokenizerException {
        File file = new File(filePath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                checkForFilesAndProcess(files);
            }
        } else if (file.isFile()) {
            handleFile(file);
        }

    }

    private void checkForFilesAndProcess(File[] files) throws IOException, TokenizerException {
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".jack")) {
                handleFile(f);
                System.out.println("Found file: " + f.getName());
            } else if (f.isDirectory()) {
                getJackFiles(f.getPath());
            }
        }
    }

    private void handleFile(File jackFile) throws TokenizerException, IOException {
        JackTokenizer jackTokenizer = new JackTokenizer(jackFile);

        String newFileName = jackFile.getName().replace(".jack",".xml");
        System.out.println("output file: " + newFileName);

        this.compilationEngine = new JackCompilationEngine(jackTokenizer, new File(newFileName));
        compilationEngine.handle();

//        TokenizerTest tokenizerTest= new TokenizerTest(new JackTokenizer(jackFile));
//        tokenizerTest.createTokenXML(jackFile.getName().replace(".jack","T.xml"));
    }


}
