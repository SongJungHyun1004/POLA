package com.jinjinjara.pola.auth.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String msg){ super(msg); }
}