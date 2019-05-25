package com.company.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private final BufferedWriter bufferedWriter;

    public VMWriter(String filepath) throws IOException {
        this.bufferedWriter = new BufferedWriter(new FileWriter(filepath));
    }

    public void writePush(PushPopSegment segment, int index) throws IOException {
        bufferedWriter.write("push " + segment.getName() + " " + index);
        bufferedWriter.newLine();
    }

    void writePop(String segment, int index) {
    }

    public void writeArithmetic(ArithmeticCommand command) throws IOException {
        bufferedWriter.write(command.getName());
        bufferedWriter.newLine();
    }

    void writeLabel(String label) {
    }

    void writeGoto(String label) {

    }

    void writeIf(String label) {
    }

    public void writeCall(String name, int nArgs) throws IOException {
        String newLine = VMKeyword.CALL.getName() + " " + name + " "  + nArgs;
        bufferedWriter.write(newLine);
        bufferedWriter.newLine();
    }

    public void writeFunction(String name, int nLocals) throws IOException {
        String newLine = VMKeyword.FUNCTION.getName() + " " + name + " " + nLocals;
        bufferedWriter.write(newLine);
        bufferedWriter.newLine();
    }

    public void writeReturn() throws IOException {
        bufferedWriter.write("return");
        bufferedWriter.newLine();
    }

    public void close() throws IOException {
        bufferedWriter.close();
    }



}
