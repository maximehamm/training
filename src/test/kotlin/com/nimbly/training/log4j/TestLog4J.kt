package com.nimbly.training.log4j

import org.apache.logging.log4j.Level.*
import org.apache.logging.log4j.LogManager
import org.junit.jupiter.api.Test

class TestLog4J: AbstractTestLog4J() {

    @Test
    fun testSimple1() {

        val config = """
                <Configuration name="ConfigTest">
                    <Appenders>
                        <Console name="Console" target="Console">
                            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n" />
                        </Console>
                    </Appenders>
                    <Loggers>
                        <Root level="#ROOT_LEVEL#" additivity="true">
                             <AppenderRef ref="Console" /> 
                        </Root>
                    </Loggers>
                </Configuration>
            """.trimIndent()

        //
        // LEVEL IS WARN
        //
        initLog4J(config.replace("#ROOT_LEVEL#", "warn"))
        var logger = LogManager.getLogger("com.nimbly.test.Training")

        logger.log(DEBUG, "Test debug 1")
        logger.log(INFO, "Test info 1")
        logger.log(WARN, "Test warning 1")
        logger.log(ERROR, "Test error 1")
        assertLogs(
            Log(WARN, "com.nimbly.test.Training", "Test warning 1"),
            Log(ERROR, "com.nimbly.test.Training", "Test error 1"))

        //
        // LEVEL IS INFO
        //
        initLog4J(config.replace("#ROOT_LEVEL#", "info"))
        logger = LogManager.getLogger("com.nimbly.test.Training")

        logger.log(DEBUG, "Test debug 2")
        logger.log(INFO, "Test info 2")
        logger.log(WARN, "Test warning 2")
        logger.log(ERROR, "Test error 2")
        assertLogs(
            Log(INFO, "com.nimbly.test.Training", "Test info 2"),
            Log(WARN, "com.nimbly.test.Training", "Test warning 2"),
            Log(ERROR, "com.nimbly.test.Training", "Test error 2"))
    }
}