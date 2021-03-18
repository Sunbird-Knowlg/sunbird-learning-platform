package org.sunbird.content.common;

/**
 * The Class ContentConfigurationConstants holds all the Constants of Content
 * Work-flow Pipeline.
 * 
 * @author Mohammad Azharuddin
 * 
 */
public class ContentConfigurationConstants {

	/**
	 * The Constant CONTENT_BASE_PATH is the disk location on server where all
	 * the File related operations will be carried out.
	 */
	public static final String CONTENT_BASE_PATH = "/data/contentBundle/";

	/**
	 * The Constant CONTENT_BUNDLE_BASE_PATH is the disk location which is used
	 * to download, accumulate all the artifacts and creating ECAR for bundling.
	 */
	public static final String CONTENT_BUNDLE_BASE_PATH = "/data/contentBundle/";

	/**
	 * The Constant CONTENT_BUNDLE_MANIFEST_FILE_NAME is the default name for
	 * Content Manifest JSON File.
	 */
	public static final String CONTENT_BUNDLE_MANIFEST_FILE_NAME = "manifest.json";

	/** The Constant FOLDER_NAME is the name of AWS Folder. */
	public static final String FOLDER_NAME = "content";

	/**
	 * The Constant ITEM_CONTROLLER_FILE_EXTENSION is the default File Extension
	 * for Item Controller Files.
	 */
	public static final String ITEM_CONTROLLER_FILE_EXTENSION = ".json";

	/** The Constant URL_PATH_SEPERATOR the default URL Path separator. */
	public static final String URL_PATH_SEPERATOR = "/";

	/**
	 * The Constant FILENAME_EXTENSION_SEPERATOR is the default File Name and
	 * Extension Separator.
	 */
	public static final String FILENAME_EXTENSION_SEPERATOR = ".";

	/**
	 * The Constant DEFAULT_CONTENT_MANIFEST_VERSION is the default version of
	 * Manifest File which is visible in Manifest File Header.
	 */
	public static final String DEFAULT_CONTENT_MANIFEST_VERSION = "1.2";
	/**
	 * The Constant DEFAULT_CONTENT_HIERARCHY_VERSION is the default version of
	 * Hierarchy File which is visible in Hierarchy File Header.
	 */
	public static final String DEFAULT_CONTENT_HIERARCHY_VERSION = "1.0";


	/**
	 * The Constant DEFAULT_ZIP_EXTENSION is the default ZIP package file
	 * extension
	 */
	public static final String DEFAULT_ZIP_EXTENSION = "zip";

	/**
	 * The Constant DEFAULT_CONTENT_OWNER is the default value of Content Owner.
	 */
	public static final String DEFAULT_CONTENT_OWNER = "EkStep";

	/** The Constant DEFAULT_ECML_FILE_NAME is the default name of ECML File. */
	public static final String DEFAULT_ECML_FILE_NAME = "index";

	/** The Constant DEFAULT_CONTENT_BODY is the default content body. */
	public static final String DEFAULT_CONTENT_BODY = "<content></content>";

	/**
	 * The Constant DEFAULT_CONTENT_CODE_PREFIX is the default prefix of code
	 * property of Content Node.
	 */
	public static final String DEFAULT_CONTENT_CODE_PREFIX = "org.sunbird.content.";

	/**
	 * The Constant DEFAULT_ASSESSMENT_ITEM_CODE_PREFIX is the default prefix of
	 * code property of Assessment Item Node.
	 */
	public static final String DEFAULT_ASSESSMENT_ITEM_CODE_PREFIX = "org.sunbird.assessment.item.";

	/**
	 * The Constant DEFAULT_ASSESSMENT_ITEM_SET_CODE_PREFIX is the default
	 * prefix of code property of Item Set Node.
	 */
	public static final String DEFAULT_ASSESSMENT_ITEM_SET_CODE_PREFIX = "org.sunbird.assessment.item.set.";

	/**
	 * The Constant DEFAULT_ECAR_EXTENSION is the default ECAR File Extension.
	 */
	public static final String DEFAULT_ECAR_EXTENSION = "ecar";
	
	public static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";
	
	public static final String CONTENT_IMAGE_OBJECT_TYPE = "ContentImage";
	
	public static final String DEFAULT_H5P_JSON_FILE_LOCATION = "h5p.json";
	
	public static final String DEFAULT_H5P_LIBRARY_PATH_PROPERTY_KEY = "content.h5p.library.path";
	
	/**
	 * The Constant AWS_UPLOAD_RESULT_URL_INDEX is value of array index where
	 * the S3 URL is loaded.
	 */
	public static final int AWS_UPLOAD_RESULT_URL_INDEX = 1;

	/**
	 * The Constant DEFAULT_CONTENT_PACKAGE_VERSION is the default version of
	 * Content Package.
	 */
	public static final int DEFAULT_CONTENT_PACKAGE_VERSION = 1;

	/**
	 * The Constant DEFAULT_CONTENT_BUNDLE_EXPIRES_IN_DAYS is the no. of days in
	 * which the Content Bundle Package Expires to load by App.
	 */
	public static final int DEFAULT_CONTENT_BUNDLE_EXPIRES_IN_DAYS = 7;
	
	public static final boolean IS_ECAR_EXTRACTION_ENABLED = true;
	
	public static final boolean IS_CONTENT_PACKAGE_EXTRACTION_ENABLED = true;

	public static final String CONTENT_BUNDLE_HIERARCHY_FILE_NAME = "hierarchy.json";


	/**
	 * Instantiates a new content configuration constants, It is being used in a
	 * way that the Class cannot be instantiated.
	 */
	private ContentConfigurationConstants() {
		throw new AssertionError();
	}

}
