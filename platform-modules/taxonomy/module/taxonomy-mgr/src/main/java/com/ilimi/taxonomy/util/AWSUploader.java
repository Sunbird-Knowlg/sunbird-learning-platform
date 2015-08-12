package com.ilimi.taxonomy.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.Aliases;
import com.amazonaws.services.cloudfront.model.AllowedMethods;
import com.amazonaws.services.cloudfront.model.CacheBehaviors;
import com.amazonaws.services.cloudfront.model.CachedMethods;
import com.amazonaws.services.cloudfront.model.CookiePreference;
import com.amazonaws.services.cloudfront.model.CreateDistributionRequest;
import com.amazonaws.services.cloudfront.model.CreateDistributionResult;
import com.amazonaws.services.cloudfront.model.DefaultCacheBehavior;
import com.amazonaws.services.cloudfront.model.Distribution;
import com.amazonaws.services.cloudfront.model.DistributionConfig;
import com.amazonaws.services.cloudfront.model.DistributionList;
import com.amazonaws.services.cloudfront.model.DistributionSummary;
import com.amazonaws.services.cloudfront.model.ForwardedValues;
import com.amazonaws.services.cloudfront.model.Headers;
import com.amazonaws.services.cloudfront.model.ListDistributionsRequest;
import com.amazonaws.services.cloudfront.model.ListDistributionsResult;
import com.amazonaws.services.cloudfront.model.LoggingConfig;
import com.amazonaws.services.cloudfront.model.Origin;
import com.amazonaws.services.cloudfront.model.Origins;
import com.amazonaws.services.cloudfront.model.PriceClass;
import com.amazonaws.services.cloudfront.model.S3OriginConfig;
import com.amazonaws.services.cloudfront.model.TrustedSigners;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * arguments 
 * - operation: createStorage, uploadFiles 
 * - storageName: name of the storage 
 * - file/folder path: applicable only for uploadFiles operation
 * - region name: optional, name of the AWS region
 * 
 * @author rayulu
 * 
 */
public class AWSUploader {

    private static List<String> messages = null;

    public static void main(String[] args) {
        messages = new LinkedList<String>();
        if (null == args || args.length < 2) {
            addLogMessage(ERROR, "Invalid arguments...", messages);
        } else {
            String operation = args[0];
            String storage = args[1];
            if (null == operation || (!operation.equals("createStorage") && !operation.equals("uploadFiles"))) {
                addLogMessage(ERROR, "Invalid operation name. Valid operations are 'createStorage' and 'uploadFiles'",
                        messages);
            } else {
                if (null == storage || storage.trim().length() == 0) {
                    addLogMessage(ERROR, "Invalid storage name. Storage name cannot be empty", messages);
                } else {
                    Region region = Region.getRegion(Regions.AP_SOUTHEAST_1);
                    if (operation.equals("createStorage")) {
                        if (args.length > 2) {
                            String regionName = args[2];
                            if (null != regionName && regionName.trim().length() > 0)
                                region = getRegionFromName(regionName);
                        }
                        createStorage(storage, region);
                        writeListToFile(messages);
                        System.out.println("DONE: Completed: " + operation);
                    } else if (operation.equals("uploadFiles")) {
                        if (args.length < 3) {
                            addLogMessage(ERROR, "Invalid number of arguments for uploadFiles operation", messages);
                        } else {
                            String path = args[2];
                            if (null == path || path.trim().length() == 0) {
                                addLogMessage(ERROR, "Invalid file path. File path cannot be empty", messages);
                            } else {
                                if (args.length > 3) {
                                    String regionName = args[3];
                                    if (null != regionName && regionName.trim().length() > 0)
                                        region = getRegionFromName(regionName);
                                }
                                Map<String, String> urls = uploadFiles(storage, path, region);
                                writeMapToFile(urls);
                                writeListToFile(messages);
                                System.out.println("DONE: Completed: " + operation);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a storage with the given name. This method creates an S3 bucket
     * and a Cloud Front Web distribution. Before creating, this method checks
     * if the storage already exists or not. The storage name can contain only
     * lower case letters, numbers and the hyphen (-). No other characters are
     * allowed in the name.
     * 
     * @param name
     *            - name of the storage
     * @param region
     *            - AWS region in which the storage needs to be created.
     */
    public static void createStorage(String name, Region region) {
        try {
            addLogMessage(INFO, "Creating storage: " + name, messages);
            AmazonS3 s3 = new AmazonS3Client();
            s3.setRegion(region);
            Bucket bucket = checkS3(s3, name);
            if (null == bucket) {
                bucket = createS3Bucket(s3, name);
            } else {
                addLogMessage(WARN, "S3 storage already exists with this name: " + name, messages);
            }
            AmazonCloudFrontClient cf = new AmazonCloudFrontClient();
            cf.setRegion(region);
            DistributionSummary dist = checkDistribution(cf, name);
            if (null == dist) {
                createCloudFrontDistribution(cf, name);
            } else {
                addLogMessage(WARN, "Cloud Front distribution already exists with this name: " + name, messages);
            }
        } catch (Exception e) {
            addLogMessage(ERROR, "Failed to create storage: " + e.getMessage(), messages);
        }
    }

    /**
     * Returns the status of the given storage. If the storage is not found,
     * this method returns a null value. If storage is found, this method
     * returns "Deployed" or "In Progress".
     * 
     * @param name
     *            - name of the storage
     * @param region
     *            - AWS region
     * @return - current status of the storage
     */
    public static String getStorageStatus(String name, Region region) {
        AmazonCloudFrontClient cf = new AmazonCloudFrontClient();
        cf.setRegion(region);
        DistributionSummary dist = checkDistribution(cf, name);
        if (null != dist) {
            return dist.getStatus();
        }
        addLogMessage(ERROR, "Storage is not found...", messages);
        return null;
    }

    /**
     * Uploads the files in the given path to AWS storage. If the path is a
     * file, only a single file is uploaded. If the path is a directory, all the
     * files in the directory are uploaded to AWS storage. This method will not
     * check the folders recursively under the given path. This method returns a
     * map with the file names as key and the AWS URL as the value.
     * 
     * @param storage
     *            - storage name to which the files should be uploaded.
     * @param path
     *            - file or folder path that needs to be uploaded
     * @param region
     *            - AWS region
     * @return - AWS URL for each file name
     */
    public static Map<String, String> uploadFiles(String storage, String path, Region region) {
        Map<String, String> urlMap = new HashMap<String, String>();
        try {
            File file = new File(path);
            if (file.exists()) {
                AmazonS3 s3 = new AmazonS3Client();
                s3.setRegion(region);
                AmazonCloudFrontClient cf = new AmazonCloudFrontClient();
                cf.setRegion(region);
                Bucket bucket = checkS3(s3, storage);
                DistributionSummary dist = checkDistribution(cf, storage);
                if (null == bucket || null == dist) {
                    addLogMessage(ERROR, "Storage is not created.. Please create the storage using createStorage...",
                            messages);
                } else {
                    if (file.isDirectory()) {
                        File[] files = file.listFiles();
                        if (null != files && files.length > 0) {
                            for (File f : files) {
                                if (f.isFile()) {
                                    try {
                                        String name = uploadFile(s3, storage, f);
                                        String url = getCloudFrontURL(dist, name);
                                        urlMap.put(name, url);
                                    } catch (Exception e) {
                                        addLogMessage(ERROR,
                                                "Failed to upload file '" + f.getName() + "' - " + e.getMessage(),
                                                messages);
                                    }

                                }
                            }
                        }
                    } else if (file.isFile()) {
                        try {
                            String name = uploadFile(s3, storage, file);
                            String url = getCloudFrontURL(dist, name);
                            urlMap.put(name, url);
                        } catch (Exception e) {
                            addLogMessage(ERROR, "Failed to upload file '" + file.getName() + "' - " + e.getMessage(),
                                    messages);
                        }
                    }
                }
            } else {
                addLogMessage(ERROR, "Invalid File Path", messages);
            }
        } catch (Exception e) {
            e.printStackTrace();
            addLogMessage(ERROR, "Failed to upload files: " + e.getMessage(), messages);
        }
        return urlMap;
    }

    private static Bucket checkS3(AmazonS3 s3, String bucketName) {
        Bucket bucket = null;
        List<Bucket> buckets = s3.listBuckets();
        if (null != buckets && buckets.size() > 0) {
            for (Bucket b : buckets) {
                if (b.getName().equalsIgnoreCase(bucketName)) {
                    bucket = b;
                    break;
                }
            }
        }
        return bucket;
    }

    private static DistributionSummary checkDistribution(AmazonCloudFrontClient cf, String distributionName) {
        DistributionSummary distribution = null;
        ListDistributionsResult res = cf.listDistributions(new ListDistributionsRequest());
        DistributionList dl = res.getDistributionList();
        for (DistributionSummary dist : dl.getItems()) {
            Origins origins = dist.getOrigins();
            if (null != origins && null != origins.getItems() && origins.getItems().size() > 0) {
                for (Origin origin : origins.getItems()) {
                    if (origin.getDomainName().equals(distributionName + ".s3.amazonaws.com")) {
                        distribution = dist;
                        break;
                    }
                }
            }
        }
        return distribution;
    }

    private static String uploadFile(AmazonS3 s3, String bucketName, File file) {
        String key = file.getName();
        addLogMessage(INFO, "Uploading File: " + key, messages);
        s3.putObject(new PutObjectRequest(bucketName, key, file));
        s3.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);
        addLogMessage(INFO, "File Uploaded: " + key, messages);
        return key;
    }

    private static String getCloudFrontURL(DistributionSummary dist, String name) {
        String domainName = dist.getDomainName();
        String url = "http://" + domainName + "/" + name;
        return url;
    }

    private static Bucket createS3Bucket(AmazonS3 s3, String bucketName) {
        addLogMessage(INFO, "Creating bucket " + bucketName, messages);
        Bucket bucket = s3.createBucket(bucketName);
        addLogMessage(INFO, "Bucket created " + bucketName, messages);
        return bucket;
    }

    private static Distribution createCloudFrontDistribution(AmazonCloudFrontClient cf, String distributionName) {
        addLogMessage(INFO, "Creating CloudFront Distribution " + distributionName, messages);
        DistributionConfig config = new DistributionConfig();
        config.setOrigins(getOrigins(distributionName));
        config.setPriceClass(PriceClass.PriceClass_All);
        config.setEnabled(true);
        config.setDefaultRootObject("");

        LoggingConfig log = new LoggingConfig();
        log.setEnabled(false);
        log.setBucket("");
        log.setIncludeCookies(false);
        log.setPrefix("");
        config.setLogging(log);

        Aliases alias = new Aliases();
        alias.setQuantity(0);
        config.setAliases(alias);

        CacheBehaviors cache = new CacheBehaviors();
        cache.setQuantity(0);
        config.setCacheBehaviors(cache);
        config.setComment(distributionName);
        config.setDefaultCacheBehavior(getDefaultCacheBehavior(distributionName));
        config.setCallerReference(distributionName);

        CreateDistributionRequest cdr = new CreateDistributionRequest(config);
        CreateDistributionResult distribution = cf.createDistribution(cdr);
        addLogMessage(INFO, "Distribution created " + distributionName, messages);
        return distribution.getDistribution();

    }

    private static Origins getOrigins(String distributionName) {
        Origins origins = new Origins();
        Origin origin = new Origin();
        S3OriginConfig s3OriginConfig = new S3OriginConfig();
        s3OriginConfig.setOriginAccessIdentity("");
        origin.setS3OriginConfig(s3OriginConfig);
        origin.setDomainName(distributionName + ".s3.amazonaws.com");
        origin.setId("S3-" + distributionName);
        List<Origin> items = new ArrayList<Origin>();
        items.add(origin);
        origins.setItems(items);
        origins.setQuantity(1);
        return origins;
    }

    private static DefaultCacheBehavior getDefaultCacheBehavior(String distributionName) {
        DefaultCacheBehavior dc = new DefaultCacheBehavior();
        AllowedMethods allowedMethods = new AllowedMethods();
        allowedMethods.setQuantity(2);
        List<String> dcMethods = new ArrayList<String>();
        dcMethods.add("GET");
        dcMethods.add("HEAD");
        allowedMethods.setItems(dcMethods);

        CachedMethods cachedMethods = new CachedMethods();
        cachedMethods.setQuantity(2);
        List<String> ccMethods = new ArrayList<String>();
        ccMethods.add("GET");
        ccMethods.add("HEAD");
        cachedMethods.setItems(ccMethods);
        allowedMethods.setCachedMethods(cachedMethods);
        dc.setAllowedMethods(allowedMethods);

        ForwardedValues forwardedValues = new ForwardedValues();
        Headers headers = new Headers();
        headers.setQuantity(0);
        forwardedValues.setHeaders(headers);
        forwardedValues.setQueryString(false);

        CookiePreference cookie = new CookiePreference();
        cookie.setForward("none");
        forwardedValues.setCookies(cookie);
        dc.setForwardedValues(forwardedValues);

        TrustedSigners ts = new TrustedSigners();
        ts.setEnabled(false);
        ts.setQuantity(0);
        dc.setTrustedSigners(ts);
        dc.setViewerProtocolPolicy("allow-all");
        dc.setMinTTL((long) 0);
        dc.setSmoothStreaming(false);
        dc.setTargetOriginId("S3-" + distributionName);

        return dc;
    }

    private static Region getRegionFromName(String name) {
        Region region = getRegion(name);
        if (null == region) {
            addLogMessage(WARN, "Region name is invalid. Using the default region: " + Regions.AP_SOUTHEAST_1, messages);
            region = Region.getRegion(Regions.AP_SOUTHEAST_1);
        }
        return region;
    }

    private static Region getRegion(String regionName) {
        Region region = null;
        try {
            Regions enumValue = Regions.fromName(regionName);
            region = Region.getRegion(enumValue);
        } catch (Exception e) {
        }
        return region;
    }

    private static final String INFO = "INFO: ";
    private static final String WARN = "WARN: ";
    private static final String ERROR = "ERROR: ";

    private static void addLogMessage(String level, String message, List<String> messages) {
        if (null != message && message.trim().length() > 0) {
            if (null == level || level.trim().length() == 0)
                level = INFO;
            System.out.println(level + message + "\n");
            messages.add(level + message);
        }
    }

    private static void writeMapToFile(Map<String, String> dictionary) {
        if (null != dictionary && dictionary.size() > 0) {
            BufferedWriter writer = null;
            try {
                String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                String name = "uploadFiles-" + time + ".txt";
                File dictionaryFile = new File(name);
                writer = new BufferedWriter(new FileWriter(dictionaryFile));
                Iterator<String> it = dictionary.keySet().iterator();
                while (it.hasNext()) {
                    String line = it.next();
                    String entryLine = line + " -> " + dictionary.get(line) + "\n";
                    writer.write(entryLine);
                }
                System.out.println("INFO: Uploaded files URL output file: " + name);
            } catch (Exception e) {
                System.out.println(ERROR + "Failed to write Map to file: " + e.getMessage());
            } finally {
                if (null != writer) {
                    try {
                        writer.close();
                    } catch (Exception e) {
                        System.out.println(ERROR + e.getMessage());
                    }
                }
            }
        }
    }

    private static void writeListToFile(List<String> messages) {
        if (null != messages && messages.size() > 0) {
            BufferedWriter writer = null;
            try {
                String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                String name = "logMessages-" + time + ".txt";
                File dictionaryFile = new File(name);
                writer = new BufferedWriter(new FileWriter(dictionaryFile));
                Iterator<String> it = messages.iterator();
                while (it.hasNext()) {
                    String line = it.next();
                    String entryLine = line + "\n";
                    writer.write(entryLine);
                }
            } catch (Exception e) {
                System.out.println(ERROR + "Failed to write List to file: " + e.getMessage());
            } finally {
                if (null != writer) {
                    try {
                        writer.close();
                    } catch (Exception e) {
                        System.out.println(ERROR + e.getMessage());
                    }
                }
            }
        }
    }
}
