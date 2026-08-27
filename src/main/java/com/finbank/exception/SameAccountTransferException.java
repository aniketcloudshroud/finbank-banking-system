package com.finbank.exception;

public class SameAccountTransferException extends RuntimeException{

    public SameAccountTransferException(String message) {
        super(message);
    }
}
