package org.sunbird.graph.model;

import java.util.Map;

public interface ICriteria {

    Map<String, Object> getCriteria();

    void setCriteria(Map<String, Object> criteria);

    String getObjectType();

    void setObjectType(String objectType);
}
