package com.nimbly.training.log4j;

import org.apache.logging.log4j.Level;

import java.util.Objects;

public class Log {

    private final Level level;
    private final String message;

    public Log(Level level, String message) {
        this.level = level;
        this.message = message;
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Log)) return false;
        Log log = (Log) o;
        return Objects.equals(level, log.level)
            && Objects.equals(message, log.message);
    }

    @Override
    public String toString() {
        return "[" + level + "] " + message;
    }
}
