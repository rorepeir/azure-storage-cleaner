package com.microsoft.ocp.latam.util;

import java.net.URISyntaxException;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;

public class GetWorkerQueue {

    public static CloudQueue getQueueReference(int number, CloudQueueClient queueClient) throws URISyntaxException, StorageException {
        try {
            switch (number) {
                case 1:  return queueClient.getQueueReference("blob-delete-worker-one-queue");
                case 2:  return queueClient.getQueueReference("blob-delete-worker-two-queue");
                case 3:  return queueClient.getQueueReference("blob-delete-worker-three-queue");
                case 4:  return queueClient.getQueueReference("blob-delete-worker-four-queue");
                case 5:  return queueClient.getQueueReference("blob-delete-worker-five-queue");
                case 6:  return queueClient.getQueueReference("blob-delete-worker-six-queue");
                case 7:  return queueClient.getQueueReference("blob-delete-worker-seven-queue");
                case 8:  return queueClient.getQueueReference("blob-delete-worker-eight-queue");
                case 9:  return queueClient.getQueueReference("blob-delete-worker-nine-queue");
                case 10: return queueClient.getQueueReference("blob-delete-worker-ten-queue");
                default: return queueClient.getQueueReference("blob-delete-worker-one-queue");
            }
        }  catch (URISyntaxException e) {
            throw e;
         } catch (StorageException e) {
             throw e;
         }
        
    } 

}