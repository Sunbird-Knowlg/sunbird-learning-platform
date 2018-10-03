package org.ekstep.content.tool.shell;

import org.ekstep.content.tool.service.ISyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class MigrationCommand implements CommandMarker {

    @Autowired
    @Qualifier("contentSyncService")
    ISyncService syncService;


    @CliCommand(value = "content-tool owner-migration", help = "Ownership migration")
    public void ownerMigration(@CliOption(key = {"userId"}, mandatory = false, help = "User Id") final String createdBy,
                               @CliOption(key = {
                                       "channel"}, mandatory = false, unspecifiedDefaultValue = "in.ekstep", help = "channel ID") final String channel,
                               @CliOption(key = {
                                       "createdFor"}, mandatory = false, help = "Content created for?") final String[] createdFor,
                               @CliOption(key = {
                                       "organisation"}, mandatory = false, help = "list of organization") final String[] organisation,
                               @CliOption(key = {
                                       "creator"}, mandatory = false, help = "Content creator") final String creator,
                               @CliOption(key = {
                                       "filter"}, mandatory = false, help = "filters to search for ") final String filter,
                               @CliOption(key = {
                                       "dry-run"}, mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "dry run") String dryRun,
                               @CliOption(key = {
                                       "force"}, mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "force update") String forceUpdate)
            throws Exception {

        System.out.println("-----------------------------------------");
        syncService.ownerMigration(createdBy, channel, createdFor, organisation, creator, filter, dryRun, forceUpdate);
        System.out.println("-----------------------------------------");
    }



}
