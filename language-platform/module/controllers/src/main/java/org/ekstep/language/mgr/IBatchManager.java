package org.ekstep.language.mgr;

import com.ilimi.common.dto.Response;

public interface IBatchManager {

    Response updateWordFeatures(String languageId);
    
    Response updateFrequencyCounts(String languageId);
}
