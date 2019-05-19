package com.company.parser;


import java.io.BufferedWriter;
import java.io.IOException;

class XMLWriter {

    static void writeXMLKeyword(String tokenType, String keyword, BufferedWriter bufferedWriter) throws IOException {
        writeXML(tokenType, true, bufferedWriter);
        bufferedWriter.write(keyword);
        writeXML(tokenType, false, bufferedWriter);
        bufferedWriter.newLine();
    }

    private static void writeXML(String string, Boolean isStart, BufferedWriter bufferedWriter) throws IOException {
        String s = isStart ? "" : "/";
        bufferedWriter.write("<" + s + string + ">");
    }

    static void startXMLCategory(String category, BufferedWriter bufferedWriter) throws IOException {
        writeXML(category, true, bufferedWriter);
        bufferedWriter.newLine();
    }

    static void endXMLCategory(String category, BufferedWriter bufferedWriter) throws IOException {
        writeXML(category, false, bufferedWriter);
        bufferedWriter.newLine();
    }
}
