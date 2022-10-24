package de.naivetardis.service.proxy.component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//@Slf4j
public class ClientHandler extends Thread {
    private final Socket client;

    public ClientHandler(Socket client) {
        super();
        this.client = client;
    }

    @Override
    public void run() {
        //log.info("Client accepted {}", client.getInetAddress());
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
            //log.error(e.getMessage());
        } finally {
            //log.info("Client finalized {}", client.getInetAddress());
            this.interrupt();
        }
    }
}
