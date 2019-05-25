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
        writePushPop(true, segment, index);
    }

    public void writePop(PushPopSegment segment, int index) throws IOException {
        writePushPop(false, segment, index);
    }

    public void writeArithmetic(ArithmeticCommand command) throws IOException {
        bufferedWriter.write(command.getName());
        bufferedWriter.newLine();
    }

    public void writeLabel(String label) throws IOException {
        writeLabelCommand("label",label);
    }

    public void writeGoto(String label) throws IOException {
        writeLabelCommand("goto",label);
    }

    public void writeIf(String label) throws IOException {
        writeLabelCommand("if-goto",label);
    }

    public void writeCall(String name, int nArgs) throws IOException {
        String newLine = VMKeyword.CALL.getName() + " " + name + " " + nArgs;
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

    private void writePushPop(boolean isPush, PushPopSegment segment, int index) throws IOException {
        String command = isPush ? "push " : "pop ";
        bufferedWriter.write(command + segment.getName() + " " + index);
        bufferedWriter.newLine();
    }

    private void writeLabelCommand(String command, String label) throws IOException {
        bufferedWriter.write(command + " " + label);
        bufferedWriter.newLine();
    }

}
