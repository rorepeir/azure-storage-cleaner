# azure-storage-cleaner

This project removes expired files from Azure Blob. It leverages Azure Queues + Azure Functions. Basically, it creates a HTTP endpoint that receives a JSON with Azure Blob Path and Time To Live in days, after that, it list candidate blob to be removed and send to worker Azure Queues.

## Installation

VS Code project is supported. You need to clone the repository and open it in VS Code.

## Running

After publishing the project to Azure Functions (You can use Azure Functions plug in to VS Code), a HTTP Endpoint will be created, as below:

https://<function_name>.azurewebsites.net/api/HttpTriggerNewDeleteBlobRequestJava?code=<credential to invoke azure function>
  
You need to do a http POST with the folloing JSON body:

{
  "connection_string": "<azure blob connection string>",
  "container_name": "container name",
  "prefix": "<prefix to fetch blobs to be removed>",
  "days": <time to live in days>,
  "connection_string_queue": "connection string of azure queues"
}
  
At this point, HttpTriggerNewDeleteBlobRequestJava will put the request on "new-blob-delete-request-queue" queue. Then, QueueTriggerNewBlobDeleteRequestJava will dequeue the message, list all blobs in that "prefix" path, and then send a blob delete request to each expired blob found to 10 worker queues (blob-delete-worker-<number>-queue).

After that, you can monitor queues depth by making a http post to this endpoint using the same previsous JSON body:

https://<function_name>.azurewebsites.net/api/TriggerHttpGetDeleteQueueDepthJava?code=<credential to invoke azure function>
  
You will get the following return (depth for each worker queue):

[{"name":"blob-delete-worker-one-queue","depth":0},{"name":"blob-delete-worker-two-queue","depth":0},{"name":"blob-delete-worker-three-queue","depth":0},{"name":"blob-delete-worker-four-queue","depth":0},{"name":"blob-delete-worker-five-queue","depth":0},{"name":"blob-delete-worker-six-queue","depth":0},{"name":"blob-delete-worker-seven-queue","depth":0},{"name":"blob-delete-worker-eight-queue","depth":0},{"name":"blob-delete-worker-nine-queue","depth":0},{"name":"blob-delete-worker-ten-queue","depth":0}]


  





