package fy.GW.utils;

// Java program to copy content from
// one file to another

import com.github.javaparser.ast.CompilationUnit;

import java.io.*;

public class JavaSrcFileWriter {

    public static void copyContent(File a, File b) throws Exception {
        FileInputStream in = new FileInputStream(a);
        FileOutputStream out = new FileOutputStream(b);
        try {
            int n;
            // read() function to read the
            // byte of data
            while ((n = in.read()) != -1) {
                // write() function to write
                // the byte of data
                out.write(n);
            }
        }
        finally {
            if (in != null) {
                // close() function to close the
                // stream
                in.close();
            }
            // close() function to close
            // the stream
            if (out != null) {
                out.close();
            }
        }
        System.out.println("File Copied");
    }

    public static void writeParseTree(CompilationUnit cu, File outputFile) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        bw.write(cu.toString());
    }
}

