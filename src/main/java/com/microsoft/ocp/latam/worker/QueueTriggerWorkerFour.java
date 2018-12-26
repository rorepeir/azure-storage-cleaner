package com.microsoft.ocp.latam.worker;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Azure Storage Queue trigger.
 */
public class QueueTriggerWorkerFour {
    /**
     * This function will be invoked when a new message is received at the specified path. The message contents are provided as input to this function.
     */
    @FunctionName("QueueTriggerWorkerFour")
    public void run(
        @QueueTrigger(name = "message", queueName = "blob-delete-worker-four-queue", connection = "storagecleaner_STORAGE") String message,
        final ExecutionContext context
    ) {
        new QueueTriggerWorker().execute(context, message);
    }
}