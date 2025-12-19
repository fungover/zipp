package org.fungover.zipp.exception;

/**
 * Exception handling, 401 unauthorized. If API key is invalid. Added
 * serialVersionUID to exception class to satisfy PMD
 **/

public class InvalidApiKeyException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidApiKeyException(String message) {
        super(message);
    }

}
