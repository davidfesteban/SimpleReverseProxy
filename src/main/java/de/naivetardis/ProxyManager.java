package de.naivetardis;

import de.naivetardis.component.ClientHandler;
import de.naivetardis.component.ServiceDataCallback;
import de.naivetardis.filter.FilterException;
import de.naivetardis.filter.FilterInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Slf4j
public class ProxyManager extends Thread {

    private static final String DEFAULT_SERVICE_IP = "127.0.0.1";
    private static final int DEFAULT_SERVICE_PORT = 1234;

    private final Set<FilterInterface> filterInterfaces;

    private final Optional<ServiceDataCallback> dataCallback;
    private int port;

    private Pair<String, Integer> defaultServiceData;

    private ProxyManager(int port) {
        this(port, new TreeSet<>(), null);
    }

    private ProxyManager(int port, Set<FilterInterface> filterInterfaces, ServiceDataCallback serviceDataCallback) {
        super(ProxyManager.class.getName());
        this.port = port;
        this.filterInterfaces = filterInterfaces;
        this.dataCallback = Optional.ofNullable(serviceDataCallback);
        this.defaultServiceData = Pair.of(DEFAULT_SERVICE_IP, DEFAULT_SERVICE_PORT);
    }

    static ProxyManager createByDefault(int port) {
        return new ProxyManager(port);
    }

    static ProxyManager create(int port, Set<FilterInterface> filterInterfaces, ServiceDataCallback serviceDataCallback) {
        return new ProxyManager(port, filterInterfaces, serviceDataCallback);
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

    public ProxyManager startNow() {
        start();
        return this;
    }

    private void socketHandler(Socket client) {
        //Delegate to a thread
        new Thread(() -> {
            try (client) {
                log.info("Client accepted {}", client.getInetAddress());

                //Applies filters as security or auth.
                for (FilterInterface filterInterface : filterInterfaces) {
                    filterInterface.filter(client);
                }

                //Tries to retrieve data of to which service we need to route/proxy
                dataCallback.ifPresent(serviceDataCallback -> defaultServiceData = serviceDataCallback.retrieveHostPort());

                //It will proxy all the calls to the service
                new ClientHandler(client, new Socket(defaultServiceData.getLeft(), defaultServiceData.getRight())).run();
                log.info("Client finalized {}", client.getInetAddress());
            } catch (IOException | FilterException e) {
                log.info(e.getMessage());
            }
        }).start();

    }
}