package com.microsoft.ocp.latam.cosmosdb;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClient;

public class DocumentClientFactory {

    private static final String HOST = System.getenv("COSMOS_DB_ENDPOINT");
    private static final String MASTER_KEY = System.getenv("COSMOS_DB_MASTER_KEY");

    private static DocumentClient documentClient;

    public static DocumentClient getDocumentClient() {
        if (documentClient == null) {
            documentClient = new DocumentClient(HOST, MASTER_KEY,
                    ConnectionPolicy.GetDefault(), ConsistencyLevel.Eventual);
        }

        return documentClient;
    }

}