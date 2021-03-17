package org.sunbird.graph.engine.importtest;


public class GraphImportDataProvider {

	// @DataProvider(name="definitions")
    public static Object[][] getDefinitionData() {
        return new Object[][]{
                {"literacy", "taxonomy_definitions.json", "Literacy Subject Definition"},
                {"literacy", "game_definitions.json", "Literacy Game Definition"},
                {"literacy", "assessment_definitions.json", "Literacy Assessments Definition"},
                {"literacy", "worksheet_definitions.json", "Literacy Worksheet Definition"},
                {"literacy", "story_definitions.json", "Literacy Story Definition"},
                
                {"literacy_v2", "taxonomy_definitions.json", "Literacy V2 Subject Definition"},
                {"literacy_v2", "game_definitions.json", "Literacy V2 Game Definition"},
                {"literacy_v2", "assessment_definitions.json", "Literacy V2 Assessments Definition"},
                {"literacy_v2", "worksheet_definitions.json", "Literacy V2 Worksheet Definition"},
                {"literacy_v2", "story_definitions.json", "Literacy V2 Story Definition"},
                
                {"numeracy", "taxonomy_definitions.json", "Numeracy Subject Definition"},
                {"numeracy", "game_definitions.json", "Numeracy Game Definition"},
                {"numeracy", "assessment_definitions.json", "Numeracy Assessments Definition"},
                {"numeracy", "worksheet_definitions.json", "Numeracy Worksheet Definition"},
                {"numeracy", "story_definitions.json", "Numeracy Story Definition"},
       };
    }
    
	// @DataProvider(name="csvdata")
    public static Object[][] getCSVData() {
        return new Object[][]{
                {"literacy", "Literacy-GraphEngine.csv", "Literacy Subject CSV Data"},
                {"literacy", "LiteracyGames-GraphEngine.csv", "Literacy Game CSV Data"},
                
                {"literacy_v2", "Literacy-GraphEngine_V2.csv", "Literacy V2 CSV Data"},
                {"literacy_v2", "LiteracyGames-GraphEngine_V2.csv", "Literacy Game V2 CSV Data"},
                
                {"numeracy", "Numeracy-GraphEngine.csv", "Numeracy Subject CSV Data"},
                {"numeracy", "NumeracyGames-GraphEngine.csv", "Numeracy Game CSV Data"}
       };
    }

}
