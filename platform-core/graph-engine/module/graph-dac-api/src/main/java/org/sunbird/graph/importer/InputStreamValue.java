package org.sunbird.graph.importer;

import java.io.InputStream;
import java.io.Serializable;

public class InputStreamValue implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private InputStream inputStream;

    public InputStreamValue(InputStream inputStream) {
        super();
        this.inputStream = inputStream;
    }

    public InputStreamValue() {
        super();
    }

    /**
     * @return the inputStream
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * @param inputStream
     *            the inputStream to set
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

}
