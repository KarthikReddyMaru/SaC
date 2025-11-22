package com.sac.util.exception;

public class IllegalMoveException extends RuntimeException {
    public IllegalMoveException() {
        super();
    }
    public IllegalMoveException(String message) {
        super(message);
    }
}
