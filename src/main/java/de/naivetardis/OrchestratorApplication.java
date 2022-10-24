package de.naivetardis;

import de.naivetardis.service.auth.AuthService;
import de.naivetardis.service.proxy.ProxyService;

public class OrchestratorApplication {

    public static void main(String[] args) {
        System.out.println("Here");
        new AuthService().start();
        System.out.println("Here2");
        new ProxyService().start();
        System.out.println("Running");
    }
}
