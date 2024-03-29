package com.amp.crm.exception;

import javax.xml.ws.WebFault;

@WebFault(targetNamespace = "http://exception.crm.com")
public class ObjectDuplicationException extends CrmRuntimeException {

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
