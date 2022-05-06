package fy.commit.entry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {

    public static void writeLog(String outPath, String content) throws IOException {
        final File file = new File(outPath);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            writer.printf("error: %s", content);
            writer.println("\n");
        }
    }
}
