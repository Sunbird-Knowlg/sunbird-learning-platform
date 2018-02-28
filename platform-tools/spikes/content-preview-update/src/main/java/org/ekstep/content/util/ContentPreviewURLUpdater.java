package org.ekstep.content.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.graph.util.NodeUtil;
import org.ekstep.graph.util.SearchUtil;

public class ContentPreviewURLUpdater {

	public static String path = "";
	public static String aws_bucket = "";
	private static final String tempFileLocation = "/data/contentUpdate/";
	private static int defaultBatchSize = 100;
	private ContentPackageExtractionUtil extractionUtil = new ContentPackageExtractionUtil();

	public static void main(String args[]) throws Exception {
		if (args != null && args.length < 3)
			throw new Exception("provide neo4j_path followed by aws_bucket and content_type in arguments to proceed");
		path = args[0];
		aws_bucket = args[1];
		String contentType = args[2];
		String size = args[3];
		if (StringUtils.isBlank(path))
			throw new Exception("Invalid neo4j path.");
		if (StringUtils.isBlank(aws_bucket))
			throw new Exception("Invalid AWS bucket.");
		if (StringUtils.isBlank(contentType)
				&& (!StringUtils.equalsIgnoreCase(contentType, ContentUpdateType.NonExtractable.name())
						|| !StringUtils.equalsIgnoreCase(contentType, ContentUpdateType.Extractable.name())))
			throw new Exception("Invalid Content type");
		System.out.println("Neo4j path : " + path + ", AWS bucket :" + aws_bucket);
		ContentPreviewURLUpdater updater = new ContentPreviewURLUpdater();
		int batchSize = 0;
		if(StringUtils.isNotBlank(size))
			batchSize = Integer.parseInt(size);
		// update Non-Extractable content(like video/Document) previewUrl
		// update Extractable content(like ecml/html/h5p) previewUrl  
		updater.batchUpdate(ContentUpdateType.valueOf(contentType), batchSize);

	}

	private void batchUpdate(ContentUpdateType type, int batchSize) {
		int startPosition = 0;
		if(batchSize==0)
			batchSize=defaultBatchSize;
		int count = SearchUtil.getNodesCount(path, type);
		System.out.println(count+" contents are found for the type "+ type.name());
		List<Map<String, Object>> nodes = SearchUtil.getNodes(path, startPosition, batchSize, type);
		if (nodes.size() > 0 && !nodes.isEmpty()) {
			System.out.println("updating " + nodes.size() + " nodes for type : " + type.name());
			updateContent(nodes, type);
		} 
		System.out.println("Migration done for size -"+batchSize);
	}

	private void updateContent(List<Map<String, Object>> nodes, ContentUpdateType type) {

		if (type == ContentUpdateType.Extractable) {
			for (Map<String, Object> node : nodes) {
				String contentId = (String) node.get(ContentPreveiwUpdaterParams.IL_UNIQUE_ID.name());
				String artifactUrl = (String) node.get(ContentPreveiwUpdaterParams.artifactUrl.name());
				String previewUrl = "";
				if (StringUtils.isNotBlank(contentId) && StringUtils.isNotBlank(artifactUrl)) {
					String awsFolderPath = extractionUtil.getExtractionPath(contentId, node);
					if (AWSUploader.checkAwsFolderExists(awsFolderPath)) {
						// get latest url
						previewUrl = AWSUploader.getURL(awsFolderPath);
						//System.out.println("latest url already there" + previewUrl);
					} else {
						String downloadPath = getBasePath(contentId);
						try {
							// download ecar file and extract it in local
							extractionUtil.downloadAndExtract(artifactUrl, downloadPath);
							// upload extracted ecar as latest folder in AWS
							extractionUtil.uploadExtractedPackage(awsFolderPath, downloadPath, true);
							// get latest url
							if (AWSUploader.checkAwsFolderExists(awsFolderPath)) {
								// get latest url
								previewUrl = AWSUploader.getURL(awsFolderPath);
							}
							System.out.println("content id: " + contentId + ", Generated preview url: " + previewUrl);
						} catch (Exception e) {
							System.err.println("skipped! error while processing content, id :" + contentId);
						} finally {
							File contentFolder = new File(downloadPath);
							if (contentFolder.exists()) {
								try {
									FileUtils.deleteDirectory(contentFolder);
								} catch (Exception e) {
									System.err.println("unable to delete directory" + downloadPath);
								}
							}
						}
					}

					// update previewUrl for this content
					if (StringUtils.isNotBlank(previewUrl)) {
						Map<String, Object> content = new HashMap<>();
						content.put(ContentPreveiwUpdaterParams.previewUrl.name(), previewUrl);
						NodeUtil.updateNode(path, contentId, content);
					}

				}
			}
		} else if (type == ContentUpdateType.NonExtractable) {
			for (Map<String, Object> node : nodes) {
				String contentId = (String) node.get(ContentPreveiwUpdaterParams.IL_UNIQUE_ID.name());
				String artifactUrl = (String) node.get(ContentPreveiwUpdaterParams.artifactUrl.name());
				if (StringUtils.isNotBlank(contentId) && StringUtils.isNotBlank(artifactUrl)) {
					Map<String, Object> content = new HashMap<>();
					content.put(ContentPreveiwUpdaterParams.previewUrl.name(), artifactUrl);
					NodeUtil.updateNode(path, contentId, content);
				}
			}
		}
	}

	private static String getBasePath(String contentId) {
		return tempFileLocation + File.separator + System.currentTimeMillis() + ContentPreveiwUpdaterParams._temp.name()
				+ File.separator + contentId;
	}

}
