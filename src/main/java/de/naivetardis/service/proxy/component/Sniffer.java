package de.naivetardis.service.proxy.component;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Predicate;

@Slf4j
public class Sniffer {
    private final InputStream inputStream;
    private final StringBuilder reallyLongRequest = new StringBuilder("");

    public Sniffer(InputStream inputStream) {
        super();
        this.inputStream = Objects.requireNonNull(inputStream);
    }

    public void read() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;

            while (!(inputLine = in.readLine()).equals(""))
                reallyLongRequest.append(inputLine);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public boolean applyFilter(Predicate<String> filter) {
        return filter.test(reallyLongRequest.toString());
    }

    public InputStream getStoredInputStream() {
        return new ByteArrayInputStream(reallyLongRequest.toString().getBytes());
    }
}
