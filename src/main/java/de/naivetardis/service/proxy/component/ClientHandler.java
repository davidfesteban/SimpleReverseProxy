package de.naivetardis.service.proxy.component;

import de.naivetardis.service.utils.PropertiesContext;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
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

                //Pipe with stream of sniffer
                ClientPipe insidePipe = new ClientPipe(streamFromClient).startNow();
                ServerPipe outsideServerPipe = new ServerPipe(insidePipe.getServiceInputStream(), streamToClient).startNow();

                //Wait to finish
                insidePipe.join();
                outsideServerPipe.join();



        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            log.info("Client finalized {}", client.getInetAddress());
        }
    }




}
