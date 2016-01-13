package org.ekstep.language.measures.entity;

import java.io.Serializable;

public class ComplexityMeasures implements Serializable {

    private static final long serialVersionUID = 423982555916654298L;
    private Double orthographic_complexity;
    private Double phonologic_complexity;
    
    public ComplexityMeasures() {
        
    }
    
    public ComplexityMeasures(Double orthographic, Double phonologic) {
        this.orthographic_complexity = orthographic;
        this.phonologic_complexity = phonologic;
    }

    public Double getOrthographic_complexity() {
        return orthographic_complexity;
    }

    public void setOrthographic_complexity(Double orthographic_complexity) {
        this.orthographic_complexity = orthographic_complexity;
    }

    public Double getPhonologic_complexity() {
        return phonologic_complexity;
    }

    public void setPhonologic_complexity(Double phonologic_complexity) {
        this.phonologic_complexity = phonologic_complexity;
    }
}
