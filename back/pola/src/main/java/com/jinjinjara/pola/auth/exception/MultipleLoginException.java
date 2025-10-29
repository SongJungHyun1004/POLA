package com.jinjinjara.pola.auth.exception;

public class MultipleLoginException extends RuntimeException{
    public MultipleLoginException(String msg){ super(msg);}
}
