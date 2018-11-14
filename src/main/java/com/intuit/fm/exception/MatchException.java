package com.intuit.fm.exception;

public class MatchException extends RuntimeException {

    public MatchException() {
        super();
    }

    public MatchException(String message) {
        super(message);
    }

    public MatchException(Throwable t) {
        super(t);
    }

    public MatchException(String message, Throwable t) {
        super(message, t);
    }
}
