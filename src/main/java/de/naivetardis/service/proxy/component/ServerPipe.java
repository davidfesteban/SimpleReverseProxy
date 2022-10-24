package de.naivetardis.service.proxy.component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

//@Slf4j
public class ServerPipe extends Thread {

    private final InputStream inputStream;
    private final OutputStream outputStream;

    public ServerPipe(InputStream inputStream, OutputStream outputStream) {
        super();
        this.inputStream = Objects.requireNonNull(inputStream);
        this.outputStream = Objects.requireNonNull(outputStream);
    }

    @Override
    public void run() {
        super.run();
        int bytesRead;
        byte[] request = new byte[1000000];
        try (outputStream) {
            while ((bytesRead = inputStream.read(request)) != -1) {
                outputStream.write(request, 0, bytesRead);
                outputStream.flush();
                request = new byte[1000000];
            }
        } catch (IOException e) {
            //log.error(e.getMessage());
        }
    }

    public ServerPipe startNow() {
        start();
        return this;
    }
}
