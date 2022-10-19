package de.naivetardis;

import de.naivetardis.service.auth.AuthService;
import de.naivetardis.service.proxy.ProxyService;

public class OrchestratorApplication {

    public static void main(String[] args) {
        new AuthService().start();
        new ProxyService().start();
    }
}
