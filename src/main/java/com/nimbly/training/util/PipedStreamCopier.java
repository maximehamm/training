package com.nimbly.training.util;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PipedStreamCopier<T> {

    private T result;
    private Exception resultException;

    private final Writer writer;
    private final Reader<T> reader;
    private final long timeout;

    private final PipedOutputStream pos = new PipedOutputStream();
    private final PipedInputStream pis = new PipedInputStream();

    public PipedStreamCopier(Writer writer, Reader<T> reader, long timeout) {
        this.writer = writer;
        this.reader = reader;
        this.timeout = timeout;

        try {
            pis.connect(pos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public T run() {

        ExecutorService service = Executors.newFixedThreadPool(2);

        service.submit(() -> {
            try {
                result = reader.read(pis);
            } catch (Exception e) {
                this.resultException = e;
            }
        });

        service.submit(() -> {
            try {
                writer.write(pos);
                pos.flush();
                pos.close();
            } catch (Exception e) {
                this.resultException = e;
            }
        });

        service.shutdown();

        try {
            //noinspection ResultOfMethodCallIgnored
            service.awaitTermination(this.timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (this.resultException != null)
            throw new RuntimeException(this.resultException);

        return result;
    }

    public interface Writer {
        void write(OutputStream os) throws IOException;
    }

    public interface Reader<T> {
        T read(InputStream is) throws IOException;
    }
}

