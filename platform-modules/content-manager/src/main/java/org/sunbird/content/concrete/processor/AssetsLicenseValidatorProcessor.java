package org.sunbird.content.concrete.processor;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.mgr.IURLManager;
import org.sunbird.common.mgr.impl.YoutubeUrlManagerImpl;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Media;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.processor.AbstractProcessor;
import org.sunbird.content.processor.ContentPipelineProcessor;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AssetsLicenseValidatorProcessor is a Content Workflow pipeline Processor
 * Which is responsible for Validating Youtube Asset Medias and
 * their license support.
 *
 * @see AssetsValidatorProcessor
 * @see BaseConcreteProcessor
 * @see ContentPipelineProcessor
 * @see GlobalizeAssetProcessor
 * @see LocalizeAssetProcessor
 * @see MissingAssetValidatorProcessor
 * @see MissingControllerValidatorProcessor
 *
 * @see AbstractProcessor
 */
public class AssetsLicenseValidatorProcessor extends AbstractProcessor {

    /** List of Media Types which require License Validation */
    private static List<String> validMediaTypes = Platform.config.hasPath("learning.service_provider") ?
            Platform.config.getStringList("learning.service_provider") : Arrays.asList("youtube");

    /**
     * Instantiates a new <code>AssetsLicenseValidatorProcessor</code> and sets the base path and
     * current content id for further processing.
     *
     * @param basePath
     *            the base path is the location for content package file
     *            handling and all manipulations.
     * @param contentId
     *            the content id is the identifier of content for which the
     *            Processor is being processed currently.
     */
    public AssetsLicenseValidatorProcessor(String basePath, String contentId) {
        if (!isValidBasePath(basePath))
            throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
                    ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
        if (StringUtils.isBlank(contentId))
            throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
                    ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
        this.basePath = basePath;
        this.contentId = contentId;
    }

    /**
     * Implementation for {@link AbstractProcessor#process(Plugin)}
     *
     * @param plugin
     * @return Plugin object
     */
    @Override
    protected Plugin process(Plugin plugin) {
        if (null != plugin)
            Optional.ofNullable(getMedia(plugin)).ifPresent(medias -> validateLicenses(medias));
        return plugin;
    }

    /**
     * Validates License Support for Youtube Medias from ECRF
     *
     * @param medias
     *          set of media from ECRF.
     */
    private void validateLicenses(List<Media> medias) {
        medias.stream().filter(media -> validMediaTypes.contains(media.getType())).
                forEach(media -> {
                    TelemetryManager.log("Validating License for Media Id::" + media.getId() + "and Media Type:: " + media.getType());
                    try {
                        validateLicense(media.getType(), media.getSrc());
                    } catch(ClientException ce) {
                        throw ce;
                    } catch(Exception e) {
                        throw new ServerException(ContentErrorCodeConstants.PROCESSOR_ERROR.name(),
                                ContentErrorMessageConstants.PROCESSOR_ERROR + " | [AssetsLicenseValidatorProcessor]", e);
                    }
                });
    }

    private void validateLicense(String type, String src) {
        switch (type) {
            case "youtube":{
		        	IURLManager youtubeUrlManager = new YoutubeUrlManagerImpl();
		    		Map<String, Object> metadata = youtubeUrlManager.validateURL(src, "license");
		    		boolean isValid = null != metadata.get("valid") ? (boolean)metadata.get("valid") : false;
		    		if(!isValid)
		    			throw new ClientException(ContentErrorCodeConstants.INVALID_YOUTUBE_MEDIA.name(), ContentErrorMessageConstants.LICENSE_NOT_SUPPORTED);
		        break;
		    }
        	    default:
        	    		break;
        }
    }
}
