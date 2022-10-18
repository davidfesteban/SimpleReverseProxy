package de.naivetardis.service.auth;

import com.sun.net.httpserver.HttpServer;
import de.naivetardis.service.auth.component.CheckAuthHandler;
import de.naivetardis.service.auth.component.RouteHandler;
import de.naivetardis.service.auth.component.VerifyHandler;
import de.naivetardis.service.utils.PropertiesContext;
import de.naivetardis.service.utils.RandomString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Supplier;

@Slf4j
public class AuthService extends Thread {
    private final Map<String, String> authenticatedUsers;
    private final RandomString randomString;
    private final Properties context;

    public AuthService() {
        this.authenticatedUsers = new HashMap<>();
        this.randomString = new RandomString(10);
        this.context = PropertiesContext.getInstance().getContext();
    }

    @Override
    public void run() {
        try {
            startInternalServer();
            startExternalServer();
        } catch (IOException e) {
            log.error("Cannot run AuthService, reason: {}", e.getMessage());
        }
    }

    private void startInternalServer() throws IOException {
        HttpServer serverVerify = HttpServer.create(new InetSocketAddress(Integer.parseInt(context.getProperty("auth.port.internal"))), 0);
        serverVerify.createContext(context.getProperty("auth.verify"), new VerifyHandler(() -> authenticatedUsers));
        serverVerify.start();
        log.info("Internal Auth Server started");
    }

    private void startExternalServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(context.getProperty("auth.port.external"))), 0);
        server.createContext(context.getProperty("auth.traffic"), new CheckAuthHandler(() -> authenticatedUsers));
        server.createContext(context.getProperty("auth.auth"), new RouteHandler(() -> authenticatedUsers));
        server.start();
        log.info("External Auth Server started");
    }


}