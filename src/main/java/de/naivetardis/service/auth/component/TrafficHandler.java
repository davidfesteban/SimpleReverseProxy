package de.naivetardis.service.auth.component;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class TrafficHandler extends BasicHandler {

    public TrafficHandler() {
        super();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (isAuthenticated(t)) {
            redirect(t);
        } else {
            authView(t);
        }
    }

    private boolean isAuthenticated(HttpExchange exchange) {
        final Wrapper<Boolean> result = new Wrapper<>(false);
        Headers headers = exchange.getRequestHeaders();

        if (headers.get(context("header.cookie.get")) != null && !headers.get(context("header.cookie.get")).isEmpty()) {
            headers.get(context("header.cookie.get")).stream()
                    .filter(s -> s.contains(context("header.token")))
                    .findFirst()
                    .ifPresent(s -> result.setValue(security().containsKey(s.replace(context("header.token") + "=", ""))));
        }

        return result.getValue();
    }

    protected class Wrapper<T> {
        T element;

        public Wrapper(T element) {
            this.element = element;
        }

        public T getValue() {
            return element;
        }

        public void setValue(T element) {
            this.element = element;
        }
    }
}
