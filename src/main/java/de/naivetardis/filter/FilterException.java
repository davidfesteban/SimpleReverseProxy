package de.naivetardis.filter;

public class FilterException extends Exception {
    public FilterException() {
        super("(╯°□°）╯︵ ┻━┻");
    }

    public FilterException(Throwable e) {
        super("(╯°□°）╯︵ ┻━┻ :" + e.getMessage());
    }

    public FilterException(String s) {
        super("(╯°□°）╯︵ ┻━┻ :" + s);
    }
}
