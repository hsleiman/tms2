package com.amp.crm.exception;

import javax.xml.ws.WebFault;

/**
 *
 * @author chris
 */
@WebFault(targetNamespace = "http://exception.sti.objectbrains.com")
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
