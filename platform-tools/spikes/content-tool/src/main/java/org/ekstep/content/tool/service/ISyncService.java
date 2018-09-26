package org.ekstep.content.tool.service;

public interface ISyncService {

    void dryRun();


    void ownerMigration(String createdBy, String channel, String[] createdFor, String[] organisation, String creator, String filter, String dryRun, String forceUpdate);

    void sync(String filter, String dryRun, String forceUpdate);
}
