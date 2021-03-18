package org.sunbird.graph.dac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SubGraph implements Serializable {

    private static final long serialVersionUID = -1325098335003990214L;
    private List<Path> paths;

    public SubGraph() {
        this.paths = new ArrayList<Path>();
    }

    public void addPath(Path path) {
        this.paths.add(path);
    }

    public List<Path> getPaths() {
        return paths;
    }

    public void setPaths(List<Path> paths) {
        this.paths = paths;
    }

}
