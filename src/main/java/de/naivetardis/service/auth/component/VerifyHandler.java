package de.naivetardis.service.auth.component;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class VerifyHandler extends BasicHandler {
    public VerifyHandler(Supplier<Map<String, String>> supplier) {
        super(supplier);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String service = getServiceFromToken(exchange);
        Headers responseHeaders = exchange.getResponseHeaders();
        if (!StringUtils.isEmpty(service)) {
            responseHeaders.set(context("header.service"), service);
            exchange.sendResponseHeaders(200, 0);
        }
        exchange.sendResponseHeaders(418, 0);
    }

    private String getServiceFromToken(HttpExchange exchange) {
        Map<String, String> queryParams = queryToMap(exchange.getRequestURI().getQuery());
        if (queryParams != null) {
            return security().getOrDefault(queryParams.getOrDefault("id", ""), "");
        }
        return null;

    }


}
