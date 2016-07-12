package org.ekstep.language.mgr;

import java.io.InputStream;
import com.ilimi.common.dto.Response;

public interface IImportManager {

    Response transformData(String languageId, String sourceId, InputStream file);
    
    Response importData(String languageId, InputStream synsetStream, InputStream wordStream);
    
    Response importJSON(String languageId, InputStream synsetsStreamInZIP);
    
    Response importCSV(String languageId, InputStream file);
    
    Response updateDefinition(String languageId, String json);
    
    Response findAllDefinitions(String id);
}