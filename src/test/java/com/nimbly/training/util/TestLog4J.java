package com.nimbly.training.util;

import com.nimbly.training.log4j.AbstractTestLog4J;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotNull;
import java.util.function.Supplier;

import static com.nimbly.training.log4j.TestLog4JKt.dosomething;

public class TestLog4J extends AbstractTestLog4J {

    @Test
    public void test7ElapsedTimeJava() {

        initLog4J(
            "<Configuration name=\"ConfigTest\">" +
            "    <Appenders>" +
            "        <Console name=\"Console\" target=\"Console\">" +
            "            <PatternLayout pattern=\"[%p] [%c] %m%n\" />" +
            "        </Console>" +
            "        <TestAppender name=\"TestAppender\">" +
            "             <PatternLayout pattern=\"[%p] [%c] %m%n\" />" +
            "        </TestAppender>" +
            "    </Appenders>" +
            "    <Loggers>" +
            "        <Root level=\"debug\">" +
            "             <AppenderRef ref=\"Console\" /> " +
            "             <AppenderRef ref=\"TestAppender\" /> " +
            "        </Root>" +
            "    </Loggers>" +
            "</Configuration>");

        Logger logger = LogManager.getLogger("com.nimbly.test.Training");

        int result = elapsed2(logger, Level.DEBUG, "Processing my stuff", () -> {
                // the code to monitor
                return dosomething(5);
            }
        );
        logger.info("Result is {}", result);

        assertLogs("TestAppender",
                "[DEBUG] [com.nimbly.test.Training] Processing my stuff - START",
                "[DEBUG] [com.nimbly.test.Training] Value is 1",
                "[DEBUG] [com.nimbly.test.Training] Value is 2",
                "[DEBUG] [com.nimbly.test.Training] Value is 3",
                "[DEBUG] [com.nimbly.test.Training] Value is 4",
                "[DEBUG] [com.nimbly.test.Training] Value is 5",
                "[DEBUG] [com.nimbly.test.Training] Processing my stuff - END - Duration = 999 ms",
                "[INFO] [com.nimbly.test.Training] Result is 5");

    }

    public <T> T elapsed2(@NotNull Logger logger,
                          @NotNull Level level,
                          @NotNull String message,
                          @NotNull Supplier<T> function) {

        logger.log(level, message + " - START");

        long start = System.currentTimeMillis();
        T r = function.get();
        long end = System.currentTimeMillis();

        logger.log(level, message + " - END - Duration = " + (end - start) + " ms");
        return r;
    }
}