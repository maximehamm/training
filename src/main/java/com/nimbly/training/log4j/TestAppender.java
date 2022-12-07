package com.nimbly.training.log4j;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.util.ArrayList;
import java.util.List;

@Plugin(name = "TestAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE)
public class TestAppender extends AbstractAppender {

    private final List<Log> events = new ArrayList<>();

    protected TestAppender(String name, Filter filter) {
        super(name, filter, null);
    }

    @PluginFactory
    public static TestAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter) {
        return new TestAppender(name, filter);
    }

    @Override
    public void append(LogEvent event) {
        events.add(new Log(event.getLevel(), event.getLoggerName(), event.getMessage().getFormattedMessage()));
    }

    public List<Log> getEvents() {
        return events;
    }
}