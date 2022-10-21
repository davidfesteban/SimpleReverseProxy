package de.naivetardis.service.proxy.component;

import de.naivetardis.service.utils.PropertiesContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
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
             final InputStream streamFromClient = client.getInputStream();
             final OutputStream streamToClient = client.getOutputStream()) {

            //Create Pipes and inject previous read data
            try (final Socket service = new Socket(context.getProperty("service.ip"), Integer.parseInt(context.getProperty("service.port")));
                 final InputStream streamFromServer = service.getInputStream();
                 final OutputStream streamToServer = service.getOutputStream()) {

                //Pipe with stream of sniffer
                ClientPipe insidePipe = new ClientPipe(streamFromClient, streamToServer).startNow();
                Pipe outsidePipe = new Pipe(streamFromServer, streamToClient).startNow();

                //Wait to finish
                insidePipe.join();
                outsidePipe.join();
            }catch (Exception e) {
                log.error(e.getMessage());
            }


        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            log.info("Client finalized {}", client.getInetAddress());
        }
    }




}
