# azure-storage-cleaner

This project removes expired files from Azure Blob. It leverages Azure Queues + Azure Functions. Basically, it creates a HTTP enpoint 
that receives a JSON with Azure Blob Path and Time To Live in days.





