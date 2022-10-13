package de.naivetardis;

import de.naivetardis.component.ServiceDataCallback;
import de.naivetardis.filter.FilterInterface;

import java.util.Set;
import java.util.TreeSet;

/**
 * Layer for accessing the ProxyManager and make it more usable preventing some bad usages.
 * Yes, we can remove it but, it is cooler being a builder.
 */

public class ExposedLauncherBuilder {
    private final Set<FilterInterface> filters;
    private final int port;
    private ServiceDataCallback serviceDataCallback;

    private ExposedLauncherBuilder(int port) {
        this.port = port;
        this.filters = new TreeSet<>();
        this.serviceDataCallback = null;
    }

    public static void main(String[] args) {
        ProxyManager.createByDefault(8080).startNow();
    }

    public static ExposedLauncherBuilder create(int port) {
        return new ExposedLauncherBuilder(port);
    }

    public ExposedLauncherBuilder addFilter(FilterInterface filterInterface) {
        filters.add(filterInterface);
        return this;
    }

    public ExposedLauncherBuilder addServiceDataCallback(ServiceDataCallback serviceDataCallback) {
        this.serviceDataCallback = serviceDataCallback;
        return this;
    }

    public ProxyManager buildStart() {
        return ProxyManager.create(port, filters, serviceDataCallback).startNow();
    }

}
