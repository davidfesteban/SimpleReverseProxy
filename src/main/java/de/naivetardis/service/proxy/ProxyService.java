package de.naivetardis.service.proxy;

import de.naivetardis.service.proxy.component.ClientHandler;
import de.naivetardis.service.proxy.component.ServiceDataCallback;
import de.naivetardis.service.utils.PropertiesContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpClient;
import java.util.Optional;
import java.util.Properties;

@Slf4j
public class ProxyService extends Thread {
    private final Properties context;
    private int port;

    public ProxyService() {
        super(ProxyService.class.getName());
        this.context = PropertiesContext.getInstance().getContext();
        this.port = Integer.parseInt(context.getProperty("proxy.port.external"));
    }

    @Override
    public void run() {
        //Avoid server connectivity issues
        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                //Keep alive for each client
                while (true) {
                    new ClientHandler(serverSocket.accept()).start();
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }


}
