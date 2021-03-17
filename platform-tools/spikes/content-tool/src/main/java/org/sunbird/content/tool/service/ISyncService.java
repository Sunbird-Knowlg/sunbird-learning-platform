package org.sunbird.content.tool.service;

import java.util.Map;

public interface ISyncService {

    void ownerMigration(String createdBy, String channel, String[] createdFor, String[] organisation, String creator, Map<String, Object> filter, String dryRun, String forceUpdate);

    void sync(Map<String, Object> filter, String dryRun, String forceUpdate);
}
