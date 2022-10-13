package de.naivetardis;

import de.naivetardis.component.FilterInterface;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Layer for accessing the ProxyManager and make it more usable preventing some bad usages.
 * Yes, we can remove it but, it is cooler being a builder.
 */

public class ExposedLauncherBuilder {
    private final Set<FilterInterface> filters;
    private final int port;

    private ExposedLauncherBuilder(int port) {
        this.port = port;
        filters = new TreeSet<>();
    }

    public static void main(String[] args) {
        ProxyManager.createByDefault(80).startNow();
    }

    public static ExposedLauncherBuilder create(int port) {
        return new ExposedLauncherBuilder(port);
    }

    public ExposedLauncherBuilder addFilter(FilterInterface filterInterface) {
        filters.add(filterInterface);
        return this;
    }

    public ProxyManager buildStart() {
        return ProxyManager.createWithFilters(port, filters).startNow();
    }

}
