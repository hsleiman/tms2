/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.ows;

import com.objectbrains.dms.iws.AdditionalConditions;
import com.objectbrains.dms.iws.AttachedDocument;
import com.objectbrains.dms.iws.AttachmentMetadata;
import com.objectbrains.dms.iws.CannotConvertDocumentToHTML_Exception;
import com.objectbrains.dms.iws.CannotConvertDocumentToPDF_Exception;
import com.objectbrains.dms.iws.DmsException;
import com.objectbrains.dms.iws.Document;
import com.objectbrains.dms.iws.DocumentFlow;
import com.objectbrains.dms.iws.DocumentGenerationInfo;
import com.objectbrains.dms.iws.DocumentInformation;
import com.objectbrains.dms.iws.DocumentNotFoundException_Exception;
import com.objectbrains.dms.iws.DocumentPojo;
import com.objectbrains.dms.iws.DocumentSearchFilter;
import com.objectbrains.dms.iws.DocumentStoreException_Exception;
import com.objectbrains.dms.iws.DocumentType;
import com.objectbrains.dms.iws.EmailAttachment;
import com.objectbrains.dms.iws.EmailInfo;
import com.objectbrains.dms.iws.IOException_Exception;
import com.objectbrains.dms.iws.JcrFile;
import com.objectbrains.dms.iws.SendGridException_Exception;
import com.objectbrains.dms.iws.StoredDocument;
import com.objectbrains.dms.iws.StoredDocumentResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface DocumentManagerOWS {

    // =====================================================================================
    // STORED DOCUMENT MANAGEMENT SERVICE
    // =====================================================================================
    
    public StoredDocumentResponse addStoredDocument(StoredDocument storedDocument);

    public StoredDocumentResponse addDocument(Document document, DocumentType documentType, String fileContent, int jobType, DocumentFlow docFlow, List<AttachedDocument> attachedDocuments);

    public DocumentInformation getStoredDocByDocumentId(String documentId) throws DmsException, DocumentNotFoundException_Exception;

    public DocumentInformation getStoredDocByPk(Long documentPk) throws DmsException, DocumentNotFoundException_Exception;

    public StoredDocumentResponse addAttachmentToStoredDoc(String documentId, AttachedDocument attachedDocument);

    public JcrFile getFileByDocId(String documentId) throws DmsException;
    
    public List<DocumentInformation> findStoredDocs(DocumentSearchFilter filter);
    
    //#840
    public AttachedDocument getFileContentByAttachmentId(long attachmentpk) throws DocumentNotFoundException_Exception;
    
    public List<AttachmentMetadata> getAllAttachmentsMetadataForSotredDoc(String docId) throws DmsException, DocumentNotFoundException_Exception, DocumentStoreException_Exception;
    
    public int getNumberOfPages(DocumentType docType, byte[] fileContent) throws DmsException;
    
    public List<DocumentInformation> getAllStoredDocsByLoanPk(long loanPk)  throws DmsException;
    public StoredDocumentResponse getFileContentByDocumentId(String documentId) throws DmsException;
    
    public void removeStoredDocument(long documentPk) throws DmsException, DocumentNotFoundException_Exception ;
    
    public void moveStoredDocument(long documentPk, long loanPk, long borrowerPk) throws DocumentNotFoundException_Exception ;
   
    
    // =====================================================================================
    // GENERATED DOCUMENT MANAGEMENT SERVICE
    // =====================================================================================
    
    public DocumentInformation getGeneratedDocByDocumentId(String documentId) throws DmsException, DocumentNotFoundException_Exception;

    public Long createDocument(Long loanPk, DocumentGenerationInfo docGenerationInfo, AdditionalConditions addlConditions) throws DmsException, 
            CannotConvertDocumentToHTML_Exception, CannotConvertDocumentToPDF_Exception, DocumentNotFoundException_Exception;

    // =====================================================================================
    // EMAIL SERVICE
    // =====================================================================================
    
    public String sendToEmailQueue(EmailInfo emailInfo, List<EmailAttachment> attachments, String userId, DocumentPojo document);
    
    public String sendNotificationEmail(EmailInfo emailInfo) throws SendGridException_Exception, IOException_Exception;

    //#delete all dms docs and logs for loan
    // =====================================================================================
    // UTILITY SERVICE
    // =====================================================================================
    
    public void deleteAllDocsAndLogsForLoan(long loanPk) throws DmsException, DocumentStoreException_Exception;
}

