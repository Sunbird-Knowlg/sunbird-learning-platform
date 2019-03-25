package org.ekstep.jobs.samza.reader;

import java.text.MessageFormat;

public class NullParent implements ParentType {
    Object parent;
    String childKey;

    public NullParent(Object parent, String childKey) {
        this.parent = parent;
        this.childKey = childKey;
    }

    @Override
    public <T> T readChild() {
        return null;
    }

    @Override
    public void addChild(Object value) {
    }
}

