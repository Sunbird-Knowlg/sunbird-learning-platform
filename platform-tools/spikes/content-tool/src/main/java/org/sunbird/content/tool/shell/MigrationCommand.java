package org.sunbird.content.tool.shell;

import org.sunbird.content.tool.service.ISyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MigrationCommand extends BaseCommand implements CommandMarker {

    @Autowired
    @Qualifier("contentSyncService")
    ISyncService syncService;


    @CliCommand(value = "content-tool owner-migration", help = "Ownership migration")
    public void ownerMigration(@CliOption(key = {"userId"}, mandatory = false, help = "User Id") final String userId,
                               @CliOption(key = {
                                       "channel"}, mandatory = false, help = "channel ID") final String channel,
                               @CliOption(key = {
                                       "createdFor"}, mandatory = false, help = "Content created for?") final String[] createdFor,
                               @CliOption(key = {
                                       "organisation"}, mandatory = false, help = "list of organisation") final String[] organisation,
                               @CliOption(key = {
                                       "creator"}, mandatory = false, help = "Content creator") final String creator,
                               @CliOption(key = {
                                       "filter"}, mandatory = false, help = "filters to search for ") final String filter,
                               @CliOption(key = {
                                       "ids"}, mandatory = false, help = "identifiers to be synced") final String[] ids,
                               @CliOption(key = {
                                       "objectType"}, mandatory = false, help = "object Type") final String objectType,
                               @CliOption(key = {
                                       "createdBy"}, mandatory = false, help = "createdBy / user id") final String createdBy,
                               @CliOption(key = {
                                       "lastUpdatedOn"}, mandatory = false, help = "last updated on date") final String lastUpdatedOn,
                               @CliOption(key = {
                                       "dry-run"}, mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "dry run") String dryRun,
                               @CliOption(key = {
                                       "force"}, mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "force update") String forceUpdate,
                               @CliOption(key = {
                                       "limit"}, mandatory = false, help = "limit on migration count") String limit,
                               @CliOption(key = {
                                       "offset"}, mandatory = false, help = "offset from which the migration should continue") String offset,
                               @CliOption(key = {
                                       "status"}, mandatory = false, unspecifiedDefaultValue = "Live", specifiedDefaultValue = "", help = " of the content") String status)
            throws Exception {

        System.out.println("-----------------------------------------");
        Map<String, Object> filters = prepareFilters(objectType, filter, ids, createdBy, lastUpdatedOn,  limit, offset, false, status);
        syncService.ownerMigration(userId, channel, createdFor, organisation, creator, filters, dryRun, forceUpdate);
        System.out.println("-----------------------------------------");
    }

}
