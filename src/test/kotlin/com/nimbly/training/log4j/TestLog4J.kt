package com.nimbly.training.log4j

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Level.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.ThreadContext
import org.apache.logging.log4j.message.SimpleMessage
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class TestLog4J: AbstractTestLog4J() {

    /**
     * Straightforward example
     */
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

    /**
     * Basic example
     */
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

    /**
     * Use case :
     *  - 1 file for errors only
     *  - 1 file for all
     */
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
        initLog4J(config)

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

    /**
     * Use case : Having a file only for specific logger(s)
     */
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

    /**
     * Use case :
     *  - 1 file for usual logs
     *  - 1 file for verbose loggers (for example sql queries)
     */
    @Test
    fun test5AndVerboseLoggers() {

        //language=XML
        val config = """
            <Configuration name="ConfigTest">
                <Appenders>
                
                    <Console name="Console" target="Console">
                        <PatternLayout pattern="[%p] [%c] %m%n" />
                    </Console>
                    
                    <TestAppender name="UsualAppender">
                         <PatternLayout pattern="[%p] [%c] %m%n" />
                    </TestAppender>
                    
                    <TestAppender name="VerboseAppender">
                         <PatternLayout pattern="[%p] [%c] %m%n" />
                    </TestAppender>
                    
                </Appenders>
                
                <Loggers>
                
                    <Logger name="com.nimbly.test" level="debug"/>
                    
                    <Logger name="org.hibernate.SQL" level="debug" additivity="false">
                        <AppenderRef ref="VerboseAppender" /> 
                    </Logger>
                    
                    <Root level="error" >
                         <AppenderRef ref="Console" /> 
                         <AppenderRef ref="UsualAppender" /> 
                         <AppenderRef ref="VerboseAppender" /> 
                    </Root>
                </Loggers>
            </Configuration>
            """.trimIndent()

        initLog4J(config)

        val logger1 = LogManager.getLogger("com.nimbly.test.Training")
        val logger2 = LogManager.getLogger("com.apple.ios.IPhone")
        val loggerHibernate = LogManager.getLogger("org.hibernate.SQL")

        logger1.log(DEBUG, "Training debug")
        logger1.log(ERROR, "Training error")

        logger2.log(DEBUG, "Apple debug")
        logger2.log(ERROR, "Apple error")

        loggerHibernate.log(INFO, "SELECT TOTO.NAME FROM TOTO WHERE ID == 'test'")

        assertLogs("UsualAppender",
            "[DEBUG] [com.nimbly.test.Training] Training debug",
            "[ERROR] [com.nimbly.test.Training] Training error",
            "[ERROR] [com.apple.ios.IPhone] Apple error")

        assertLogs("VerboseAppender",
            "[DEBUG] [com.nimbly.test.Training] Training debug",
            "[ERROR] [com.nimbly.test.Training] Training error",
            "[ERROR] [com.apple.ios.IPhone] Apple error",
            "[INFO] [org.hibernate.SQL] SELECT TOTO.NAME FROM TOTO WHERE ID == 'test'")
    }

    /**
     * Use case : Adding information on all logs, for example the user login
     */
    @Test
    fun test6UsingMDC() {

        //language=XML
        initLog4J("""
            <Configuration name="ConfigTest">
                <Appenders>
                    <Console name="Console" target="Console">
                        <PatternLayout pattern="[%p] [%c] [%X{userId}] %m {email=%X{email}}%n" />
                    </Console>
                    <TestAppender name="TestAppender">
                        <PatternLayout pattern="[%p] [%c] [%X{userId}] %m {email=%X{email}}%n" />
                    </TestAppender>
                </Appenders>
                <Loggers>
                    <Root level="debug">
                         <AppenderRef ref="Console" /> 
                         <AppenderRef ref="TestAppender" /> 
                    </Root>
                </Loggers>
            </Configuration>
            """.trimIndent()
        )

        val logger = LogManager.getLogger("com.nimbly.test.Training")

        thread {

            logger.log(DEBUG, "Logging...")

            ThreadContext.put("email", "maxime.hamm@nimbly-consulting.com");
            ThreadContext.put("userId", "Nimbly")

            logger.log(DEBUG, "A first log")

            sleep(200)
            logger.log(ERROR, "A log with error")
        }

        thread {

            sleep(110)
            logger.log(DEBUG, "Logging...")

            ThreadContext.put("email", "steve@apple.com");
            ThreadContext.put("userId", "Apple")

            logger.log(DEBUG, "A first log")

            logger.log(ERROR, "A log with error")
        }

        sleep(300)

        assertLogs("TestAppender",
            "[DEBUG] [com.nimbly.test.Training] [] Logging... {email=}",
            "[DEBUG] [com.nimbly.test.Training] [Nimbly] A first log {email=maxime.hamm@nimbly-consulting.com}",
            "[DEBUG] [com.nimbly.test.Training] [] Logging... {email=}",
            "[DEBUG] [com.nimbly.test.Training] [Apple] A first log {email=steve@apple.com}",
            "[ERROR] [com.nimbly.test.Training] [Apple] A log with error {email=steve@apple.com}",
            "[ERROR] [com.nimbly.test.Training] [Nimbly] A log with error {email=maxime.hamm@nimbly-consulting.com}"
        )
    }

    /**
     * Use case : Logging elapsed time
     */
    @Test
    fun test7ElapsedTime() {

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
                    <Root level="debug">
                         <AppenderRef ref="Console" /> 
                         <AppenderRef ref="TestAppender" /> 
                    </Root>
                </Loggers>
            </Configuration>
            """.trimIndent()
        )

        val logger = LogManager.getLogger("com.nimbly.test.Training")

        val result = elapsed(logger, DEBUG, "Processing my stuff") {
            dosomething(5)
        }
        logger.info("Result is $result")

        assertLogs("TestAppender",
            "[DEBUG] [com.nimbly.test.Training] Processing my stuff - START",
            "[DEBUG] [com.nimbly.test.Training] Value is 1",
            "[DEBUG] [com.nimbly.test.Training] Value is 2",
            "[DEBUG] [com.nimbly.test.Training] Value is 3",
            "[DEBUG] [com.nimbly.test.Training] Value is 4",
            "[DEBUG] [com.nimbly.test.Training] Value is 5",
            "[DEBUG] [com.nimbly.test.Training] Processing my stuff - END - Duration = XXX ms",
            "[INFO] [com.nimbly.test.Training] Result is 5")
    }

    /**
     * Use case : Logging elapsed time and warn if time exceed some limit
     */
    @Test
    fun test8ElapsedTimeAndWarn() {

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
                    <Root level="debug">
                         <AppenderRef ref="Console" /> 
                         <AppenderRef ref="TestAppender" /> 
                    </Root>
                </Loggers>
            </Configuration>
            """.trimIndent()
        )

        val logger = LogManager.getLogger("com.nimbly.test.Training")

        elapsed(logger, DEBUG, "Processing ONE", warnlimit = 200) {
            logger.debug("Run - speed")
            sleep(50)
        }

        elapsed(logger, DEBUG, "Processing TWO", warnlimit = 200) {
            logger.debug("Run - too long")
            sleep(250)
        }

        assertLogs("TestAppender",
            "[DEBUG] [com.nimbly.test.Training] Processing ONE - START",
            "[DEBUG] [com.nimbly.test.Training] Run - speed",
            "[DEBUG] [com.nimbly.test.Training] Processing ONE - END - Duration = XXX ms",
            "[DEBUG] [com.nimbly.test.Training] Processing TWO - START",
            "[DEBUG] [com.nimbly.test.Training] Run - too long",
            "[WARN] [com.nimbly.test.Training] Processing TWO - END - Duration = XXX ms")
    }

    /**
     * Use case : do not evalate debug message if debug level does not need it
     */
    @Test
    fun test9LoggerMessageLateInit() {

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
                        <Root level="info">
                             <AppenderRef ref="Console" /> 
                             <AppenderRef ref="TestAppender" /> 
                        </Root>
                    </Loggers>
                </Configuration>
            """.trimIndent()

        initLog4J(config)

        val logger = LogManager.getLogger("com.nimbly.test.Training")

        // Message is expected :
        //   selected logger level (i.e. DEBUG) is equals or higher as config (i.e. INFO)
        logger.log(INFO) {
            SimpleMessage(buildMesage("First"));
        }

        // Message is NOT expected :
        //   selected logger level (i.e. DEBUG) is lower as config (i.e. INFO)
        logger.log(DEBUG) {
            SimpleMessage(buildMesage("Second"));
        }

        assertLogs("TestAppender",
            "[INFO] [com.nimbly.test.Training] Heavy message 'First' + generated")
    }

    private fun buildMesage(msg: String): String {
        var logger = LogManager.getLogger("com.nimbly.test.Builder")

        val string = "Heavy message '$msg' + generated"
        logger.debug(string)
        return string
    }

}

fun dosomething(count: Int): Int {
    val logger = LogManager.getLogger("com.nimbly.test.Training")
    var i = 0;
    repeat(count) {
        i++
        logger.debug("Value is $i")
        sleep(100)
    }
    return i;
}

fun <T> elapsed(
    logger: Logger,
    level: Level,
    message: String,
    warnlimit: Long? = null,
    function: () -> T
): T {
    logger.log(level, "$message - START")

    val start = System.currentTimeMillis()
    val r = function.invoke()
    val end = System.currentTimeMillis()
    val elapsed = end - start

    var l = level
    if (warnlimit != null && elapsed > warnlimit &&  l.intLevel() > WARN.intLevel()) {
        l = WARN
    }
    logger.log(l, "$message - END - Duration = $elapsed ms")
    return r
}