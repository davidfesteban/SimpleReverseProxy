package de.naivetardis.service.proxy.component;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

@Slf4j
public class Pipe extends Thread {

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private InputStream previousSniffedData;

    public Pipe(InputStream inputStream, OutputStream outputStream) {
        this(inputStream, outputStream, null);
    }

    public Pipe(InputStream inputStream, OutputStream outputStream, InputStream previousSniffedData) {
        super();
        this.inputStream = Objects.requireNonNull(inputStream);
        this.outputStream = Objects.requireNonNull(outputStream);
        this.previousSniffedData = previousSniffedData;
    }

    @Override
    public void run() {
        super.run();
        int bytesRead;
        byte[] request = new byte[1000000];
        try (outputStream) {
            while (previousSniffedData != null && (bytesRead = previousSniffedData.read(request)) != -1) {
                outputStream.write(request, 0, bytesRead);
                outputStream.flush();
                request = new byte[1000000];
            }

            while ((bytesRead = inputStream.read(request)) != -1) {
                outputStream.write(request, 0, bytesRead);
                outputStream.flush();
                request = new byte[1000000];
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public Pipe startNow() {
        start();
        return this;
    }
}
