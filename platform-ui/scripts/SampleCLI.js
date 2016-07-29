#!/usr/bin/env node

var cli = require('cli');

var options = cli.parse({
    dryrun:   ['d', 'Dry run (parse the items csv and print to console).'],
    user:  ['u', 'Your user id (will show in My Items view)', 'string'],
    env:      ['e', 'Environment', 'string', 'prod'],
    file:     ['f', 'Items csv file to process', 'file'],
    mapping:  ['m', 'Mapping json file', 'file', 'mcq_mapping_v2.json'],

});

if ((!options.file) || (!options.env) || (!options.user)) {
    cli.error("Insufficient inputs.");
    cli.fatal("   [itemimporter --help] for usage help. ");
}
