package org.sunbird.graph.dac.enums;

public enum RelationDirection {

    INCOMING, OUTGOING, BOTH;

    public RelationDirection reverse() {
        switch (this) {
        case OUTGOING:
            return INCOMING;
        case INCOMING:
            return OUTGOING;
        case BOTH:
            return BOTH;
        default:
            throw new IllegalStateException("Unknown Direction " + "enum: " + this);
        }
    }
}
