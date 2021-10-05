package org.sunbird.content.tool.shell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ContentToolBanner  extends DefaultBannerProvider {

	public String getBanner() {
        StringBuffer buf = new StringBuffer();
        buf.append("   _____            _             _       _______          _ ")
                .append(OsUtils.LINE_SEPARATOR);
        buf.append("  / ____|          | |           | |     |__   __|        | |")
                .append(OsUtils.LINE_SEPARATOR);
        buf.append(" | |     ___  _ __ | |_ ___ _ __ | |_ ______| | ___   ___ | |")
                .append(OsUtils.LINE_SEPARATOR);
        buf.append(" | |    / _ \\| '_ \\| __/ _ \\ '_ \\| __|______| |/ _ \\ / _ \\| |")
                .append(OsUtils.LINE_SEPARATOR);
        buf.append(" | |___| (_) | | | | ||  __/ | | | |_       | | (_) | (_) | |")
                .append(OsUtils.LINE_SEPARATOR);
        buf.append("  \\_____\\___/|_| |_|\\__\\___|_| |_|\\__|      |_|\\___/ \\___/|_|")
                .append(OsUtils.LINE_SEPARATOR);
        buf.append("                                                             ")
                .append(OsUtils.LINE_SEPARATOR);
        buf.append("                                                             ")
                .append(OsUtils.LINE_SEPARATOR);
        buf.append("Version:")
            .append(this.getVersion());
        return buf.toString();
    }

    public String getVersion() {
        return "1.0.0";
    }
 
    public String getWelcomeMessage() {
        return "Welcome to Content-Tool: Sync and Ownership Migration";
    }
 
    public String getProviderName() {
        return "Learning-Platform";
    }
	
}