package com.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import javafx.application.Platform;

/**
 * Will write to debug log the name of a method each time it is accessed.
 * Exceptions are methods called hundreds of times and redundant methods.
 * Classes with debug log:
 * <p>App
 * <p>Manager
 * <p>Loader
 * <p>All menu classes
 * <p>entities, as needed
 * @since 1.1.2
 */
public abstract class Debug {

    private static final String debugFile = "src\\main\\logs\\debug.txt";
    private static BufferedWriter writer;
    //private static int i = 0;

    public static void init() {
        try {
            writer = new BufferedWriter(new FileWriter(debugFile));
            write(String.valueOf(ZonedDateTime.now()));
            write("Debug writer initialized");
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void close() {
        try {
            write("Writer signing off");
            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a line to the debug log.
     * @param line
     * @since 1.1.2
     */
    public static void write(String line) {
        try {
            //i += 1;
            Platform.runLater(() -> {
                try {
                    writer.write(line + "\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch(RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a line, along with names/references of objects separated by commas, to debug log.
     * @param line
     * @param objs Array of objects
     * @since 1.1.2
     */
    public static void write(String line, Object... objs) {
        StringBuilder sb = new StringBuilder(line);
        for (Object o : objs) {
            sb.append(", " + o);
        }
        write(sb.toString());
    }

    public static void error(Exception e) {
        write(e.toString());
    }

    public static void error(String str, Exception e) {
        write(str + " " + e.toString());
    }

    public static void error(int code, Exception e) {
        write("Error " + code + ": " + e.toString());
    }    

    public static void startThread(String threadName) {
        write("==========START THREAD: " + threadName);
    }

    public static void endThread(String threadName) {
        write("==========END THREAD: " + threadName);
    }
}
