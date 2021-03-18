package org.sunbird.graph.dac.model;

import java.io.Serializable;

import org.sunbird.graph.dac.model.RelationCriterion.DIRECTION;

public class RelationFilter implements Serializable {

    private static final long serialVersionUID = 6067704868050557358L;
    
    private String name;
    private int fromDepth = 1;
    private int toDepth = 1;
    private String direction = DIRECTION.OUT.name();
    
    public RelationFilter(String name) {
        this.name = name;
    }
    
    public RelationFilter(String name, int fromDepth) {
        this.name = name;
        this.fromDepth = fromDepth;
    }
    
    public RelationFilter(String name, int fromDepth, int toDepth) {
        this.name = name;
        this.fromDepth = fromDepth;
        this.toDepth = toDepth;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getFromDepth() {
        return fromDepth;
    }
    public void setFromDepth(int fromDepth) {
        this.fromDepth = fromDepth;
    }
    public int getToDepth() {
        return toDepth;
    }
    public void setToDepth(int toDepth) {
        this.toDepth = toDepth;
    }
    public String getDirection() {
        return direction;
    }
    public void setDirection(String direction) {
        this.direction = direction;
    }
    

}
