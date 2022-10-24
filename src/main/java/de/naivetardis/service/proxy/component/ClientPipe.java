package de.naivetardis.service.proxy.component;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import de.naivetardis.service.utils.PropertiesContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class ClientPipe extends Thread {

    private final InputStream inputStream;
    private final Properties context;
    private OutputStream outputStream;
    private Socket serviceSocket;
    private boolean validated;

    public ClientPipe(InputStream inputStream, OutputStream outputStream) {
        super();
        this.inputStream = Objects.requireNonNull(inputStream);
        this.outputStream = outputStream;
        this.serviceSocket = null;
        this.context = PropertiesContext.getInstance().getContext();
        validated = false;
    }

    public ClientPipe(InputStream streamFromClient) {
        this(streamFromClient, null);
    }

    @Override
    public void run() {
        super.run();
        int bytesRead;
        byte[] request = new byte[1000000];
        try {
            while ((bytesRead = inputStream.read(request)) != -1) {
                if (!validated) {
                    checkAuth(request);
                }

                if (outputStream == null) {
                    createServiceSocket(request);
                    try {
                        outputStream = serviceSocket.getOutputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }


                outputStream.write(request, 0, bytesRead);
                outputStream.flush();
                request = new byte[1000000];

            }
        } catch (Exception e) {
            if (e instanceof SecurityException) {
                throw new SecurityException(e);
            } else {
                log.error(e.getMessage());
            }
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ClientPipe startNow() {
        start();
        return this;
    }

    public InputStream getServiceInputStream() {
        try {
            while (serviceSocket == null) {
                Thread.sleep(500);
            }
            return serviceSocket.getInputStream();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void createServiceSocket(byte[] request) {
        final String incomingText = new String(request, StandardCharsets.UTF_8);

        Arrays.stream(incomingText.split(StringUtils.LF))
                .filter(s -> s.contains("Host"))
                .findFirst()
                .map(s -> s.replace("Host: ", "").split(".")[0])
                .ifPresent(s -> {
                    DockerClient dockerClient = DockerClientBuilder.getInstance(context.getProperty("docker.api.url")).build();
                    dockerClient.listContainersCmd()
                            .withShowAll(true)
                            .withShowSize(true)
                            .withStatusFilter(List.of("running"))
                            .exec()
                            .stream()
                            .filter(container -> container.getNames()[0].contains(s))
                            .findFirst()
                            .ifPresent(container -> {
                                try {
                                    serviceSocket = new Socket(context.getProperty("service.ip"), container.getPorts()[0].getPublicPort());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                });
    }

    private void checkAuth(byte[] request) {
        //Sniffer starts reading
        String incomingText = new String(request, StandardCharsets.UTF_8);
        String headerTokenValue = readHeaderToken(incomingText);
        log.info(headerTokenValue);

        //Validate client is authenticated
        throwExceptionIfNotAuthenticated(buildValidationRequest(headerTokenValue));
        validated = true;
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
                .map(s -> s.substring(s.indexOf(token()) + token().length(), s.indexOf(token()) + token().length() + randomLength()))
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
