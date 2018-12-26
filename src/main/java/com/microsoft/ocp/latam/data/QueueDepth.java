package com.microsoft.ocp.latam.data;


public class QueueDepth {

    private String name;
    private long depth;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the depth
     */
    public long getDepth() {
        return depth;
    }

    /**
     * @param depth the depth to set
     */
    public void setDepth(long depth) {
        this.depth = depth;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }



}