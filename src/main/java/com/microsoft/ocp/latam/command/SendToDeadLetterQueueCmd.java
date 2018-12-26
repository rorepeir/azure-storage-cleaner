package com.microsoft.ocp.latam.command;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import com.microsoft.ocp.latam.data.BlobCleanerRequest;

public class SendToDeadLetterQueueCmd {

    private final String DEAD_LETTER_QUEUE_NAME = "dead-letter-queue";
    private Gson gsonUtil = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    public void execute (BlobCleanerRequest blobCleanerRequest) {
        // get reference to storage account queue
        CloudStorageAccount storageAccount;
        try {

            storageAccount = CloudStorageAccount.parse(blobCleanerRequest.getConnectionStringQueue());

            // Create the queue client.
            CloudQueueClient queueClient = storageAccount.createCloudQueueClient();

            // Retrieve a reference to a queue.
            CloudQueue queue = queueClient.getQueueReference(DEAD_LETTER_QUEUE_NAME);

            // Create a message and add it to the queue.
            CloudQueueMessage message = new CloudQueueMessage(gsonUtil.toJson(blobCleanerRequest));
            queue.addMessage(message);
        } catch (InvalidKeyException | URISyntaxException e) {
            e.printStackTrace();
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

}