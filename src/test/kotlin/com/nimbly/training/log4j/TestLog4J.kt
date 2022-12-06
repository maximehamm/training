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
                        <Root level="#ROOT#" additivity="true">
                             <AppenderRef ref="Console" /> 
                        </Root>
                    </Loggers>
                </Configuration>
            """.trimIndent()

        //
        // LEVEL IS WARN
        //
        initLog4J(config.replace("#ROOT#", "warn"))
        var logger = LogManager.getLogger("com.nimbly.test.Training")

        logger.log(DEBUG, "Test debug 1")
        logger.log(INFO, "Test info 1")
        logger.log(WARN, "Test warning 1")
        logger.log(ERROR, "Test error 1")
        assertLogs(
            "[WARN] [com.nimbly.test.Training] Test warning 1",
            "[ERROR] [com.nimbly.test.Training] Test error 1")

        //
        // LEVEL IS INFO
        //
        initLog4J(config.replace("#ROOT#", "info"))
        logger = LogManager.getLogger("com.nimbly.test.Training")

        logger.log(DEBUG, "Test debug 2")
        logger.log(INFO, "Test info 2")
        logger.log(WARN, "Test warning 2")
        logger.log(ERROR, "Test error 2")
        assertLogs(
            "[INFO] [com.nimbly.test.Training] Test info 2",
            "[WARN] [com.nimbly.test.Training] Test warning 2",
            "[ERROR] [com.nimbly.test.Training] Test error 2")
    }

    @Test
    fun testSimple2() {

        val config = """
                <Configuration name="ConfigTest">
                    <Appenders>
                        <Console name="Console" target="Console">
                            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n" />
                        </Console>
                    </Appenders>
                    <Loggers>
                        <Logger name="com.nimbly.test" level="#com.nimbly.test#">
                            <AppenderRef ref="Console"/>
                        </Logger>
                        <Root level="#ROOT#" additivity="true">
                             <AppenderRef ref="Console" /> 
                        </Root>
                    </Loggers>
                </Configuration>
            """.trimIndent()

        //
        initLog4J(config
            .replace("#ROOT#", "error")
            .replace("#com.nimbly.test#", "warn"))

        val logger1 = LogManager.getLogger("com.nimbly.test.Training")
        val logger2 = LogManager.getLogger("com.apple.ios.IPhone")

        logger1.log(DEBUG, "Training debug")
        logger1.log(INFO, "Training info")
        logger1.log(WARN, "Training warning")
        logger1.log(ERROR, "Training error")

        logger2.log(DEBUG, "Apple debug")
        logger2.log(INFO, "Apple info")
        logger2.log(WARN, "Apple warning")
        logger2.log(ERROR, "Apple error")

        assertLogs(
            "[WARN] [com.nimbly.test.Training] Training warning",
            "[ERROR] [com.nimbly.test.Training] Training error",
            "[ERROR] [com.apple.ios.IPhone] Apple error")
    }
}