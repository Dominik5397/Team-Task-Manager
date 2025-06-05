package org.example.exception;

/**
 * Wyjątek rzucany gdy naruszono reguły biznesowe aplikacji.
 */
public class BusinessLogicException extends RuntimeException {
    
    private final String errorCode;
    private final Object[] parameters;

    public BusinessLogicException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.parameters = new Object[0];
    }

    public BusinessLogicException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.parameters = new Object[0];
    }

    public BusinessLogicException(String errorCode, String message, Object... parameters) {
        super(message);
        this.errorCode = errorCode;
        this.parameters = parameters;
    }

    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.parameters = new Object[0];
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getParameters() {
        return parameters;
    }
} 