package com.nimbly.training.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TempFilesTest {

    @Test
    public void test1() throws IOException {

        File tmp = null;
        try {
            tmp = File.createTempFile("NomFacture-", ".pdf");
            tmp.deleteOnExit();

            System.out.println("Path is " + tmp.getAbsolutePath());
            assertTrue(tmp.exists(), "File should exists");

        } finally {
            if (tmp != null) {
                tmp.delete();
            }
        }

        assertFalse(tmp.exists(), "File should not exists");
    }

    @Test
    public void test2() throws IOException {

        String fileName = withTempFile("NomFacture-", ".pdf", tmp -> {

            System.out.println("Path is " + tmp.getAbsolutePath());
            assertTrue(tmp.exists(), "File should exists");
            return tmp.getAbsolutePath();
        });

        System.out.println("Path is " + fileName);
        assertFalse(new File(fileName).exists(), "File should exists");
    }

    public static <T> T withTempFile(String prefix, String suffix, Callable<T> callable) throws IOException {
        File tmp = null;
        try {
            tmp = File.createTempFile(prefix, suffix);
            tmp.deleteOnExit();
            return callable.call(tmp);

        } finally {
            if (tmp != null) {
                tmp.delete();
            }
        }
    }

    public interface Callable<T> {
        T call(File file);
    }
}