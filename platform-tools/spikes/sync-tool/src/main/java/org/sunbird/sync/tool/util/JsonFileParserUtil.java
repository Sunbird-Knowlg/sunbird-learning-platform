package org.sunbird.sync.tool.util;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * @author gauraw
 */
public class JsonFileParserUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * @param filePath
     * @return
     */
    public static List<String> getIdentifiers(String filePath) throws Exception{
        String jsonFilePath = extractFile(filePath);
        if(StringUtils.isBlank(jsonFilePath))
            throw new Exception("Something Went Wrong While Extracting File.");
        List<Map<String, Object>> events = new ArrayList<>();
        List<String> identifiers = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(jsonFilePath))) {
            stream.forEach(line -> {
                try {
                    events.add(mapper.readValue(line, new TypeReference<Map<String, Object>>() {
                    }));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Map<String, Object> event : events) {
            identifiers.add((String) event.get("nodeUniqueId"));
        }
        return identifiers;
    }

    /**
     * @param filePath
     * @return
     */
    public static String extractFile(String filePath) {
        boolean isExtracted = false;
        String inputFileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        String outputFileName = inputFileName.replace(".gz", "");
        String basePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        String extractedFilePath = basePath + outputFileName;

        byte[] buffer = new byte[1024];

        try {
            GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(filePath));
            FileOutputStream outputStream = new FileOutputStream(extractedFilePath);
            int len;
            while ((len = gzipInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            gzipInputStream.close();
            outputStream.close();
            isExtracted = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (isExtracted)
            return extractedFilePath;
        else return null;
    }

}
