package com.microsoft.ocp.latam.worker;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.ocp.latam.command.SendToDeadLetterQueueCmd;
import com.microsoft.ocp.latam.cosmosdb.DocumentClientFactory;
import com.microsoft.ocp.latam.data.BlobCleanerRequest;
import com.microsoft.ocp.latam.data.ProcessedDelete;
import com.microsoft.ocp.latam.util.GsonSingleton;

/**
 * It removes a blob given its URI 
 */
public class QueueTriggerWorker {

    protected void execute(final ExecutionContext context, final String message) {
        context.getLogger().info("Java Queue trigger function processed a message: " + message);

        // build request
        BlobCleanerRequest blobCleanerRequest = GsonSingleton.getInstance().fromJson(message, BlobCleanerRequest.class);

        try {
            // delete blob given blob URI
            StorageCredentials credentials = StorageCredentials.tryParseCredentials(blobCleanerRequest.getConnectionString());
            URI blobURI = new URI(blobCleanerRequest.getAbsolutURI());
            CloudBlockBlob blobToDelete = new CloudBlockBlob(blobURI, credentials);
            blobToDelete.delete();
        } catch (InvalidKeyException e) {
            // move to DLQ
            blobCleanerRequest.setExceptionMessage(e.getClass().getName()+"-"+e.getMessage());
            new SendToDeadLetterQueueCmd().execute(blobCleanerRequest);
        } catch (StorageException e) {
            // move to DLQ
            if (e.getMessage() != null && e.getMessage().contains("blob does not exist")) {return;}
            blobCleanerRequest.setExceptionMessage(e.getClass().getName()+"-"+e.getMessage());
            new SendToDeadLetterQueueCmd().execute(blobCleanerRequest);
        } catch (URISyntaxException e) {
            // move to DLQ
            blobCleanerRequest.setExceptionMessage(e.getClass().getName()+"-"+e.getMessage());
            new SendToDeadLetterQueueCmd().execute(blobCleanerRequest);
        }
    }

    @Deprecated
    private void saveProcessedDelete(String name, BlobCleanerRequest blobCleanerRequest) {
        // build processed delete
        ProcessedDelete processedDelete = new ProcessedDelete();
        processedDelete.setCorrelationId(blobCleanerRequest.getCorrelationId());
        processedDelete.setObjectId(name.split("/")[1]);
        processedDelete.setDeleteTime(System.currentTimeMillis());
        processedDelete.setFileName(name);
        processedDelete.setPath(blobCleanerRequest.getPrefix());
        
        // get cosmos db client
        DocumentClient documentClient = DocumentClientFactory
             .getDocumentClient();

        // build document to be saved
        Document newDocument = new Document(GsonSingleton.getInstance().toJson(processedDelete));

        String collectionLink = String.format("/dbs/%s/colls/%s", "storagecleaner", "processed_delete");

        try {
            documentClient.createDocument(collectionLink, newDocument, null, false).getResource();
        } catch (DocumentClientException e) {
            e.printStackTrace();
		}
    }
    
}