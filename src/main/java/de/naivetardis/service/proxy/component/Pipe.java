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

    public Pipe(InputStream inputStream, OutputStream outputStream) {
        super();
        this.inputStream = Objects.requireNonNull(inputStream);
        this.outputStream = Objects.requireNonNull(outputStream);
    }

    @Override
    public void run() {
        super.run();
        int bytesRead;
        try (outputStream) {
            byte[] request = new byte[1000000];
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
