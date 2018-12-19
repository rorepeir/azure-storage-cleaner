package com.microsoft.ocp.latam;

import java.net.URI;
import java.security.InvalidKeyException;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.ocp.latam.command.SendToDeadLetterQueueCmd;
import com.microsoft.ocp.latam.cosmosdb.DocumentClientFactory;
import com.microsoft.ocp.latam.data.BlobCleanerRequest;
import com.microsoft.ocp.latam.data.ProcessedDelete;
import com.microsoft.ocp.latam.util.GsonSingleton;

/**
 * Azure Functions with Azure Storage Queue trigger.
 */
public class QueueTriggerBlobDeleteProcessingJava {
    /**
     * This function will be invoked when a new message is received at the specified
     * path. The message contents are provided as input to this function.
     * 
     * @throws Exception
     */
    @FunctionName("QueueTriggerBlobDeleteProcessingJava")
    public void run(
        @QueueTrigger(name = "message", queueName = "blob-delete-processing-queue", connection = "storagecleaner_STORAGE") String message,
        final ExecutionContext context
    ) throws Exception {
        context.getLogger().info("Java Queue trigger function processed a message: " + message);

        // build request
        Gson gsonUtil = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        BlobCleanerRequest blobCleanerRequest = gsonUtil.fromJson(message, BlobCleanerRequest.class);

        try {
            // fetch blob list given prefix
            StorageCredentials credentials = StorageCredentials.tryParseCredentials(blobCleanerRequest.getConnectionString());
            URI blobURI = new URI(blobCleanerRequest.getAbsolutURI());
            CloudBlockBlob blobToDelete = new CloudBlockBlob(blobURI, credentials);
            blobToDelete.delete();
            
            // save processed delete
            this.saveProcessedDelete(blobToDelete.getName(), blobCleanerRequest);
        } catch (InvalidKeyException e) {
            // move to DLQ
            blobCleanerRequest.setExceptionMessage(e.getClass().getName()+"-"+e.getMessage());
            new SendToDeadLetterQueueCmd().execute(blobCleanerRequest);
        } catch (StorageException e) {
            // move to DLQ
            blobCleanerRequest.setExceptionMessage(e.getClass().getName()+"-"+e.getMessage());
            new SendToDeadLetterQueueCmd().execute(blobCleanerRequest);
        } 
    }

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
