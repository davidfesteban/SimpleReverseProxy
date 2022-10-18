package de.naivetardis.service.proxy;

import de.naivetardis.service.proxy.component.ClientHandler;
import de.naivetardis.service.proxy.component.ServiceDataCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;

@Slf4j
public class ProxyService extends Thread {

    private static final String DEFAULT_SERVICE_IP = "127.0.0.1";
    private static final int DEFAULT_SERVICE_PORT = 1234;

    private final Optional<ServiceDataCallback> dataCallback;
    private int port;

    private Pair<String, Integer> defaultServiceData;

    private ProxyService(int port) {
        this(port, null);
    }

    private ProxyService(int port, ServiceDataCallback serviceDataCallback) {
        super(ProxyService.class.getName());
        this.port = port;
        this.dataCallback = Optional.ofNullable(serviceDataCallback);
        this.defaultServiceData = Pair.of(DEFAULT_SERVICE_IP, DEFAULT_SERVICE_PORT);
    }

    static ProxyService createByDefault(int port) {
        return new ProxyService(port);
    }

    static ProxyService create(int port, ServiceDataCallback serviceDataCallback) {
        return new ProxyService(port, serviceDataCallback);
    }

    @Override
    public void run() {

        //Avoid server connectivity issues
        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {

                //Keep alive for each client
                while (true) {
                    try {
                        socketHandler(serverSocket.accept());
                    } catch (IOException e) {
                        log.info(e.getMessage());
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    public ProxyService startNow() {
        start();
        return this;
    }

    private void socketHandler(Socket client) {
        //Delegate to a thread
        new Thread(() -> {
            try (client) {
                log.info("Client accepted {}", client.getInetAddress());

                //Tries to retrieve data of to which service we need to route/proxy
                //dataCallback.ifPresent(serviceDataCallback -> defaultServiceData = serviceDataCallback.retrieveHostPort());


                //It will proxy all the calls to the service
                new ClientHandler(client, new Socket(defaultServiceData.getLeft(), defaultServiceData.getRight())).run();
                log.info("Client finalized {}", client.getInetAddress());
            } catch (IOException e) {
                log.info(e.getMessage());
            }
        }).start();

    }
}