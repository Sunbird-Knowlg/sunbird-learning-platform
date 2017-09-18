/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import org.ekstep.tools.loader.utils.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.ekstep.tools.loader.functions.LookupFunction;
import org.ekstep.tools.loader.functions.SluggifyFunction;
import org.ekstep.tools.loader.functions.SplitFunction;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;
import org.apache.logging.log4j.Logger;
import org.jtwig.value.convert.string.StringConverter;

/**
 *
 * @author feroz
 */
public class JsonMapperProcessor implements Processor {
    
    static Logger logger = LogManager.getLogger(JsonMapperProcessor.class);
    File templateFile = null;
    JtwigTemplate template = null;
    LookupFunction lookupFn = null;
    
    public JsonMapperProcessor(File file) {
        this.templateFile = file;
    }
    
    @Override
    public List<Record> process(List<Record> data, ProgressCallback callback) throws Exception {
        loadMappings(templateFile);
        return transform(data, callback);
    }
            
    public void loadMappings(File file) {
        
        // Nested functions in the transformation context
        lookupFn = new LookupFunction();
        SplitFunction splitFn = new SplitFunction();
        SluggifyFunction slugFn = new SluggifyFunction();
        
        StringConverter conv = new StringConverter() {
            @Override
            public String convert(Object o) {
                String retVal = "null";
                if (o != null) {
                    if (StringUtils.isNotBlank(o.toString())) {
                        retVal = JsonUtil.escape(o.toString());
                    }
                }
                return retVal;
            }
        };
        
        EnvironmentConfiguration configuration = EnvironmentConfigurationBuilder.configuration()
                .value().withStringConverter(conv)
                .and()
                .functions().add(lookupFn).add(splitFn).add(slugFn)
                .and().build();
        
        template = JtwigTemplate.fileTemplate(file, configuration);
        logger.info("Loaded transformation from the mapping file " + file.getName());
    }
    
    
 
    public void addLookupTable(String key, Map<String, String> lookupData) {
        lookupFn.addLookupTable(key, lookupData);
        logger.info("Added lookups for transformation: " + key);
    }
    
    public List<Record> transform(List<Record> rawData, ProgressCallback callback) {
        
        long begin = System.currentTimeMillis();
        
        List<Record> output = new ArrayList<>();
        int rowNum = 0;
        int totalRows = rawData.size();
        
        for (Record row : rawData) {
            rowNum++;
            
            JsonObject rowOutput = transformRow(row.getCsvData(), (rowNum + 1));
            if (rowOutput != null) {
                row.setJsonData(rowOutput);
                output.add(row);
            }
            
            callback.progress(totalRows, rowNum);
        }
        
        long end = System.currentTimeMillis();
        logger.info("Transformation completed, processed " + totalRows + " records in " + (end - begin) + " ms");
        return output;
    }
    
    /**
     * Transforms a given row of CSV data into the output JSON. The transformation
     * is a two step process - first is a template subsctitution to convert the values
     * into output format, second is a HOCON parse on the data. The second step is 
     * important because it simplifies the transformation that is written. HOCON for all
     * practical purposes is a very simple format to encode JSON data. 
     * 
     * @param row Row of CSV data
     * @return JSON parsed after converting the CSV
     */
    public JsonObject transformRow(Map<String, String> row, int index) {
        JsonObject result = null;
        
        try {
            // Initialize the transformation model with the row data
            JtwigModel model = JtwigModel.newModel();
            for (String key : row.keySet()) {
                model.with(key, row.get(key));
            }

            // Step 1 - Transform using Jtwig Template
            String configStr = template.render(model);
            logger.debug("Row " + index + " mappings complete");

            // Step 2 - Parse using HOCON spec
            Config conf = ConfigFactory.parseString(configStr);
            logger.debug("Row " + index + " converted to JSON");

            // Step 3 - Convert to an in-memory JSON object
            ConfigRenderOptions options = ConfigRenderOptions.concise().setFormatted(true).setJson(true);
            String json = conf.root().render(options);
            JsonParser parser = new JsonParser();
            result = parser.parse(json).getAsJsonObject();
            logger.debug("Row " + index + " parsing as JSON completed");

        } catch (Exception ex) {
            logger.warn("Row " + index + " failed to transform. This indicates a problem with the transformation file.");
            logger.warn("    " + ex.getMessage());
        }
        
        return result;
    }
}
