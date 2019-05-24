package com.company.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private final BufferedWriter bufferedWriter;
    private String filepath;

    public VMWriter(String filepath) throws IOException {
        this.filepath = filepath;
        this.bufferedWriter = new BufferedWriter(new FileWriter(filepath));
    }

    void writePush(String segment, int index) {
    }

    void writePop(String segment, int index) {
    }

    void writeArithmetic(String command) {
    }

    void writeLabel(String label) {
    }

    void writeGoto(String label) {

    }

    void writeIf(String label) {
    }

    void writeCall(String name, int nArgs) {
    }

    public void writeFunction(String name, int nLocals) throws IOException {
        String newLine = VMKeyword.FUNCTION.getName() + " " + name + " " + nLocals;
        bufferedWriter.write(newLine);
        bufferedWriter.newLine();
    }

    void writeReturn() {
    }

    public void close() throws IOException {
        bufferedWriter.close();
    }



}
