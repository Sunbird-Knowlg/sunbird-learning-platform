package org.sunbird.platform.importdata;

import org.testng.annotations.DataProvider;

public class GraphTestDataProvider {

    @DataProvider(name="definitions")
    public static Object[][] getDefinitionData() {
        return new Object[][]{
                {"domain", "domain_definition_v2.json", "Domain Definition"},
                {"domain", "dimension_definition_v2.json", "Dimension Definition"},
                {"domain", "concept_definition_v2.json", "Concept Definition"}, 
                {"domain", "method_definition_v2.json", "Method Definition"},
                {"domain", "misconception_definition_v2.json", "Misconception Definition"}, 
                {"domain", "content_definition_v2.json", "Content Definition"},
                
                
       };
    }
    
    @DataProvider(name="csvdata")
    public static Object[][] getCSVData() {
        return new Object[][]{
                {"domain", "literacy_domain.csv", "Literacy CSV Data"},
                {"domain", "numeracy_domain.csv", "NUmeracy CSV Data"},
                
               
       };
    }

}
