package de.naivetardis.filter;

import java.net.Socket;

public interface FilterInterface<T> extends Comparable<T> {
    void filter(Socket requestFromClient) throws FilterException;
}
