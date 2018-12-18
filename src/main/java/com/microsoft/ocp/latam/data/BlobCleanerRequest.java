package com.microsoft.ocp.latam.data;

public class BlobCleanerRequest {

    private String prefix;
    private String containerName;
    private String connectionString;
    private int days;
    private String connectionStringQueue;
    private String deleteProcessingQueueName;
    private String exceptionMessage;

    /**
     * @return the containerName
     */
    public String getContainerName() {
        return containerName;
    }
    
    /**
     * @return the exceptionMessage
     */
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    /**
     * @param exceptionMessage the exceptionMessage to set
     */
    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    /**
     * @return the deleteProcessingQueueName
     */
    public String getDeleteProcessingQueueName() {
        return deleteProcessingQueueName;
    }

    /**
     * @param deleteProcessingQueueName the deleteProcessingQueueName to set
     */
    public void setDeleteProcessingQueueName(String deleteProcessingQueueName) {
        this.deleteProcessingQueueName = deleteProcessingQueueName;
    }

    /**
     * @return the connectionStringQueue
     */
    public String getConnectionStringQueue() {
        return connectionStringQueue;
    }

    /**
     * @param connectionStringQueue the connectionStringQueue to set
     */
    public void setConnectionStringQueue(String connectionStringQueue) {
        this.connectionStringQueue = connectionStringQueue;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return the days
     */
    public int getDays() {
        return days;
    }

    /**
     * @param days the days to set
     */
    public void setDays(int days) {
        this.days = days;
    }

    /**
     * @return the connectionString
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * @param connectionString the connectionString to set
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * @param containerName the containerName to set
     */
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
    
    

}