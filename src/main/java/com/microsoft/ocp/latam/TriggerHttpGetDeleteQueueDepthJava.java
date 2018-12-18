package com.microsoft.ocp.latam;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.ocp.latam.data.BlobCleanerRequest;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class TriggerHttpGetDeleteQueueDepthJava {
    /**
     * This function listens at endpoint "/api/TriggerHttpGetDeleteQueueDepthJava".
     * Two ways to invoke it using "curl" command in bash: 1. curl -d "HTTP Body"
     * {your host}/api/TriggerHttpGetDeleteQueueDepthJava 2. curl {your
     * host}/api/TriggerHttpGetDeleteQueueDepthJava?name=HTTP%20Query
     * 
     * @throws Exception
     */
    @FunctionName("TriggerHttpGetDeleteQueueDepthJava")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws Exception {
        
        // parse request params
        String requestParams = request.getBody().get();
        Gson gsonUtil = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        BlobCleanerRequest blobCleanerRequest = gsonUtil.fromJson(requestParams, BlobCleanerRequest.class);

        // get reference to storage account queue
        CloudStorageAccount storageAccount;
        try {
            // storage account
            storageAccount = CloudStorageAccount.parse(blobCleanerRequest.getConnectionStringQueue());

            // Create the queue client.
            CloudQueueClient queueClient = storageAccount.createCloudQueueClient();

            // Retrieve a reference to a queue.
            CloudQueue queue = queueClient.getQueueReference(blobCleanerRequest.getDeleteProcessingQueueName());
            queue.downloadAttributes();

            return request.createResponseBuilder(HttpStatus.OK).body("Current Depth:"+queue.getApproximateMessageCount()).build();
        } catch (InvalidKeyException | URISyntaxException e) {
           throw e;
        } catch (StorageException e) {
            throw e;
        }
        
    }
}
