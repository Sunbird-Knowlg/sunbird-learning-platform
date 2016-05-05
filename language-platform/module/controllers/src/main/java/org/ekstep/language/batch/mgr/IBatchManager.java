package org.ekstep.language.batch.mgr;

import com.ilimi.common.dto.Response;

public interface IBatchManager {
    
    Response updatePictures(String languageId);
    
    Response updatePosList(String languageId);

    Response updateWordFeatures(String languageId);
    
    Response updateFrequencyCounts(String languageId);
}
