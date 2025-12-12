package org.fungover.zipp.exception;

/**
 * Exception handling, 401 unauthorized. If API key is invalid.
 **/

public class InvalidApiKeyException extends RuntimeException {

    public InvalidApiKeyException(String message) {
        super(message);
    }

}
