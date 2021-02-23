package org.ekstep.jobs.samza.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.model.QRCodeGenerationRequest;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.QRCodeImageGeneratorParams;
import org.ekstep.jobs.samza.util.QRCodeImageGeneratorUtil;
import org.ekstep.jobs.samza.util.QRCodeCassandraConnector;
import org.ekstep.jobs.samza.util.CloudStorageUtil;
import org.ekstep.jobs.samza.util.ZipEditorUtil;
import org.ekstep.jobs.samza.service.task.JobMetrics;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class QRCodeImageGeneratorService implements ISamzaService {

	private JobLogger LOGGER = new JobLogger(QRCodeImageGeneratorService.class);

	private Config appConfig = null;

	@Override
	public void initialize(Config config) throws Exception {
		JSONUtils.loadProperties(config);
		appConfig = config;
		LOGGER.info("QRCodeImageGeneratorService:initialize: Service config initialized");
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
        List<File> availableImages = new ArrayList<File>();
        File zipFile = null;
	    try{
            LOGGER.info("QRCodeImageGeneratorService:processMessage: Processing request: "+message);
            LOGGER.info("QRCodeImageGeneratorService:processMessage: Starting message processing at "+System.currentTimeMillis());

            if(!message.containsKey(QRCodeImageGeneratorParams.eid.name())) {
                return;
            }

            String eid = (String) message.get(QRCodeImageGeneratorParams.eid.name());
            if(!eid.equalsIgnoreCase(QRCodeImageGeneratorParams.BE_QR_IMAGE_GENERATOR.name())) {
                return;
            }

            List<Map<String,Object>> dialCodes = (List<Map<String,Object>>) message.get(QRCodeImageGeneratorParams.dialcodes.name());
            if(null == dialCodes || dialCodes.size()==0) {
                return;
            }

            Map<String, Object> config = (Map<String, Object>) message.get(QRCodeImageGeneratorParams.config.name());
            String imageFormat = (String) config.get(QRCodeImageGeneratorParams.imageFormat.name());

            List<String> dataList = new ArrayList<String>();
            List<String> textList = new ArrayList<String>();
            List<String> fileNameList = new ArrayList<String>();
            String downloadUrl = null;
            String tempFilePath = appConfig.getOrDefault(QRCodeImageGeneratorParams.lp_tempfile_location.name(), "/tmp");

            for(Map<String, Object> dialCode : dialCodes) {
                if(dialCode.containsKey(QRCodeImageGeneratorParams.location.name())) {
                    try {
                        downloadUrl = (String) dialCode.get(QRCodeImageGeneratorParams.location.name());
                        String fileName = (String) dialCode.get(QRCodeImageGeneratorParams.id.name());
                        File fileToSave = new File(tempFilePath + File.separator + fileName+"."+imageFormat);
                        LOGGER.info("QRCodeImageGeneratorService:processMessage: creating file - " + fileToSave.getAbsolutePath());
                        fileToSave.createNewFile();
                        LOGGER.info("QRCodeImageGeneratorService:processMessage: created file - " + fileToSave.getAbsolutePath());
                        CloudStorageUtil.downloadFile(downloadUrl, fileToSave);
                        availableImages.add(fileToSave);
                        continue;
                    } catch(Exception e) {
                        LOGGER.error("QRCodeImageGeneratorService:processMessage: Error while downloading image:", downloadUrl, e);
                    }
                }

                dataList.add((String)dialCode.get(QRCodeImageGeneratorParams.data.name()));
                textList.add((String)dialCode.get(QRCodeImageGeneratorParams.text.name()));
                fileNameList.add((String)dialCode.get(QRCodeImageGeneratorParams.id.name()));

            }

            Map<String, String> storage = (Map<String, String>) message.get(QRCodeImageGeneratorParams.storage.name());
            String container = storage.get(QRCodeImageGeneratorParams.container.name());
            String path = storage.get(QRCodeImageGeneratorParams.path.name());
            String zipFileName = storage.get(QRCodeImageGeneratorParams.fileName.name());
            String processId = (String) message.get(QRCodeImageGeneratorParams.processId.name());

            QRCodeGenerationRequest qrGenRequest = getQRCodeGenerationRequest(config, dataList, textList, fileNameList);
            List<File> generatedImages = QRCodeImageGeneratorUtil.createQRImages(qrGenRequest, appConfig, container, path);

            if(!StringUtils.isBlank(processId)) {
                LOGGER.info("QRCodeImageGeneratorService:processMessage: Generating zip for QR codes with processId " + processId);
                if(StringUtils.isBlank(zipFileName)) {
                    zipFileName = processId;
                }
                availableImages.addAll(generatedImages);
                zipFile = ZipEditorUtil.zipFiles(availableImages, zipFileName, tempFilePath);

                String zipDownloadUrl = CloudStorageUtil.uploadFile(container, path, zipFile, false);
                QRCodeCassandraConnector.updateDownloadZIPUrl(processId, zipDownloadUrl);
            } else {
                LOGGER.info("QRCodeImageGeneratorService:processMessage: Skipping zip creation due to missing processId.");
            }
            LOGGER.info("QRCodeImageGeneratorService:processMessage: Message processed successfully at "+System.currentTimeMillis());
        } catch (Exception e) {
            QRCodeCassandraConnector.updateFailure((String) message.get(QRCodeImageGeneratorParams.processId.name()),
                    e.getMessage());
            throw e;
        } finally {
            if(null != zipFile) {
                zipFile.delete();
            }
            for(File imageFile : availableImages) {
                if(null != imageFile) {
                    imageFile.delete();
                }
            }
        }
	}

	private QRCodeGenerationRequest getQRCodeGenerationRequest(Map<String, Object> config, List<String> dataList, List<String> textList, List<String> fileNameList) {
		QRCodeGenerationRequest qrGenRequest = new QRCodeGenerationRequest();
		qrGenRequest.setData(dataList);
		qrGenRequest.setText(textList);
		qrGenRequest.setFileName(fileNameList);
		qrGenRequest.setErrorCorrectionLevel((String) config.get(QRCodeImageGeneratorParams.errorCorrectionLevel.name()));
		qrGenRequest.setPixelsPerBlock((Integer) config.get(QRCodeImageGeneratorParams.pixelsPerBlock.name()));
		qrGenRequest.setQrCodeMargin((Integer) config.get(QRCodeImageGeneratorParams.qrCodeMargin.name()));
		qrGenRequest.setTextFontName((String) config.get(QRCodeImageGeneratorParams.textFontName.name()));
		qrGenRequest.setTextFontSize((Integer) config.get(QRCodeImageGeneratorParams.textFontSize.name()));
		qrGenRequest.setTextCharacterSpacing((Double) config.get(QRCodeImageGeneratorParams.textCharacterSpacing.name()));
		qrGenRequest.setFileFormat((String) config.get(QRCodeImageGeneratorParams.imageFormat.name()));
		qrGenRequest.setColorModel((String) config.get(QRCodeImageGeneratorParams.colourModel.name()));
		qrGenRequest.setImageBorderSize((Integer) config.get(QRCodeImageGeneratorParams.imageBorderSize.name()));
		if(config.containsKey(QRCodeImageGeneratorParams.qrCodeMarginBottom.name())) {
            qrGenRequest.setQrCodeMarginBottom((Integer) config.get(QRCodeImageGeneratorParams.qrCodeMarginBottom.name()));
        } else {
            qrGenRequest.setQrCodeMarginBottom(appConfig.getInt(QRCodeImageGeneratorParams.qr_image_margin_bottom.name()));
        }
        if(config.containsKey(QRCodeImageGeneratorParams.imageMargin.name())) {
            qrGenRequest.setImageMargin((Integer) config.get(QRCodeImageGeneratorParams.imageMargin.name()));
        } else {
            qrGenRequest.setImageMargin(appConfig.getInt(QRCodeImageGeneratorParams.qr_image_margin.name()));
        }
        qrGenRequest.setTempFilePath(appConfig.getOrDefault(QRCodeImageGeneratorParams.lp_tempfile_location.name(), "/tmp"));
		return qrGenRequest;
	}
}
