package de.naivetardis.service.auth.component;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

public class VerifyHandler extends BasicHandler {
    public VerifyHandler() {
        super();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (isTokenOnMemory(exchange)) {
            exchange.sendResponseHeaders(200, 0);
        } else {
            exchange.sendResponseHeaders(418, 0);
        }
        exchange.close();
    }

    private boolean isTokenOnMemory(HttpExchange exchange) {
        Map<String, String> queryParams = queryToMap(exchange.getRequestURI().getQuery());
        if (queryParams != null) {
            return security().containsKey(queryParams.getOrDefault("id", ""));
        }
        return false;
    }


}
