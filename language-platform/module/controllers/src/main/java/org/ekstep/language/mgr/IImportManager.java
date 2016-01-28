package org.ekstep.language.mgr;

import java.io.InputStream;
import com.ilimi.common.dto.Response;

public interface IImportManager {

    Response importData(String languageId, String sourceId, InputStream file);
    
    Response enrich(String languageId, InputStream synsetStream, InputStream wordStream);
}