package de.naivetardis.service.auth;

import com.sun.net.httpserver.HttpServer;
import de.naivetardis.service.auth.component.AuthHandler;
import de.naivetardis.service.auth.component.TrafficHandler;
import de.naivetardis.service.auth.component.VerifyHandler;
import de.naivetardis.service.utils.PropertiesContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

//@Slf4j
public class AuthService extends Thread {
    private final Properties context;

    public AuthService() {
        super(AuthService.class.getName());
        this.context = PropertiesContext.getInstance().getContext();
    }

    @Override
    public void run() {
        try {
            startInternalServer();
            startExternalServer();
        } catch (IOException e) {
            //log.error("Cannot run AuthService, reason: {}", e.getMessage());
        }
    }

    private void startInternalServer() throws IOException {
        HttpServer serverVerify = HttpServer.create(new InetSocketAddress(Integer.parseInt(context.getProperty("auth.port.internal"))), 0);
        serverVerify.createContext(context.getProperty("auth.verify.path"), new VerifyHandler());
        serverVerify.start();
        //log.info("Internal Auth Server started");
    }

    private void startExternalServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(context.getProperty("auth.port.external"))), 0);
        server.createContext(context.getProperty("auth.traffic"), new TrafficHandler());
        server.createContext(context.getProperty("auth.auth"), new AuthHandler());
        server.start();
        //log.info("External Auth Server started");
    }

}
