package de.naivetardis.service.auth.component;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class CheckAuthHandler extends BasicHandler {

    public CheckAuthHandler(Supplier<Map<String, String>> supplier) {
        super(supplier);
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
        Headers headers = exchange.getRequestHeaders();
        if (headers.get(context("header.token")) != null && !headers.get(context("header.token")).isEmpty()) {
            return security().containsKey(headers.get(context("header.token")).get(0));
        }
        return false;
    }
}
