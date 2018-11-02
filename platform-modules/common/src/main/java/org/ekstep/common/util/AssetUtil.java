package org.ekstep.common.util;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.Arrays;
import java.util.List;

public class AssetUtil {

    private static List<String> validLicenses = Platform.config.hasPath("learning.valid_license") ? Platform.config.getStringList("learning.valid_license") : Arrays.asList("creativeCommon");;

    public static String getLicenseType(String provider, String url) {
        String licenseType;
        switch (StringUtils.lowerCase(provider)) {
            case "youtube": TelemetryManager.log("Getting Youtube License");
                            licenseType = YouTubeDataAPIV3Service.getLicense(url);
                            break;
            default       : throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid Provider");
        }
        return licenseType;
    }

    public static boolean isValidLicense(String license) {
        return validLicenses.contains(license);
    }

}
