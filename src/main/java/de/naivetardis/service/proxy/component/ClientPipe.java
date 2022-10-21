package de.naivetardis.service.proxy.component;

import de.naivetardis.service.utils.PropertiesContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class ClientPipe extends Thread {

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Properties context;
    private boolean validated;
    public ClientPipe(InputStream inputStream, OutputStream outputStream) {
        super();
        this.inputStream = Objects.requireNonNull(inputStream);
        this.outputStream = Objects.requireNonNull(outputStream);
        this.context = PropertiesContext.getInstance().getContext();
        validated = false;
    }

    @Override
    public void run() {
        super.run();
        int bytesRead;
        byte[] request = new byte[1000000];
        try (outputStream) {
            while ((bytesRead = inputStream.read(request)) != -1) {
                outputStream.write(request, 0, bytesRead);

                if(!validated) {
                    checkAuth(request);
                }

                outputStream.flush();
                request = new byte[1000000];
            }
        } catch (Exception e) {
            if(e instanceof SecurityException){
                throw new SecurityException(e);
            } else {
                log.error(e.getMessage());
            }
        }
    }

    public ClientPipe startNow() {
        start();
        return this;
    }

    private void checkAuth(byte[] request) {
        //Sniffer starts reading
        String incomingText = new String(request, StandardCharsets.UTF_8);
        String headerTokenValue = readHeaderToken(incomingText);
        log.info(headerTokenValue);

        //Validate client is authenticated
        throwExceptionIfNotAuthenticated(buildValidationRequest(headerTokenValue));
        validated = true;

        //Call to DockerService to look for the service port data exposed
        //TODO:Call DockerService with headerServiceValue
    }

    private void throwExceptionIfNotAuthenticated(HttpRequest validationRequest) {
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(validationRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new SecurityException();
            }
        } catch (IOException | InterruptedException e) {
            throw new SecurityException(e);
        }
    }

    private String readHeaderToken(String incomingText) {
        final StringBuilder result = new StringBuilder("");

        Arrays.stream(incomingText.split(StringUtils.LF))
                .filter(s -> s.contains(context.getProperty("header.cookie.get")))
                .findFirst()
                .map(s -> s.substring(s.indexOf(token())+token().length(), s.indexOf(token())+token().length()+randomLength()))
                .ifPresent(result::append);

        return result.toString();
    }

    private HttpRequest buildValidationRequest(String headerTokenValue) {
        return HttpRequest.newBuilder().uri(
                URI.create(context.getProperty("ip.localhost") + context.getProperty("auth.port.internal") +
                        context.getProperty("auth.verify.query") + headerTokenValue)).GET().build();
    }

    private String token() {
        return context.getProperty("header.token") + "=";
    }

    private int randomLength() {
        return Integer.parseInt(context.getProperty("random.length"));
    }
}
