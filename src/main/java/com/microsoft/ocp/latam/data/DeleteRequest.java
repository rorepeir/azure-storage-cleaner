package com.microsoft.ocp.latam.data;


public class DeleteRequest {

    private String correlationId;
    private long numberOfFiles;
    private String path;
    private long days;
    private long currentTime;

    /**
     * @return the correlationId
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * @return the currentTime
     */
    public long getCurrentTime() {
        return currentTime;
    }

    /**
     * @param currentTime the currentTime to set
     */
    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    /**
     * @return the days
     */
    public long getDays() {
        return days;
    }

    /**
     * @param days the days to set
     */
    public void setDays(long days) {
        this.days = days;
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
     * @return the numberOfFiles
     */
    public long getNumberOfFiles() {
        return numberOfFiles;
    }

    /**
     * @param numberOfFiles the numberOfFiles to set
     */
    public void setNumberOfFiles(long numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    /**
     * @param correlationId the correlationId to set
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }



}