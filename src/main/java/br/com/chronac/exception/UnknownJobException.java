package br.com.chronac.exception;

public class UnknownJobException extends RuntimeException {

    public UnknownJobException(String message) {
        super(message);
    }
}
