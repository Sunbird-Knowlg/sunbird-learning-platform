package org.sunbird.sync.tool.shell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SyncShellBanner extends DefaultBannerProvider {
 
	/*
		 _   _            _  _   _       _____ ____    ____                     _____           _ 
		| \ | | ___  ___ | || | (_)     | ____/ ___|  / ___| _   _ _ __   ___  |_   _|__   ___ | |
		|  \| |/ _ \/ _ \| || |_| |_____|  _| \___ \  \___ \| | | | '_ \ / __|   | |/ _ \ / _ \| |
		| |\  |  __/ (_) |__   _| |_____| |___ ___) |  ___) | |_| | | | | (__    | | (_) | (_) | |
		|_| \_|\___|\___/   |_|_/ |     |_____|____/  |____/ \__, |_| |_|\___|   |_|\___/ \___/|_|
		                      |__/                           |___/                                

	 */
	public String getBanner() {
        StringBuffer buf = new StringBuffer();
        buf.append(" _   _            _  _   _       _____ ____    ____                     _____           _ ")
            .append(OsUtils.LINE_SEPARATOR);
        buf.append("| \\ | | ___  ___ | || | (_)     | ____/ ___|  / ___| _   _ _ __   ___  |_   _|__   ___ | |")
            .append(OsUtils.LINE_SEPARATOR);
        buf.append("|  \\| |/ _ \\/ _ \\| || |_| |_____|  _| \\___ \\  \\___ \\| | | | '_ \\ / __|   | |/ _ \\ / _ \\| |")
            .append(OsUtils.LINE_SEPARATOR);
        buf.append("| |\\  |  __/ (_) |__   _| |_____| |___ ___) |  ___) | |_| | | | | (__    | | (_) | (_) | |")
        .append(OsUtils.LINE_SEPARATOR);
        buf.append("|_| \\_|\\___|\\___/   |_|_/ |     |_____|____/  |____/ \\__, |_| |_|\\___|   |_|\\___/ \\___/|_|")
        .append(OsUtils.LINE_SEPARATOR);
        buf.append("                      |__/                           |___/                                ")
        .append(OsUtils.LINE_SEPARATOR);
        buf.append("Version:")
            .append(this.getVersion());
        return buf.toString();
    }
 
    public String getVersion() {
        return "1.0.0";
    }
 
    public String getWelcomeMessage() {
        return "Welcome to Neo4j-ES Sync Tool";
    }
 
    public String getProviderName() {
        return "Learning-Platform";
    }
}
