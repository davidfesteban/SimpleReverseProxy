package de.naivetardis.component;

import de.naivetardis.ProxyManager;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Slf4j
public class ClientHandler extends Thread {

    private final Socket client;
    private final Socket service;

    public ClientHandler(Socket client, Socket service) {
        super();
        this.client = client;
        this.service = service;
    }

    @Override
    public void run() {
        super.run();
        try (client;
             service;
             final InputStream streamFromClient = client.getInputStream();
             final OutputStream streamToClient = client.getOutputStream();
             final InputStream streamFromServer = service.getInputStream();
             final OutputStream streamToServer = service.getOutputStream()) {


            Pipe insidePipe = new Pipe(streamFromClient, streamToServer).startNow();
            Pipe outsidePipe = new Pipe(streamFromServer, streamToClient).startNow();

            //Wait to finish
            insidePipe.join();
            outsidePipe.join();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public ClientHandler startNow() {
        start();
        return this;
    }
}
