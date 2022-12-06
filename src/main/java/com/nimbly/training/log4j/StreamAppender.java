package com.nimbly.training.log4j;

import kotlin.text.Charsets;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamAppender {

    private final ByteArrayOutputStream stream;
    private final OutputStreamAppender appender;

    public StreamAppender() {
        this.stream = new ByteArrayOutputStream();
        final PatternLayout layout = PatternLayout.createLayout(
                "[%level] %message%n",
                null, null, null, Charset.defaultCharset(), true, false, "", "");
        this.appender = OutputStreamAppender.createAppender(
                layout, null, stream, "Test-stream", true, true);
        this.appender.start();
    }

    public OutputStreamAppender getAppender() {
        return appender;
    }

    public String dump() {
        return stream.toString(Charsets.UTF_8);
    }

    public List<Log> dumpLogs() {
        List<Log> out = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(\\w*)\\] (.*)");
        Matcher matcher = pattern.matcher(dump());
        while (matcher.find()) {
            Level level = Level.getLevel(matcher.group(1));
            String message = matcher.group(2);
            out.add(new Log(level, message));
        }
        return out;
    }
}
