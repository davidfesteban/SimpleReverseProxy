package de.naivetardis.service.proxy.component;

import de.naivetardis.service.utils.PropertiesContext;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

@Slf4j
public class ClientHandler extends Thread {
    private final Socket client;
    private final Properties context;

    public ClientHandler(Socket client) {
        super();
        this.client = client;
        this.context = PropertiesContext.getInstance().getContext();
    }

    @Override
    public void run() {
        log.info("Client accepted {}", client.getInetAddress());
        try (client;
             final InputStream streamFromClient = client.getInputStream();
             final OutputStream streamToClient = client.getOutputStream()) {

            //Sniffer starts reading
            Sniffer sniffer = new Sniffer(streamFromClient);
            String incomingText = sniffer.read();
            String headerTokenValue = readHeaderToken(incomingText);

            //Validate client is authenticated
            String headerServiceValue = getServiceValueFromRequest(buildValidationRequest(headerTokenValue));

            //Call to DockerService to look for the service port data exposed
            //TODO:Call DockerService with headerServiceValue
            log.info("The service to route is {}", headerServiceValue);

            //Create Pipes and inject previous read data
            try (final Socket service = new Socket(context.getProperty("service.ip"), Integer.parseInt(context.getProperty("service.port")));
                 final InputStream streamFromServer = service.getInputStream();
                 final OutputStream streamToServer = service.getOutputStream()) {

                //Pipe with stream of sniffer
                Pipe insidePipe = new Pipe(streamFromClient, streamToServer, sniffer.getStoredInputStream()).startNow();
                Pipe outsidePipe = new Pipe(streamFromServer, streamToClient).startNow();

                //Wait to finish
                insidePipe.join();
                outsidePipe.join();
            }


        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            log.info("Client finalized {}", client.getInetAddress());
        }
    }

    private String getServiceValueFromRequest(HttpRequest validationRequest) {
        String result = "";
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(validationRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response != null && response.statusCode() == 200) {
            result = response.headers().firstValue(context.getProperty("header.service")).get();
        }
        return result;
    }

    private String readHeaderToken(String incomingText) {
        int lastIndexTokenHeader = incomingText.lastIndexOf(context.getProperty("header.token") + ": ");
        StringBuilder sb = new StringBuilder("");

        for (int i = lastIndexTokenHeader; i < incomingText.length() && incomingText.charAt(i) != ' '; ++i) {
            sb.append(incomingText.charAt(i));
        }

        return sb.toString();
    }

    private HttpRequest buildValidationRequest(String headerTokenValue) {
        return HttpRequest.newBuilder().uri(
                URI.create(context.getProperty("ip.localhost") + context.getProperty("auth.port.internal") +
                        context.getProperty("auth.verify") + headerTokenValue)).GET().build();
    }

    private HttpRequest buildServiceRequest(String serviceName) {
        return HttpRequest.newBuilder().uri(
                URI.create(context.getProperty("ip.localhost") + context.getProperty("auth.port.internal") +
                        context.getProperty("auth.verify") + serviceName)).GET().build();
    }


}
