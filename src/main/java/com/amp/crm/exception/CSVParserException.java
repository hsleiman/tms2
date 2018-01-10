package com.amp.crm.exception;

import javax.xml.ws.WebFault;

@WebFault(targetNamespace = "http://exception.crm.com")
public class CSVParserException extends CrmRuntimeException {

    public CSVParserException() {
    }

    public CSVParserException(String message) {
        super(message);
    }

    public CSVParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public CSVParserException(Throwable cause) {
        super(cause);
    }
    
}
