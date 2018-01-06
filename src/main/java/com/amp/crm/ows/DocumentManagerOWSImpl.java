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
import com.objectbrains.dms.iws.DocumentServiceIWS;
import com.objectbrains.dms.iws.DocumentStoreException_Exception;
import com.objectbrains.dms.iws.DocumentType;
import com.objectbrains.dms.iws.EmailAttachment;
import com.objectbrains.dms.iws.EmailInfo;
import com.objectbrains.dms.iws.EmailQueueIWS;
import com.objectbrains.dms.iws.GeneratedDocumentManagementIWS;
import com.objectbrains.dms.iws.IOException_Exception;
import com.objectbrains.dms.iws.JcrFile;
import com.objectbrains.dms.iws.SendGridException_Exception;
import com.objectbrains.dms.iws.StoredDocument;
import com.objectbrains.dms.iws.StoredDocumentManagementIWS;
import com.objectbrains.dms.iws.StoredDocumentResponse;
import com.objectbrains.dms.iws.UtilityIWS;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author raine.cabal
 */
@Service
public class DocumentManagerOWSImpl implements DocumentManagerOWS {
        
    @Autowired(required = false)
    private StoredDocumentManagementIWS storedDocService;
    
    @Autowired(required = false)
    private GeneratedDocumentManagementIWS generatedDocument;
    
    @Autowired(required = false)
    private DocumentServiceIWS documentService;
    
    @Autowired(required = false)
    private EmailQueueIWS emailQueueIWS;
    
    @Autowired(required = false)
    private UtilityIWS dmsUtility;
    
    // =====================================================================================
    // STORED DOCUMENT MANAGEMENT SERVICE
    // =====================================================================================
    
    @Override
    public StoredDocumentResponse addStoredDocument(StoredDocument storedDocument) {
        return storedDocService.addStoredDocument(storedDocument);
    }
    
    @Override
    public StoredDocumentResponse addDocument(Document document, DocumentType documentType, String fileContent, int jobType, DocumentFlow docFlow, List<AttachedDocument> attachedDocuments) {
        return storedDocService.addDocument(document, documentType, fileContent, jobType, docFlow, attachedDocuments);
    }
    
    @Override
    public DocumentInformation getStoredDocByDocumentId(String documentId) throws DmsException, DocumentNotFoundException_Exception {
        return storedDocService.getStoredDocByDocumentId(documentId);
    }
    
    @Override
    public DocumentInformation getStoredDocByPk(Long documentPk) throws DmsException, DocumentNotFoundException_Exception {
        return storedDocService.getStoredDocByPk(documentPk);
    }
    
    @Override
    public StoredDocumentResponse addAttachmentToStoredDoc(String documentId, AttachedDocument attachedDocument) {
        return storedDocService.addAttachmentToStoredDoc(documentId, attachedDocument);
    }
    
    @Override
    public JcrFile getFileByDocId(String documentId) throws DmsException {
            return storedDocService.getFileByDocId(documentId);
    }
    
    @Override
    public List<DocumentInformation> findStoredDocs(DocumentSearchFilter filter) {
        return storedDocService.findStoredDocs(filter);
    }
    
    //#840
    @Override
    public AttachedDocument getFileContentByAttachmentId(long attachmentpk) throws DocumentNotFoundException_Exception{
        return storedDocService.getFileContentByAttachmentId(attachmentpk);
    }
    
    @Override
    public List<AttachmentMetadata> getAllAttachmentsMetadataForSotredDoc(String docId) throws DmsException, DocumentNotFoundException_Exception, DocumentStoreException_Exception{
        return storedDocService.getAllAttachmentsMetadataForSotredDoc(docId);
    }
    
    @Override
    public int getNumberOfPages(DocumentType docType, byte[] fileContent) throws DmsException{
        return storedDocService.getNumberOfPages(docType, fileContent);
    }
    
    @Override
    public void removeStoredDocument(long documentPk) throws DmsException, DocumentNotFoundException_Exception {
        storedDocService.removeStoredDoc(documentPk);
    }
    
    @Override
    public void moveStoredDocument(long documentPk, long loanPk, long borrowerPk) throws DocumentNotFoundException_Exception {
        storedDocService.moveStoredDoc(documentPk, loanPk, borrowerPk);
    }
    // =====================================================================================
    // GENERATED DOCUMENT MANAGEMENT SERVICE
    // =====================================================================================
    
    @Override
    public DocumentInformation getGeneratedDocByDocumentId(String documentId) throws DmsException, DocumentNotFoundException_Exception {
        return generatedDocument.getGeneratedDocByDocumentId(documentId);
    }
    
    @Override
    public Long createDocument(Long loanPk, DocumentGenerationInfo docGenerationInfo, AdditionalConditions addlConditions) throws DmsException, CannotConvertDocumentToHTML_Exception, CannotConvertDocumentToPDF_Exception, DocumentNotFoundException_Exception {
        return documentService.createDocument(loanPk, docGenerationInfo, addlConditions);
    }
        
    // =====================================================================================
    // EMAIL SERVICE
    // =====================================================================================
    @Override
    public String sendToEmailQueue(EmailInfo emailInfo, List<EmailAttachment> attachments, String userId, DocumentPojo document) {
        return emailQueueIWS.sendToEmailQueue(emailInfo, attachments, userId, document);
    }
    
    @Override
    public String sendNotificationEmail(EmailInfo emailInfo) throws SendGridException_Exception, IOException_Exception {
        return emailQueueIWS.sendNotificationEmail(emailInfo);
    }

    //#delete all dms docs and logs for loan
    // =====================================================================================
    // UTILITY SERVICE
    // =====================================================================================
    @Override
    public void deleteAllDocsAndLogsForLoan(long loanPk) throws DmsException, DocumentStoreException_Exception{
        dmsUtility.deleteAllDocsAndLogsForLoan(loanPk);
    }

    @Override
    public List<DocumentInformation> getAllStoredDocsByLoanPk(long loanPk) throws DmsException{
        return storedDocService.getAllStoredDocsByLoanId(loanPk);
    }

    @Override
    public StoredDocumentResponse getFileContentByDocumentId(String documentId) throws DmsException {
        return storedDocService.getFileContentByDocId(documentId);
    }

}
