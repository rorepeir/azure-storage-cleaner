package com.microsoft.ocp.latam;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import com.microsoft.ocp.latam.command.SendToDeadLetterQueueCmd;
import com.microsoft.ocp.latam.cosmosdb.DocumentClientFactory;
import com.microsoft.ocp.latam.data.BlobCleanerRequest;
import com.microsoft.ocp.latam.data.DeleteRequest;
import com.microsoft.ocp.latam.util.GsonSingleton;

/**
 * Azure Functions with Azure Storage Queue trigger.
 */
public class QueueTriggerNewBlobDeleteRequestJava {

    /**
     * This function will be invoked when a new message is received at the specified
     * path. The message contents are provided as input to this function.
     * 
     * @throws Exception
     */
    @FunctionName("QueueTriggerNewBlobDeleteRequestJava")
    public void run(
        @QueueTrigger(name = "message", queueName = "new-blob-delete-request-queue", connection = "storagecleaner_STORAGE") String message,
        final ExecutionContext context
    ) throws Exception {
        context.getLogger().info("Java Queue trigger function processed a message: " + message);

        // Parse json body request
        Gson gsonUtil = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        BlobCleanerRequest blobCleanerRequest = gsonUtil.fromJson(message, BlobCleanerRequest.class);
        Date dateLimit = getDateLimit(blobCleanerRequest.getDays(), context);
        context.getLogger().info("Date Limit: "+dateLimit.toString());

        try {
            // create storage account reference
            CloudStorageAccount account = CloudStorageAccount.parse(blobCleanerRequest.getConnectionString());

            // create blob client reference
            CloudBlobClient blobClient = account.createCloudBlobClient();

            // get blob container reference
            CloudBlobContainer containerRef = blobClient.getContainerReference(blobCleanerRequest.getContainerName());

            // fetch blob list given prefix
            Iterable<ListBlobItem> blobList = containerRef.listBlobs(blobCleanerRequest.getPrefix());
      
            // iterate over, send all blob files to be deleted to Storage Queue
            long count = 0;
            for (ListBlobItem item: blobList) {
                CloudBlockBlob blob = (CloudBlockBlob)item;

                Date createDate = blob.getProperties().getCreatedTime();
                if (createDate.before(dateLimit)) {
                    // set uri
                    blobCleanerRequest.setAbsolutURI(blob.getUri().toString());
                    
                    // send to delete queue
                    this.sendToDeleteQueue(blob.getName(), blobCleanerRequest, gsonUtil);

                    // increase counter of files to be deleted
                    count++;
                }
            }

            // save delete request
            this.saveDeleteRequest(count, blobCleanerRequest);
        } catch (URISyntaxException e) {
            // move to DLQ
            blobCleanerRequest.setExceptionMessage(e.getClass().getName()+"-"+e.getMessage());
            new SendToDeadLetterQueueCmd().execute(blobCleanerRequest);
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

    private void saveDeleteRequest(long count, BlobCleanerRequest blobCleanerRequest) {
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setCorrelationId(blobCleanerRequest.getCorrelationId());
        deleteRequest.setCurrentTime(System.currentTimeMillis());
        deleteRequest.setDays(blobCleanerRequest.getDays());
        deleteRequest.setNumberOfFiles(count);
        deleteRequest.setPath(blobCleanerRequest.getPrefix());

        // get cosmos db client
        DocumentClient documentClient = DocumentClientFactory
             .getDocumentClient();

        // build document to be saved
        Document newDocument = new Document(GsonSingleton.getInstance().toJson(deleteRequest));

        String collectionLink = String.format("/dbs/%s/colls/%s", "storagecleaner", "delete_request");

        try {
            documentClient.createDocument(collectionLink, newDocument, null, false).getResource();
        } catch (DocumentClientException e) {
            e.printStackTrace();
		}
    }

    private void sendToDeleteQueue(String name, BlobCleanerRequest blobCleanerRequest, Gson gsonUtil) throws Exception {
        // get reference to storage account queue
        CloudStorageAccount storageAccount;
        try {
            storageAccount = CloudStorageAccount.parse(blobCleanerRequest.getConnectionStringQueue());

            // Create the queue client.
            CloudQueueClient queueClient = storageAccount.createCloudQueueClient();

            // Retrieve a reference to a queue.
            CloudQueue queue = queueClient.getQueueReference(blobCleanerRequest.getDeleteProcessingQueueName());

            // Create a message and add it to the queue.
            blobCleanerRequest.setPrefix(name);
            CloudQueueMessage message = new CloudQueueMessage(gsonUtil.toJson(blobCleanerRequest));
            queue.addMessage(message);
        } catch (InvalidKeyException | URISyntaxException e) {
           throw e;
        } catch (StorageException e) {
            throw e;
        }
    }

    private Date getDateLimit(int days, ExecutionContext context) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return Date.from(now.minus(days,ChronoUnit.DAYS).toInstant());
    }

}
