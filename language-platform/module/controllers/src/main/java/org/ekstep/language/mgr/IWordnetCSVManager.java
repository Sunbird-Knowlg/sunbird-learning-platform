package org.ekstep.language.mgr;

import com.ilimi.common.dto.Response;

public interface IWordnetCSVManager {
    
    Response createWordnetCitations(String languageId, String wordsCSV);
    
    Response addWordnetIndexes(String languageId, String wordsCSV);
    
    Response replaceWordnetIds(String languageId, String wordsCSV, String synsetCSV, String outputDir);
}
