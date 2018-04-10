package org.ekstep.sync.tool.shell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LPShellBanner extends DefaultBannerProvider {
 
    public String getBanner() {
        StringBuffer buf = new StringBuffer();
        buf.append(" _   _            _  _   _   ____                     _____           _ ")
            .append(OsUtils.LINE_SEPARATOR);
        buf.append("| \\ | | ___  ___ | || | (_) / ___| _   _ _ __   ___  |_   _|__   ___ | |")
            .append(OsUtils.LINE_SEPARATOR);
        buf.append("|  \\| |/ _ \\/ _ \\| || |_| | \\___ \\| | | | '_ \\ / __|   | |/ _ \\ / _ \\| |")
            .append(OsUtils.LINE_SEPARATOR);
        buf.append("| |\\  |  __/ (_) |__   _| |  ___) | |_| | | | | (__    | | (_) | (_) | |")
        .append(OsUtils.LINE_SEPARATOR);
        buf.append("|_| \\_|\\___|\\___/   |_|_/ | |____/ \\__, |_| |_|\\___|   |_|\\___/ \\___/|_|")
        .append(OsUtils.LINE_SEPARATOR);
        buf.append("                      |__/         |___/                                ")
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
