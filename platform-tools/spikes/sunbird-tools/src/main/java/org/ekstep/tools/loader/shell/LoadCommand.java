package org.ekstep.tools.loader.shell;

import java.io.File;
import org.ekstep.tools.loader.service.BulkLoaderService;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class LoadCommand implements CommandMarker {

    public LoadCommand() {
    }

    @CliCommand(value = "load content", help = "Bulk load content to the target environment")
    public String loadContent(
            @CliOption(key = { "input" }, mandatory = true, help = "Input CSV file") final File csvFile,
            @CliOption(key = { "mapping" }, mandatory = true, help = "Mapping file") final File tfmFile,
            @CliOption(key = { "key-column" }, mandatory = true, help = "Name of the key column in csv") final String keyColumn,
            @CliOption(key = { "dry-run" }, mandatory = false, help = "Dry-run only") final boolean dryRun
    ) throws Exception {
        
        ShellContext context = ShellContext.getInstance();
        if (context.getCurrentConfig() == null) return "Not logged in.";
        
        BulkLoaderService service = new BulkLoaderService();
        service.setCsvFile(csvFile);
        service.setTfmFile(tfmFile);
        service.setKeyColumn(keyColumn);
        service.setUserID(keyColumn);
        
        System.out.println("Starting the bulk load process...");
        long begin = System.currentTimeMillis();
        service.execute(service);
        long end = System.currentTimeMillis();
        
        return "Completed the operation in " + (end - begin) + " ms.";
    }
    
}

