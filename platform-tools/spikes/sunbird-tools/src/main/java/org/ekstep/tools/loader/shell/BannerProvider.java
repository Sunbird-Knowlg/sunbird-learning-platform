package org.ekstep.tools.loader.shell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

/**
 * @author Jarred Li
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BannerProvider extends DefaultBannerProvider  {

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public String getBanner() {
        StringBuilder buf = new StringBuilder();
        
        buf.append("==============================================================================" + OsUtils.LINE_SEPARATOR + OsUtils.LINE_SEPARATOR);
        buf.append("   ______  ___  _____  _______  ___    __________  ____  __   ____" + OsUtils.LINE_SEPARATOR);
        buf.append("  / __/ / / / |/ / _ )/  _/ _ \\/ _ \\  /_  __/ __ \\/ __ \\/ /  / __/" + OsUtils.LINE_SEPARATOR);
        buf.append(" _\\ \\/ /_/ /    / _  |/ // , _/ // /   / / / /_/ / /_/ / /___\\ \\  " + OsUtils.LINE_SEPARATOR);
        buf.append("/___/\\____/_/|_/____/___/_/|_/____/   /_/  \\____/\\____/____/___/  " + OsUtils.LINE_SEPARATOR);
        buf.append("                                                                  " + OsUtils.LINE_SEPARATOR);
        buf.append("Version:" + this.getVersion());
        buf.append(OsUtils.LINE_SEPARATOR + OsUtils.LINE_SEPARATOR + "==============================================================================" + OsUtils.LINE_SEPARATOR);
        
        return buf.toString();
    }

    public String getVersion() {
        return "1.2.3";
    }

    public String getWelcomeMessage() {
        return "Welcome to Sunbird Tools CLI" + OsUtils.LINE_SEPARATOR;
    }

    @Override
    public String getProviderName() {
        return "Sunbird Tools";
    }
}
