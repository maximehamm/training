package com.nimbly.training.log4j

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Level.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.ConfigurationSource
import org.apache.logging.log4j.core.config.Configurator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals


class TestLog4J {

    private lateinit var customAppender: StreamAppender

    @Test
    fun testLoadConfig() {

        initLog4J("""
                <Configuration name="ConfigTest">
                    <Appenders>
                        <Console name="Console" target="Console">
                            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n" />
                        </Console>
                    </Appenders>
                    <Loggers>
                        <Root level="error" additivity="true">
                             <AppenderRef ref="Console" /> 
                        </Root>
                    </Loggers>
                </Configuration>
            """.trimIndent()
        )

        val logger: Logger = LogManager.getLogger("com.nimbly.test.Training")

        logger.log(DEBUG, "Test debug")
        logger.log(INFO, "Test info")
        logger.log(WARN, "Test warning")
        logger.log(ERROR, "Test error")

        assertLogs(Log(ERROR, "Test error"))
    }

    @BeforeEach
    fun initTestAppender() {
        customAppender = StreamAppender()
    }

    private fun initLog4J(config: String) {

        val source = ConfigurationSource(config.byteInputStream(Charsets.UTF_8))
        val context = Configurator.initialize(null, source)

        context.configuration.addAppender(customAppender.appender)
        context.configuration.rootLogger.addAppender(customAppender.appender, Level.INFO, null)
    }

    private fun assertLogs(vararg logs: Log) {
        assertContentEquals(
            logs.asList(),
            customAppender.dumpLogs())
    }
}