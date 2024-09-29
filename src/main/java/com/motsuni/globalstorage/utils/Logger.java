package com.motsuni.globalstorage.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public class Logger {
    private static final Logger instance = new Logger();

    private boolean hasLogDirectory = false;
    private static final String logPath = "plugins/GlobalStorage/logs";

    private Logger() {
        // make log directory
        Path path = Paths.get(logPath);

        if (!path.toFile().exists()) {
            this.hasLogDirectory = path.toFile().mkdirs();
        } else {
            this.hasLogDirectory = true;
        }
    }

    public void append(String level, String message) {
        if (!this.hasLogDirectory) {
            System.out.println("Failed to create log directory");
            return;
        }

        Date date = new Date();
        // YYYY_MM_DD.log
        String fileName = String.format("%s/%tY_%tm_%td.log", logPath, date, date, date);
        Path path = Paths.get(fileName);

        try {
            String msg = String.format("[%s] %tF %tT %s\n", level, date, date, message);
            Files.writeString(path, msg, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void info(String message) {
        String s = String.format("[GlobalStorage:INFO] %s", message);
        System.out.println(s);

        instance.append("INFO", message);
    }

    public static void error(String message) {
        String s = String.format("[GlobalStorage:ERROR] %s", message);
        System.err.println(s);

        instance.append("ERROR", message);
    }

    public static void warning(String message) {
        String s = String.format("[GlobalStorage:WARNING] %s", message);
        System.out.println(s);

        instance.append("WARNING", message);
    }

    public static void debug(String message) {
        String s = String.format("[GlobalStorage:DEBUG] %s", message);
        System.out.println(s);

        instance.append("DEBUG", message);
    }
}
