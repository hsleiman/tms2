package com.objectbrains.sti.exception;

import javax.xml.ws.WebFault;

/**
 *
 * @author chris
 */
@WebFault(targetNamespace = "http://exception.sti.objectbrains.com")
public class ObjectDuplicationException extends StiRuntimeException {

    public ObjectDuplicationException() {
    }

    public ObjectDuplicationException(String message) {
        super(message);
    }

    public ObjectDuplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectDuplicationException(Throwable cause) {
        super(cause);
    }
    
}
