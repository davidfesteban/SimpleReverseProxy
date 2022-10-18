package de.naivetardis.service.proxy;

import de.naivetardis.service.proxy.component.ServiceDataCallback;

/**
 * Layer for accessing the ProxyManager and make it more usable preventing some bad usages.
 * Yes, we can remove it but, it is cooler being a builder.
 */

public class ProxyServiceBuilder {
    private final int port;
    private ServiceDataCallback serviceDataCallback;

    private ProxyServiceBuilder(int port) {
        this.port = port;
        this.serviceDataCallback = null;
    }

    public static ProxyServiceBuilder create(int port) {
        return new ProxyServiceBuilder(port);
    }

    public ProxyServiceBuilder addServiceDataCallback(ServiceDataCallback serviceDataCallback) {
        this.serviceDataCallback = serviceDataCallback;
        return this;
    }

    public ProxyService buildStart() {
        return ProxyService.create(port, serviceDataCallback).startNow();
    }

}
