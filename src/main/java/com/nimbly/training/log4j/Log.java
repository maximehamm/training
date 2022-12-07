package com.nimbly.training.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

import java.io.Serializable;
import java.util.Objects;

public class Log {

    private final String logger;
    private final Level level;
    private final String message;
    private final String formattedMessage;
    private final ReadOnlyStringMap contextData;

    public Log(LogEvent event, Layout<? extends Serializable> layout) {
        this(event.getLevel(),
             event.getLoggerName(),
             event.getMessage().getFormattedMessage(),
             (layout != null) ? layout.toSerializable(event).toString().trim() : null,
             event.getContextData());
    }

    public Log(Level level, String logger, String message, String formattedMessage, ReadOnlyStringMap contextData) {
        this.logger = logger;
        this.level = level;
        this.message = message;
        this.formattedMessage = formattedMessage;
        this.contextData = contextData;
    }

    public String getLogger() {
        return logger;
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public ReadOnlyStringMap getContextData() {
        return contextData;
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
            && Objects.equals(logger, log.logger)
            && Objects.equals(message, log.message);
    }

    @Override
    public String toString() {
        if (formattedMessage!=null)
            return formattedMessage;

        String context = (contextData!=null && !contextData.isEmpty())
                ? " {" + contextData.toString() + "}"
                : "";
        return "[" + level + "] [" + logger + "] " + message + context;
    }
}
