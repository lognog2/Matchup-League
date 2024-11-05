package com.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.HashMap;
import javafx.application.Platform;

/**
 * Will write to debug logs or error log the name of a method each time it is accessed.
 * <p>Debug write: Prints name of method and values of parameters in debug
 * <p>Error write: Prints error message in debug and its stack trace.
 * Exceptions are methods called hundreds of times in a row and redundant methods.
 * <p>Classes with debug log:
 * <p>App
 * <p>Manager
 * <p>Loader
 * <p>All menu classes
 * <p>Entities, as needed
 * @since 1.1.2
 * @version 2
 * @see ExitCode
 * @see Log
 */
public abstract class Debug {

    private static final Log debugLog = new Log("debug.txt");
    private static final Log errorLog = new Log("error.txt");
    private static Log[] logs = {debugLog, errorLog};
    private static Map<Integer, String> codeMap;
    //private static int i = 0;

    public static void init() {
        write("Debug.init");
        codeMap = new HashMap<>();
        ExitCode.init();
    }

    /**
     * Closes all logs.
     * @see Log#close()
     * @since 1.2.0
     */
    public static void close() {
        for (Log log : logs) {
            log.close();
        }
    }

    /* DEBUG LOG */

    /**
     * Writes a line to the debug log. All other debug write methods funnel into this method.
     * @param line
     * @since 1.1.2
     * @see Log#write(Object)
     */
    public static void write(Object obj) {
        //if (obj == null) obj = "null";
        debugLog.write(obj);
    }

    /**
     * Writes a line, along with names/references of objects separated by commas, to debug log.
     * @param line
     * @param objs Array of objects
     * @since 1.1.2
     * @see #write(Object)
     */
    public static void write(String line, Object... objs) {
        StringBuilder sb = new StringBuilder(line);
        for (Object o : objs) {
            if (o == null) o = "null";
            sb.append(", " + o.toString());
        }
        write(sb);
    }

    /**
     * Writes a message to the debug log from an {@link ExitCode} and an additional message.
     * <p>If the passed code is less than zero, it is automatically called as {@link #error(int)}.
     * @param code exit code
     * @param obj object to add to message on new line, unless null
     * @since 1.2.0
     * @see #write(Object)
     */
    public static void write(int code, Object obj) {
        
        if (code > 0) {
            StringBuilder sb = new StringBuilder("MLEXIT " + code + ": " + codeMap.get(code));
            if (obj != null) sb.append("\n" + obj.toString());
            write(sb);
        } else {
            error(code, new Exception(obj.toString()));
        }
    }

    /**
     * Writes a message to the debug log from an {@link ExitCode}.
     * <p>If the passed code is less than zero, it is automatically called as {@link #error(int)}.
     * @param code exit code
     * @since 1.2.0
     * @see #write(Object)
     */
    public static void write(int code) {
        if (code < 0) {
            error(code);
        } else {
            write(code, null);
        }
    }

    /* DEBUG WARN */

    /**
     * Writes a warning message to the debug log with no exit code.
     * @param obj object to write
     * @since 1.2.0
     * @see #write(Object)
     */
    public static void warn(Object obj) {
        write("MLWARN\n" + obj.toString());
    }

    /**
     * Writes a warning message to the debug log.
     * @param obj object to include with warning message
     * @since 1.2.0
     * @see #write(Object)
     */
    public static void warn(int code, Object obj) {
        StringBuilder sb = new StringBuilder("MLWARN " + code + ": " + codeMap.get(code));
        if (obj != null) sb.append("\n" + obj.toString());
        write(sb);
    }

    /**
     * Writes a warning message with the passed exit code and a null object.
     * @param code exit code
     * @since 1.2.0
     * @see #warn(int, Object)
     */
    public static void warn(int code) {
        warn(code, null);
    }

    /* DEBUG SPECIAL PHRASES */

    /**
     * Writes a message to the debug log indicating the start of a thread.
     * @param threadName
     * @since 1.1.2
     * @see #write(Object)
     */
    public static void startThread(String threadName) {
        write("==========START THREAD: " + threadName);
    }

    /**
     * Writes a message to the debug log indicating the end of a thread.
     * @param threadName
     * @since 1.1.2
     * @see #write(Object)
     */
    public static void endThread(String threadName) {
        write("==========END THREAD: " + threadName);
    }

    /* ERROR LOG */

    /**
     * Writes an object to the error log. All other error write methods funnel into this method.
     * @param obj object
     * @since 1.2.0
     * @see Log#write(Object)
     */
    public static void error(Object obj) {
        errorLog.write(obj);
    }

    /**
     * Writes an object to the debug log and a stack trace to the error log.
     * @param obj object
     * @param ste stack trace element
     * @since 1.2.0
     * @see #error(Object)
     */
    public static void error(Object obj, StackTraceElement[] ste) {
        write(">:(\n" + obj.toString());
        error(ste);
    }

    /**
     * Writes an exception message to the debug log and its stack trace to the error log.
     * @since 1.2.0
     * @see #error(Object, StackTraceElement[])
     */
    public static void error(Exception e) {
        error(e, e.getStackTrace());
    }

    /**
     * Writes an {@link ExitCode} error and a thrown exception to the debug and error logs.
     * @param code exit code
     * @param e exception
     * @since 1.2.0
     * @see #error(Object, StackTraceElement[])
     */
    public static void error(int code, Exception e) {
        error("MLERROR " + code + ": " + codeMap.get(code) + "\n" + e.getMessage(), e.getStackTrace());
    }
    
    /**
     * Writes an {@link ExitCode} error and a new exception with the passed message to the debug and error logs.
     * @param code exit code
     * @param message exception message
     * @since 1.2.0
     * @see #error(Object, StackTraceElement[])
     */
    public static void error(int code, String message) {
        Exception e = new Exception(message);
        error(code, e);
    }  
    
    /**
     * Writes an {@link ExitCode} error to the debug and error logs without a preexisting exception.
     * @param code exit code
     * @since 1.2.0
     * @see #error(int, String)
     */
    public static void error(int code) {
        error(code, "This error was thrown without an exception");
    }  

    /**
     * Exit codes are given at the end of a task according to its result.
     * Positive codes are completions and negative codes are incompletions.
     * @since 1.2.0
     */
    enum ExitCode {
        CONFLICTWARN (5, "Conflict warning"),
        NOTASK (0, "No actions taken"),
        SUCCESS (1, "Success"),
        JAVAERROR (-1, "Java error"),
        APIERROR (-2, "SQL/Hibernate error"),
        INSUFFDATA(-3, "Insufficient data"),
        LOGERROR(-4, "Log error"),
        CONFLICT(-5, "Conflict error");
        
        private final int code;
        private final String message;

        ExitCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public static void init() {
            for (ExitCode ec : ExitCode.values()) {
                codeMap.put((Integer)ec.code, ec.message); 
            }
        }

        public int getCode() {
            return code;
        }
        public String getMessage() {
            return message;
        }

    } //end enum ExitCode
} //end class Debug

/**
 * Consists of a file and a writer, and writes given info to the file
 * @since 1.2.0
 */
class Log {
    private final String logPath = "src\\main\\logs\\";
    private final String filePath;
    private BufferedWriter writer;

    public Log (String fileName) {
        filePath = logPath + fileName;
        try {
            writer = new BufferedWriter(new FileWriter(filePath));
            write("Debug writer initialized");
            write(String.valueOf(ZonedDateTime.now()));
            write("File location: " + filePath + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a line to the log.
     * @param line
     * @since 1.2.0
     */
    public void write(Object obj) {
        Platform.runLater(() -> {
            try {
                writer.write(obj.toString() + "\n");
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void close() {
        try {
            write("Writer signing off");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

} //end class Log
