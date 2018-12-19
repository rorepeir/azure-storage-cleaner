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
import java.util.Optional;
import java.util.UUID;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import com.microsoft.ocp.latam.data.BlobCleanerRequest;
import com.microsoft.ocp.latam.util.GsonSingleton;

/**
 * Azure Functions with HTTP Trigger.
 * 
 * It receives a HTTP Post with a new clean storage blob request.
 * 
 */
public class HttpTriggerNewDeleteBlobRequestJava {

    private final String NEW_BLOB_DELETE_REQUEST_QUEUE_NAME = "new-blob-delete-request-queue";

    /**
     * This function listens at endpoint "/api/HttpTriggerNewDeleteBlobRequestJava". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpTriggerNewDeleteBlobRequestJava
     * 2. curl {your host}/api/HttpTriggerNewDeleteBlobRequestJava?name=HTTP%20Query
     */
    @FunctionName("HttpTriggerNewDeleteBlobRequestJava")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
                
        context.getLogger().info("New Storage Delete Blob Request."+request.toString());

        // build request
        String requestParams = request.getBody().get();
        BlobCleanerRequest blobCleanerRequest = GsonSingleton.getInstance().fromJson(requestParams, BlobCleanerRequest.class);
        blobCleanerRequest.setCorrelationId(UUID.randomUUID().toString());

        // get reference to storage account queue
        CloudStorageAccount storageAccount;
        try {
            storageAccount = CloudStorageAccount.parse(blobCleanerRequest.getConnectionStringQueue());

            // Create the queue client.
            CloudQueueClient queueClient = storageAccount.createCloudQueueClient();

            // Retrieve a reference to a queue.
            CloudQueue queue = queueClient.getQueueReference(NEW_BLOB_DELETE_REQUEST_QUEUE_NAME);

            // Create a message and add it to the queue.
            CloudQueueMessage message = new CloudQueueMessage(GsonSingleton.getInstance().toJson(blobCleanerRequest));
            queue.addMessage(message);
            
            return request.createResponseBuilder(HttpStatus.OK).body("Request received successfully. Message id:"+blobCleanerRequest.getCorrelationId()).build();
        } catch (InvalidKeyException | URISyntaxException e) {
           return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()).build();
        } catch (StorageException e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()).build();
        }
    }
}
