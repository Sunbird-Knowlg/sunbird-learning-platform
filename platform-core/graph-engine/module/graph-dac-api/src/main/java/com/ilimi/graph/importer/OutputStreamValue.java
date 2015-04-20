package com.ilimi.graph.importer;

import java.io.OutputStream;

import com.ilimi.graph.common.dto.BaseValueObject;

public class OutputStreamValue extends BaseValueObject {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private OutputStream outputStream;

    public OutputStreamValue(OutputStream outputStream) {
        super();
        this.outputStream = outputStream;
    }

    public OutputStreamValue() {
        super();
    }

    /**
     * @return the outputStream
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * @param outputStream
     *            the outputStream to set
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

}
