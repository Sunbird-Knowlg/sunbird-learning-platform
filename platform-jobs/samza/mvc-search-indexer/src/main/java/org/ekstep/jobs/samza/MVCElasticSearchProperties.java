package org.ekstep.jobs.samza;

public class MVCElasticSearchProperties {
    public static String[] propertyArray = {"organisation","channel","framework","board","medium","subject","gradeLevel","name","description","language","appId","appIcon","appIconLabel",
            "contentEncoding","identifier","node_id","nodeType","mimeType","resourceType","contentType","allowedContentTypes","objectType","posterImage","artifactUrl","launchUrl","previewUrl","streamingUrl","downloadUrl","status","pkgVersion","source","lastUpdatedOn","ml_contentText","ml_contentTextVector",
            "ml_Keywords","ml_level1Concept","ml_level2Concept","ml_level3Concept","label","all_fields"};
    public static String api = "https://dock.sunbirded.org/api/content/v1/read/";
}
