package com.company;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class JackAnalyzer {
    private final File jackFile;
    private JackTokenizer jackTokenizer;
    private JackCompilationEngine compilationEngine;

    JackAnalyzer(String filepath) throws AnalyzerException {
        this.jackFile = new File(filepath);
        this.jackTokenizer = new JackTokenizer(jackFile);
        this.compilationEngine = new JackCompilationEngine();
    }

    void run() throws IOException {
        String newFileName = jackFile.getName().replace(".jack","T.xml");
        System.out.println("output file: " + newFileName);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(newFileName));
        writeTokenXML(true, bufferedWriter);
        while (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            String tokenXML = getXMLString();
            bufferedWriter.write(tokenXML);
            bufferedWriter.newLine();
        }
        writeTokenXML(false, bufferedWriter);
        bufferedWriter.close();
    }

    private void writeTokenXML(boolean isBeginning, BufferedWriter b) throws IOException {
        String s = isBeginning ? "" : "/";
        b.write("<" + s + "tokens>");
        b.newLine();
    }

    private String getXMLString() {
        String tokenType = jackTokenizer.tokenType().getName();
        String tokenValue = jackTokenizer.tokenValue();
        switch (tokenValue) {
            case "<":
                tokenValue = "&lt;";
                break;
            case ">":
                tokenValue = "&gt;";
                break;
            case "\"":
                tokenValue = "&quot;";
                break;
            case "&":
                tokenValue = "&amp;";
                break;
        }
        return "\t <" + tokenType + "> " + tokenValue + " </" + tokenType + ">";
    }
}
