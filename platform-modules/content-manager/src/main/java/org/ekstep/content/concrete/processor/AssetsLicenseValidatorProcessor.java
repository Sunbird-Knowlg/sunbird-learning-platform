package org.ekstep.content.concrete.processor;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.YouTubeDataAPIV3Service;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.entity.Media;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.processor.AbstractProcessor;
import org.ekstep.content.processor.ContentPipelineProcessor;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.ekstep.common.util.AssetUtil.isValidLicense;

/**
 * YoutubeAssetProcessor is a Content Workflow pipeline Processor
 * Which is responsible for Validating Youtube Asset Medias and
 * their license support.
 *
 * @see AssessmentItemCreatorProcessor
 * @see AssetCreatorProcessor
 * @see AssetsValidatorProcessor
 * @see BaseConcreteProcessor
 * @see ContentPipelineProcessor
 * @see EmbedControllerProcessor
 * @see GlobalizeAssetProcessor
 * @see LocalizeAssetProcessor
 * @see MissingAssetValidatorProcessor
 * @see MissingControllerValidatorProcessor
 *
 * @see AbstractProcessor
 */
public class AssetsLicenseValidatorProcessor extends AbstractProcessor {

    /** List of Media Types which require License Validation*/
    private static List<String> validMediaTypes;

    /**
     * Instantiates a new <code>YoutbeAssetProcessor</code> and sets the base path and
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
        validMediaTypes = Arrays.asList("youtube");
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
            case "youtube": if (!isValidLicense(YouTubeDataAPIV3Service.getLicense(src)))
                                throw new ClientException(ContentErrorCodeConstants.INVALID_YOUTUBE_MEDIA.name(), ContentErrorMessageConstants.LICENSE_NOT_SUPPORTED);
                            break;
            default       :
        }
    }
}
