package com.microsoft.ocp.latam;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.ocp.latam.command.SendToDeadLetterQueueCmd;
import com.microsoft.ocp.latam.data.BlobCleanerRequest;

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
            // create storage account reference
            CloudStorageAccount account = CloudStorageAccount.parse(blobCleanerRequest.getConnectionString());

            // create blob client reference
            CloudBlobClient blobClient = account.createCloudBlobClient();

            // get blob container reference
            CloudBlobContainer containerRef = blobClient.getContainerReference(blobCleanerRequest.getContainerName());

            // fetch blob list given prefix
            Iterable<ListBlobItem> blobList = containerRef.listBlobs(blobCleanerRequest.getPrefix());
      
            // iterate over, send all blob files to be deleted to Storage Queue
            for (ListBlobItem item: blobList) {
                CloudBlockBlob blob = (CloudBlockBlob)item;

                context.getLogger().info("Blob found to be deleted:"+blob.getName());
                blob.deleteIfExists();
            }
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
}
