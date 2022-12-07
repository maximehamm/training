package com.nimbly.training.log4j

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.ConfigurationSource
import org.apache.logging.log4j.core.config.Configurator
import kotlin.test.assertEquals

abstract class AbstractTestLog4J {

    companion object {
        private var context: LoggerContext? = null
    }

    protected fun initLog4J(config: String) {

        if (context != null) {
            LogManager.getFactory().removeContext(context)
        }

        context = Configurator.initialize(null,
            ConfigurationSource(config.byteInputStream(Charsets.UTF_8)))

        ThreadContext.clearAll()
    }

    protected fun assertLogs(appender: String,
        vararg logs: String) {

        val testAppender = context!!.configuration.appenders[appender] as TestAppender

        assertEquals(
            logs.asList().joinToString("\n"),
            testAppender.events
                .map { it.toString().trim() }
                .joinToString("\n"))
    }
}