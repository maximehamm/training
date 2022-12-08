package com.nimbly.training.util;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public class PipedStreamCopierTest {

    @Test
    public void test1() {

        AtomicReference<String> result = new AtomicReference<>();

        PipedStreamCopier<String> copier = new PipedStreamCopier<>(
                (OutputStream os) -> {
                    os.write("This is the input".getBytes());
                },
                (InputStream is) -> {
                    result.set(IOUtils.toString(is, StandardCharsets.UTF_8));
                    return "Success";
                },
                20
        );

        String status = copier.run();

        Assertions.assertEquals("Success", status);
        Assertions.assertEquals("This is the input", result.get());

    }
}
