package org.ekstep.platform.importdata;

import org.testng.annotations.DataProvider;

public class GraphTestDataProvider {

    @DataProvider(name="definitions")
    public static Object[][] getDefinitionData() {
        return new Object[][]{
                {"domain", "domain_definition_v2.json", "Literacy Subject Definition"},
                {"domain", "dimension_definition_v2.json", "Literacy Game Definition"},
                {"domain", "concept_definition_v2.json", "Literacy Assessments Definition"}             
                
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
