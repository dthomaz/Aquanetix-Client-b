package uk.co.aquanetix.android;

import java.util.Map;

import uk.co.aquanetix.network.RequestDescription;
import android.content.Context;

/**
 * Handles custom logging and exception reporting.
 * Useful for debugging the android client.
 * Sends all logs to AWS.
 * LogLevel:
 *  0 = None
 *  1 = Error
 *  2 = Warn
 *  3 = Info
 */
public class AquaLog {
    
    public static final int NONE = 0;
    public static final int ERROR = 1;
    public static final int WARN = 2;
    public static final int INFO = 3;
    
    private static final LogFileMgr logFile = new LogFileMgr();
    private static final String TAB = "\t";
    private static int logLevel = INFO; // Default = log everything

    public static void info(String msg) {
        if (!shouldLog(INFO)) {
            return;
        }
        logFile.appendMessageAtLogFile("INFO", msg, null);
    }
    
    public static void info(RequestDescription req, String response) {
        if (!shouldLog(INFO)) {
            return;
        }
        info(req, response, null);
    }
    
    public static void info(RequestDescription req, String response, Throwable e) {
        if (!shouldLog(INFO)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(req.getMethod());
        sb.append(TAB);
        sb.append(req.getUrl());
        logMap(sb, "HEADERS: ", req.getHeaders());
        logMap(sb, "PARAMS: ", req.getParams());
        sb.append(TAB);
        sb.append("RESPONSE:");
        sb.append(TAB);
        sb.append(response);
        logFile.appendMessageAtLogFile("INFO", sb.toString(), e);
    }
    
    public static void warn(String msg) {
        if (!shouldLog(WARN)) {
            return;
        }
        logFile.appendMessageAtLogFile("WARN", msg, null);
    }
    
    public static void warn(String msg, Throwable e) {
        if (!shouldLog(WARN)) {
            return;
        }
        logFile.appendMessageAtLogFile("WARN", msg, e);
    }
    
    public static void error(String msg, Throwable e) {
        if (!shouldLog(ERROR)) {
            return;
        }
        logFile.appendMessageAtLogFile("ERROR", msg, e);
    }
    
    private static void logMap(StringBuilder sb, String title, Map<String, String> map) {
        sb.append(TAB);
        sb.append(title);
        for (String key:map.keySet()) {
            sb.append(key);
            sb.append("=");
            sb.append(map.get(key));
            sb.append(", ");
        }
    }
    
    public static void sync(Context ctx) {
        logFile.sync(ctx);
    }
    
    private static boolean shouldLog(int logLevel) {
        return logLevel <= AquaLog.logLevel;
    }
    
    public static void setLogLevel(int logLevel) {
        AquaLog.logLevel = logLevel;
    }
}
