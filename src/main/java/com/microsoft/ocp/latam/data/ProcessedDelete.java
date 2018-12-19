package com.microsoft.ocp.latam.data;

public class ProcessedDelete {

    private String objectId;
    private long deleteTime;
    private String fileName;
    private String path;
    private String correlationId;

   
    /**
     * @return the correlationId
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * @return the objectId
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * @param objectId the objectId to set
     */
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    /**
     * @param correlationId the correlationId to set
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the deleteTime
     */
    public long getDeleteTime() {
        return deleteTime;
    }

    /**
     * @param deleteTime the deleteTime to set
     */
    public void setDeleteTime(long deleteTime) {
        this.deleteTime = deleteTime;
    }




}