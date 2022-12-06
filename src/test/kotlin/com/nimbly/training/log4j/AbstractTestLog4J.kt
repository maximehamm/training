package com.nimbly.training.log4j

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.ConfigurationSource
import org.apache.logging.log4j.core.config.Configurator
import kotlin.test.assertContentEquals

abstract class AbstractTestLog4J {

    private lateinit var customAppender: StreamAppender
    private var context: LoggerContext? = null

    protected fun initLog4J(config: String) {

        if (context != null) {
            LogManager.getFactory().removeContext(context)
        }

        context = Configurator.initialize(null,
            ConfigurationSource(config.byteInputStream(Charsets.UTF_8)))

        val appender = context!!.configuration.rootLogger.appenders[StreamAppender.NAME]
        if (appender == null) {
            customAppender = StreamAppender()
            context!!.configuration.addAppender(customAppender.appender)
            context!!.configuration.rootLogger.addAppender(customAppender.appender, null, null)
        }
        else {
            customAppender.reset()
        }
    }

    protected fun assertLogs(vararg logs: Log) {
        assertContentEquals(
            logs.asList(),
            customAppender.dumpLogs())
    }
}