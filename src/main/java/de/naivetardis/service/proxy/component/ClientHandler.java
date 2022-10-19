package de.naivetardis.service.proxy.component;

import de.naivetardis.service.utils.PropertiesContext;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.function.Predicate;

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
             service;
             final InputStream streamFromClient = client.getInputStream();
             final OutputStream streamToClient = client.getOutputStream();
             final InputStream streamFromServer = service.getInputStream();
             final OutputStream streamToServer = service.getOutputStream()) {

            Sniffer sniffer = new Sniffer(streamFromClient);
            sniffer.applyFilter(new Predicate<String>() {
                @Override
                public boolean test(String s) {
                    s.indexOf()
                    return;
                }
            });

            //Pipe with stream of sniffer
            ...
            Pipe insidePipe = new Pipe(streamFromClient, streamToServer).startNow();
            Pipe outsidePipe = new Pipe(streamFromServer, streamToClient).startNow();

            //Wait to finish
            insidePipe.join();
            outsidePipe.join();
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            log.info("Client finalized {}", client.getInetAddress());
        }
    }

    private void obtainRoutedServiceFromToken(InputStream streamFromClient) {

    }


}
