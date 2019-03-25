package org.ekstep.jobs.samza.reader;

public interface ParentType {
    <T> T readChild();

    void addChild(Object value);
}
