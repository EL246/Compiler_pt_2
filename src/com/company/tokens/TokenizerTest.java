package com.company.tokens;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TokenizerTest {
    private JackTokenizer jackTokenizer;

    public TokenizerTest(JackTokenizer jackTokenizer) {
        this.jackTokenizer = jackTokenizer;
    }

    private void createTokenXML(String newFileName) throws IOException {
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
