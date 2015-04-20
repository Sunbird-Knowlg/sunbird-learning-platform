package com.ilimi.graph.dac.model;

import java.util.ArrayList;
import java.util.List;

import com.ilimi.graph.common.dto.BaseValueObject;

public class SubGraph extends BaseValueObject {

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
