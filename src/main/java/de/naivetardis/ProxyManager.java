package de.naivetardis;

import de.naivetardis.component.ClientHandler;
import de.naivetardis.component.FilterInterface;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.TreeSet;

@Slf4j
public class ProxyManager extends Thread {

    private final Set<FilterInterface> filterInterfaces;
    private int port;

    private ProxyManager(int port) {
        this(port, new TreeSet<>());
    }

    private ProxyManager(int port, Set<FilterInterface> filterInterfaces) {
        super(ProxyManager.class.getName());
        this.port = port;
        this.filterInterfaces = filterInterfaces;
    }

    static ProxyManager createByDefault(int port) {
        return new ProxyManager(port);
    }

    static ProxyManager createWithFilters(int port, Set<FilterInterface> filterInterfaces) {
        return new ProxyManager(port, filterInterfaces);
    }

    @Override
    public void run() {
        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                new Thread(() -> {
                    while (true) {
                        try (Socket client = serverSocket.accept()) {
                            filterInterfaces.forEach(filterInterface -> filterInterface.filter(client));
                            route(client, "127.0.0.1", 1234);
                        } catch (IOException e) {
                            log.info(e.getMessage());
                        }
                    }
                }).start();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    public ProxyManager startNow() {
        start();
        return this;
    }

    private void route(Socket socketRequestFromClient, String host, int remotePort) throws IOException {
        new ClientHandler(socketRequestFromClient, new Socket(host, remotePort)).start();
        log.info("Client accepted");
    }
}