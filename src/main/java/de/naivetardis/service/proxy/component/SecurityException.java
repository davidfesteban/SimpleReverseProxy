package de.naivetardis.service.proxy.component;

public class SecurityException extends RuntimeException{
    public SecurityException(Exception e) {
        super(e);
    }

    public SecurityException() {
        super("Error in auth");
    }
}
