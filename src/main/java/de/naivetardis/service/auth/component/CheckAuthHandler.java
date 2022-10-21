package de.naivetardis.service.auth.component;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class CheckAuthHandler extends BasicHandler {

    public CheckAuthHandler() {
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
        Headers headers = exchange.getRequestHeaders();
        if (headers.get(context("header.cookie.get")) != null && !headers.get(context("header.cookie.get")).isEmpty()) {
            return security().containsKey(headers.get(context("header.cookie.get")).get(0).replace(context("header.token")+"=",""));
        }
        return false;
    }
}
