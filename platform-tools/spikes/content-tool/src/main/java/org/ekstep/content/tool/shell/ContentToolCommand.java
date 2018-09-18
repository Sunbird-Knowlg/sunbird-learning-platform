package org.ekstep.content.tool.shell;

import org.ekstep.content.tool.service.ISyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class ContentToolCommand implements CommandMarker {

    @Autowired
    @Qualifier("contentSyncService")
    ISyncService syncService;


    @CliCommand(value = "content-tool owner-migration", help = "Ownership migration")
    public void ownerMigration(@CliOption(key = {"userId"}, mandatory = false, help = "User Id") final String createdBy,
                               @CliOption(key = {
                                       "channel"}, mandatory = false, help = "channel ID") final String channel,
                               @CliOption(key = {
                                       "createdFor"}, mandatory = false, help = "Content created for?") final String[] createdFor,
                               @CliOption(key = {
                                       "organisation"}, mandatory = false, help = "list of organization") final String[] organisation,
                               @CliOption(key = {
                                       "creator"}, mandatory = false, help = "Content creator") final String creator,
                               @CliOption(key = {
                                       "filter"}, mandatory = false, help = "filters to search for ") final String filter,
                               @CliOption(key = {
                                       "dry-run"}, mandatory = false, unspecifiedDefaultValue = "false", help = "dry run") String dryRun)
            throws Exception {

        System.out.println("-----------------------------------------");
        syncService.ownerMigration(createdBy, channel, createdFor, organisation, creator , filter, dryRun);
        System.out.println("-----------------------------------------");
    }

    @CliCommand(value = "content-tool sync", help = "Content Sync")
    public void sync(@CliOption(key = {
                                       "filter"}, mandatory = false, help = "filters to search for ") final String filter,
                               @CliOption(key = {
                                       "dry-run"}, mandatory = false, unspecifiedDefaultValue = "false", help = "dry run ") String dryRun)
            throws Exception {

        System.out.println("-----------------------------------------");
        syncService.sync(filter, dryRun);
        System.out.println("-----------------------------------------");
    }

}
