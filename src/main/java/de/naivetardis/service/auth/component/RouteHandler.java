package de.naivetardis.service.auth.component;

import com.sun.net.httpserver.HttpExchange;
import de.naivetardis.service.utils.RandomString;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class RouteHandler extends BasicHandler {
    private final RandomString randomString;

    public RouteHandler(Supplier<Map<String, String>> supplier) {
        super(supplier);
        randomString = new RandomString(10);
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (isAuthSuccessfull(t)) {
            addToken(t);
            redirect(t);
        } else {
            authView(t);
        }
    }

    private void addToken(HttpExchange exchange) {
        String token = randomString.nextString();
        security().put(token, exchange.getRequestURI().getHost());
        exchange.getResponseHeaders().add(context("header.token"), token);
    }

    private boolean isAuthSuccessfull(HttpExchange exchange) {
        Map<String, String> queryParams = queryToMap(exchange.getRequestURI().getQuery());

        if (queryParams != null) {
            return queryParams.getOrDefault("email", "").equalsIgnoreCase(context("security.email"))
                    && queryParams.getOrDefault("pswd", "").equalsIgnoreCase("security.pswd");
        }

        return false;
    }
}
