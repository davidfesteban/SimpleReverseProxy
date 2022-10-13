package de.naivetardis.component;

import org.apache.commons.lang3.tuple.Pair;

public interface ServiceDataCallback {
    Pair<String, Integer> retrieveHostPort();
}
