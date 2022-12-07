package com.nimbly.training.log4j

import org.apache.logging.log4j.Level.*
import org.apache.logging.log4j.LogManager
import org.junit.jupiter.api.Test
import org.apache.logging.log4j.ThreadContext;

class TestLog4J: AbstractTestLog4J() {

    @Test
    fun test1Basics() {

        //language=XML
        val config = """
                <Configuration name="ConfigTest">
                    <Appenders>
                        <Console name="Console" target="Console">
                            <PatternLayout pattern="[%p] [%c] %m%n" />
                        </Console>
                        <TestAppender name="TestAppender">
                             <PatternLayout pattern="[%p] [%c] %m%n" />
                        </TestAppender>
                    </Appenders>
                    <Loggers>
                        <Root level="#ROOT#">
                             <AppenderRef ref="Console" /> 
                             <AppenderRef ref="TestAppender" /> 
                        </Root>
                    </Loggers>
                </Configuration>
            """.trimIndent()

        //
        // LEVEL IS WARN
        //
        println("#### LEVEL IS WAN")
        initLog4J(config.replace("#ROOT#", "warn"))
        var logger = LogManager.getLogger("com.nimbly.test.Training")

        logger.log(DEBUG, "Test debug 1")
        logger.log(INFO, "Test info 1")
        logger.log(WARN, "Test warning 1")
        logger.log(ERROR, "Test error 1")
        assertLogs("TestAppender",
            "[WARN] [com.nimbly.test.Training] Test warning 1",
            "[ERROR] [com.nimbly.test.Training] Test error 1")

        //
        // LEVEL IS INFO
        //
        println("#### LEVEL IS INFO")
        initLog4J(config.replace("#ROOT#", "info"))
        logger = LogManager.getLogger("com.nimbly.test.Training")

        logger.log(DEBUG, "Test debug 2")
        logger.log(INFO, "Test info 2")
        logger.log(WARN, "Test warning 2")
        logger.log(ERROR, "Test error 2")
        assertLogs("TestAppender",
            "[INFO] [com.nimbly.test.Training] Test info 2",
            "[WARN] [com.nimbly.test.Training] Test warning 2",
            "[ERROR] [com.nimbly.test.Training] Test error 2")
    }

    @Test
    fun test2MoreLoggers() {

        //language=XML
        initLog4J("""
            <Configuration name="ConfigTest">
                <Appenders>
                    <Console name="Console" target="Console">
                        <PatternLayout pattern="[%p] [%c] %m%n" />
                    </Console>
                    <TestAppender name="TestAppender">
                         <PatternLayout pattern="[%p] [%c] %m%n" />
                    </TestAppender>
                </Appenders>
                <Loggers>
                    <Logger name="com.nimbly.test" level="warn">
                    </Logger>
                    <Root level="error">
                         <AppenderRef ref="Console" /> 
                         <AppenderRef ref="TestAppender" /> 
                    </Root>
                </Loggers>
            </Configuration>
            """.trimIndent()
        )

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

        assertLogs("TestAppender",
            "[WARN] [com.nimbly.test.Training] Training warning",
            "[ERROR] [com.nimbly.test.Training] Training error",
            "[ERROR] [com.apple.ios.IPhone] Apple error")
    }

    @Test
    fun test3SeparatingErrors() {

        //language=XML
        val config = """
            <Configuration name="ConfigTest">
                <Appenders>
                
                    <Console name="Console" target="Console">
                        <PatternLayout pattern="[%p] [%c] %m%n" />
                    </Console>
                    
                    <TestAppender name="AppenderErrors">
                        <PatternLayout pattern="[%p] [%c] %m%n" />
                        <LevelRangeFilter minLevel="ERROR" maxLevel="ERROR"/>
                    </TestAppender>
                    
                    <TestAppender name="AppenderDebug">
                         <PatternLayout pattern="[%p] [%c] %m%n" />
                    </TestAppender>
                    
                </Appenders>
                
                <Loggers>
                
                    <Logger name="com.nimbly.test" level="debug"/>
                    
                    <Root level="error" >
                         <AppenderRef ref="Console" /> 
                         <AppenderRef ref="AppenderErrors" />
                         <AppenderRef ref="AppenderDebug" /> 
                    </Root>
                </Loggers>
            </Configuration>
            """.trimIndent()

        //
        // Additivity is true (default)
        //
        initLog4J(config.replace("#ADDITIVITY#", "true"))

        val logger1 = LogManager.getLogger("com.nimbly.test.Training")
        val logger2 = LogManager.getLogger("com.apple.ios.IPhone")

        logger1.log(DEBUG, "Training debug")
        logger1.log(ERROR, "Training error")

        logger2.log(DEBUG, "Apple debug")
        logger2.log(ERROR, "Apple error")

        assertLogs("AppenderErrors",
            "[ERROR] [com.nimbly.test.Training] Training error",
            "[ERROR] [com.apple.ios.IPhone] Apple error")

        assertLogs("AppenderDebug",
            "[DEBUG] [com.nimbly.test.Training] Training debug",
            "[ERROR] [com.nimbly.test.Training] Training error",
            "[ERROR] [com.apple.ios.IPhone] Apple error")
    }

    @Test
    fun test4AndBusinessLogger() {

        //language=XML
        val config = """
            <Configuration name="ConfigTest">
                <Appenders>
                
                    <Console name="Console" target="Console">
                        <PatternLayout pattern="[%p] [%c] %m%n" />
                    </Console>
                    
                    <TestAppender name="AppenderErrors">
                        <PatternLayout pattern="[%p] [%c] %m%n" />
                        <LevelRangeFilter minLevel="ERROR" maxLevel="ERROR"/>
                    </TestAppender>
                    
                    <TestAppender name="AppenderDebug">
                         <PatternLayout pattern="[%p] [%c] %m%n" />
                    </TestAppender>
                    
                    <TestAppender name="AppenderBusiness">
                         <PatternLayout pattern="[%p] [%c] %m%n" />
                    </TestAppender>
                    
                </Appenders>
                
                <Loggers>
                
                    <Logger name="com.nimbly.business" level="info" additivity="false">
                         <AppenderRef ref="AppenderBusiness" /> 
                    </Logger>
                 
                    <Logger name="com.nimbly.test" level="debug"/>
                    
                    <Root level="error" >
                         <AppenderRef ref="Console" /> 
                         <AppenderRef ref="AppenderErrors" />
                         <AppenderRef ref="AppenderDebug" /> 
                    </Root>
                </Loggers>
            </Configuration>
            """.trimIndent()

        //
        // Additivity is true (default)
        //
        initLog4J(config.replace("#ADDITIVITY#", "true"))

        val logger1 = LogManager.getLogger("com.nimbly.test.Training")
        val logger2 = LogManager.getLogger("com.apple.ios.IPhone")
        val loggerBusiness = LogManager.getLogger("com.nimbly.business.BusinessRecorder")

        logger1.log(DEBUG, "Training debug")
        logger1.log(ERROR, "Training error")

        logger2.log(DEBUG, "Apple debug")
        logger2.log(ERROR, "Apple error")

        loggerBusiness.log(DEBUG, "A business debug")
        loggerBusiness.log(INFO, "A business debug information")

        assertLogs("AppenderErrors",
            "[ERROR] [com.nimbly.test.Training] Training error",
            "[ERROR] [com.apple.ios.IPhone] Apple error")

        assertLogs("AppenderDebug",
            "[DEBUG] [com.nimbly.test.Training] Training debug",
            "[ERROR] [com.nimbly.test.Training] Training error",
            "[ERROR] [com.apple.ios.IPhone] Apple error")

        assertLogs("AppenderBusiness",
            "[INFO] [com.nimbly.business.BusinessRecorder] A business debug information")
    }

    @Test
    fun test5AndSqlLogger() {

        //language=XML
        val config = """
            <Configuration name="ConfigTest">
                <Appenders>
                
                    <Console name="Console" target="Console">
                        <PatternLayout pattern="[%p] [%c] %m%n" />
                    </Console>
                    
                    <TestAppender name="AppenderErrors">
                        <PatternLayout pattern="[%p] [%c] %m%n" />
                        <LevelRangeFilter minLevel="ERROR" maxLevel="ERROR"/>
                    </TestAppender>
                    
                    <TestAppender name="AppenderDebug">
                         <PatternLayout pattern="[%p] [%c] %m%n" />
                    </TestAppender>
                    
                    <TestAppender name="AppenderDebugWithSQL">
                         <PatternLayout pattern="[%p] [%c] %m%n" />
                    </TestAppender>
                   
                    <TestAppender name="AppenderBusiness">
                         <PatternLayout pattern="[%p] [%c] %m%n" />
                    </TestAppender>
                    
                </Appenders>
                
                <Loggers>
                
                    <Logger name="com.nimbly.test" level="debug"/>
                    
                    <Logger name="org.hibernate.SQL" level="debug" additivity="false">
                        <AppenderRef ref="AppenderDebugWithSQL" /> 
                    </Logger>
                    
                    <Logger name="com.nimbly.business" level="info" additivity="false">
                         <AppenderRef ref="AppenderBusiness" /> 
                    </Logger>
                    
                    <Root level="error" >
                         <AppenderRef ref="Console" /> 
                         <AppenderRef ref="AppenderErrors" />
                         <AppenderRef ref="AppenderDebug" /> 
                         <AppenderRef ref="AppenderDebugWithSQL" /> 
                    </Root>
                </Loggers>
            </Configuration>
            """.trimIndent()

        //
        // Additivity is true (default)
        //
        initLog4J(config.replace("#ADDITIVITY#", "true"))

        val logger1 = LogManager.getLogger("com.nimbly.test.Training")
        val logger2 = LogManager.getLogger("com.apple.ios.IPhone")
        val loggerBusiness = LogManager.getLogger("com.nimbly.business.BusinessRecorder")
        val loggerHibernate = LogManager.getLogger("org.hibernate.SQL")

        logger1.log(DEBUG, "Training debug")
        logger1.log(ERROR, "Training error")

        logger2.log(DEBUG, "Apple debug")
        logger2.log(ERROR, "Apple error")

        loggerBusiness.log(DEBUG, "A business debug")
        loggerBusiness.log(INFO, "A business debug information")

        loggerHibernate.log(INFO, "SELECT TOTO.NAME FROM TOTO WHERE ID == 'test'")

        assertLogs("AppenderErrors",
            "[ERROR] [com.nimbly.test.Training] Training error",
            "[ERROR] [com.apple.ios.IPhone] Apple error")

        assertLogs("AppenderDebug",
            "[DEBUG] [com.nimbly.test.Training] Training debug",
            "[ERROR] [com.nimbly.test.Training] Training error",
            "[ERROR] [com.apple.ios.IPhone] Apple error")

        assertLogs("AppenderDebugWithSQL",
            "[DEBUG] [com.nimbly.test.Training] Training debug",
            "[ERROR] [com.nimbly.test.Training] Training error",
            "[ERROR] [com.apple.ios.IPhone] Apple error",
            "[INFO] [org.hibernate.SQL] SELECT TOTO.NAME FROM TOTO WHERE ID == 'test'")

        assertLogs("AppenderBusiness",
            "[INFO] [com.nimbly.business.BusinessRecorder] A business debug information")
    }

    @Test
    fun test6UsingMDC() {

        //language=XML
        initLog4J("""
            <Configuration name="ConfigTest">
                <Appenders>
                    <Console name="Console" target="Console">
                        <PatternLayout pattern="[%p] [%c] %m%n" />
                    </Console>
                    <TestAppender name="TestAppender">
                        <PatternLayout pattern="[%p] [%c] %m {ios=%X{ios}, login=%X{login}}%n" />
                    </TestAppender>
                </Appenders>
                <Loggers>
                    <Logger name="com.nimbly.test" level="warn">
                    </Logger>
                    <Root level="error">
                         <AppenderRef ref="Console" /> 
                         <AppenderRef ref="TestAppender" /> 
                    </Root>
                </Loggers>
            </Configuration>
            """.trimIndent()
        )

        val logger1 = LogManager.getLogger("com.nimbly.test.Training")
        val logger2 = LogManager.getLogger("com.apple.ios.IPhone")

        ThreadContext.put("login", "maxime.hamm@nimbly-consulting.com");
        ThreadContext.put("ios", "15.2.1");

        logger1.log(DEBUG, "Training debug")
        logger1.log(WARN, "Training warning")
        logger1.log(ERROR, "Training error")

        logger2.log(DEBUG, "Apple debug")
        logger2.log(ERROR, "Apple error")

        assertLogs("TestAppender",
            "[WARN] [com.nimbly.test.Training] Training warning {ios=15.2.1, login=maxime.hamm@nimbly-consulting.com}",
            "[ERROR] [com.nimbly.test.Training] Training error {ios=15.2.1, login=maxime.hamm@nimbly-consulting.com}",
            "[ERROR] [com.apple.ios.IPhone] Apple error {ios=15.2.1, login=maxime.hamm@nimbly-consulting.com}")
    }

}