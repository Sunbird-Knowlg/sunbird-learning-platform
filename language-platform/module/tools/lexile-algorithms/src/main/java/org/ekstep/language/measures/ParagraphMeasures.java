package org.ekstep.language.measures;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.cache.GradeComplexityCache;
import org.ekstep.language.measures.entity.ComplexityMeasures;
import org.ekstep.language.measures.entity.ParagraphComplexity;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.measures.meta.SyllableMap;
import org.ekstep.language.util.IWordnetConstants;
import org.ekstep.language.util.LanguageUtil;
import org.ekstep.language.util.WordnetUtil;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;

public class ParagraphMeasures {

    static {
        SyllableMap.loadSyllables("te");
        SyllableMap.loadSyllables("hi");
        SyllableMap.loadSyllables("ka");
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map<String, Object> analyseTexts(String languageId, Map<String, String> texts) {
        try {
            if (!SyllableMap.isLanguageEnabled(languageId) || null == texts || texts.isEmpty())
                return null;
            
            Map<String, Map<String, Object>> summaryMap = new HashMap<String, Map<String, Object>>();

            Map<String, String> posMap = new HashMap<String, String>();
            Map<String, String> rootWordMap = new HashMap<String, String>();
            Map<String, String> categoryMap = new HashMap<String, String>();
            Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
            Map<String, Integer> storyCountMap = new HashMap<String, Integer>();
            Map<String, ComplexityMeasures> complexityMap = new HashMap<String, ComplexityMeasures>();
            Map<String, Map> wordRowMap = new HashMap<String, Map>();
            
            for (Entry<String, String> textEntry : texts.entrySet()) {
                String filename = textEntry.getKey();
                String s = textEntry.getValue();
                ParagraphComplexity pc = getTextComplexity(languageId, s);
                System.out.println(
                        filename + ": " + pc.getMeanOrthoComplexity() + ", " + pc.getMeanPhonicComplexity());
                Map<String, Object> summary = new HashMap<String, Object>();
                summary.put("total_orthographic_complexity", pc.getTotalOrthoComplexity());
                summary.put("total_phonologic_complexity", pc.getTotalPhonicComplexity());
                summary.put("avg_orthographic_complexity", pc.getMeanOrthoComplexity());
                summary.put("avg_phonologic_complexity", pc.getMeanPhonicComplexity());
                summary.put("word_count", pc.getWordCount());
                summary.put("syllable_count", pc.getSyllableCount());
                summary.put("averageTotalComplexity", pc.getMeanComplexity());
                summary.put("gradeSuitable", getSuitableGradeSummaryInfo(languageId, pc.getMeanComplexity()));
                
                Map<String, Integer> wordFrequency = pc.getWordFrequency();
                Map<String, ComplexityMeasures> wordMeasures = pc.getWordMeasures();
                ComplexityMeasuresComparator comparator = new ComplexityMeasuresComparator(wordMeasures);
                Map<String, ComplexityMeasures> sortedMap = new TreeMap<String,ComplexityMeasures>(comparator);
                sortedMap.putAll(wordMeasures);
                complexityMap.putAll(wordMeasures);
                
                double totalWordComplexity = 0;
                Map<String, Map<String, Object>> wordDetailsMap = new HashMap<String, Map<String, Object>>();
                for (Entry<String, ComplexityMeasures> entry : sortedMap.entrySet()) {
                    String pos = posMap.get(entry.getKey());
                    String lemma = rootWordMap.get(entry.getKey());
                    String category = categoryMap.get(entry.getKey());
                    String thresholdLevel = null;
                    double oc = entry.getValue().getOrthographic_complexity();
                    double phc = entry.getValue().getPhonologic_complexity();
                    Double wc = pc.getWordComplexityMap().get(entry.getKey());
                    if (StringUtils.isBlank(lemma)) {
                        Node node = getNode("variants", entry.getKey(), languageId);
                        if (null == node)
                            node = getNode("lemma", entry.getKey(), languageId);
                        pos = getPOSValue(node, languageId);
                        lemma = getLemmaValue(node, languageId);
                        thresholdLevel = getThresholdLevelValue(node, languageId);
                        category = getCategoryValue(node, languageId);
                        wc = getWordComplexityValue(node, languageId);
                        if (null == wc)
                            wc = (double) 0;
                        else
                            wc = formatDoubleValue(wc);
                        pc.getWordComplexityMap().put(entry.getKey(), wc);
                        totalWordComplexity += wc;
                        posMap.put(entry.getKey(), pos);
                        categoryMap.put(entry.getKey(), category);
                        rootWordMap.put(entry.getKey(), lemma);
                    }
                    Map<String, Object> wordDetailMap = new HashMap<String, Object>();
                    wordDetailMap.put("word", entry.getKey());
                    wordDetailMap.put("orthographic_complexity", oc + "");
                    wordDetailMap.put("phonologic_complexity", phc + "");
                    wordDetailMap.put("word_complexity", wc);
                    wordDetailMap.put("syllableCount", pc.getSyllableCountMap().get(entry.getKey()) + "");
                    wordDetailMap.put("pos", pos);
                    wordDetailMap.put("category", category);
                    wordDetailMap.put("thresholdLevel", thresholdLevel);
                    wordDetailMap.put("total_complexity", oc + phc + ((null == wc) ? 0 : wc.doubleValue()));
                    wordDetailsMap.put(entry.getKey(), wordDetailMap);
                    
                    Integer count = (null == frequencyMap.get(entry.getKey()) ? 0 : frequencyMap.get(entry.getKey()));
                    count += (null == wordFrequency.get(entry.getKey()) ? 1 : wordFrequency.get(entry.getKey()));
                    frequencyMap.put(entry.getKey(), count);
                        
                    Integer storyCount = (null == storyCountMap.get(entry.getKey()) ? 0 : storyCountMap.get(entry.getKey()));
                    storyCount += 1;
                    storyCountMap.put(entry.getKey(), storyCount);
                }
                wordRowMap.put(filename, wordDetailsMap);
                summary.put("total_word_complexity", totalWordComplexity);
                double meanWordComplexity = formatDoubleValue(totalWordComplexity / sortedMap.size());
                summary.put("avg_word_complexity", meanWordComplexity);
                summaryMap.put(textEntry.getKey(), summary);
            }
            Map<String, Object> response = new HashMap<String, Object>();
            response.put("cutoff_complexity", 50);
            response.put("summary", summaryMap);
            response.put("texts", wordRowMap);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object> analyseTextsCSV(String languageId, Map<String, String> texts) {
        try {
            if (!SyllableMap.isLanguageEnabled(languageId) || null == texts || texts.isEmpty())
                return null;
            String[] headerRows = new String[] { "File Name", "Total Orthographic Complexity", "Avg. Syllable Orthographic Complexity",
                    "Total Phonological Complexity", "Avg. Syllable Phonological Complexity", "Word Count", "Syllable Count"};
            String[] wordRowsHeader = new String[]{"Word", "PoS", "Orthographic Complexity", "Phonological Complexity", "Category", "Root Word"};
            String[] totalWordsHeader = new String[]{"Word", "Root Word", "Frequency", "Number of stories", "PoS", "Orthographic Complexity", "Phonological Complexity", "Category"};

            Map<String, String> posMap = new HashMap<String, String>();
            Map<String, String> rootWordMap = new HashMap<String, String>();
            Map<String, String> categoryMap = new HashMap<String, String>();
            Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
            Map<String, Integer> storyCountMap = new HashMap<String, Integer>();
            Map<String, ComplexityMeasures> complexityMap = new HashMap<String, ComplexityMeasures>();
            Map<String, List<String[]>> wordRowMap = new HashMap<String, List<String[]>>();
            List<String[]> rows = new ArrayList<String[]>();
            rows.add(headerRows);
            for (Entry<String, String> textEntry : texts.entrySet()) {
                String filename = textEntry.getKey();
                String s = textEntry.getValue();
                ParagraphComplexity pc = getTextComplexity(languageId, s);
                System.out.println(
                        filename + ": " + pc.getMeanOrthoComplexity() + ", " + pc.getMeanPhonicComplexity());
                String[] row = new String[headerRows.length];
                row[0] = filename;
                row[1] = pc.getTotalOrthoComplexity().toString();
                row[2] = pc.getMeanOrthoComplexity().toString();
                row[3] = pc.getTotalPhonicComplexity().toString();
                row[4] = pc.getMeanPhonicComplexity().toString();
                row[5] = "" + pc.getWordCount();
                row[6] = "" + pc.getSyllableCount();
                
                Map<String, Integer> wordFrequency = pc.getWordFrequency();
                Map<String, ComplexityMeasures> wordMeasures = pc.getWordMeasures();
                ComplexityMeasuresComparator comparator = new ComplexityMeasuresComparator(wordMeasures);
                Map<String, ComplexityMeasures> sortedMap = new TreeMap<String,ComplexityMeasures>(comparator);
                sortedMap.putAll(wordMeasures);
                complexityMap.putAll(wordMeasures);
                
                List<String[]> wordRows = new ArrayList<String[]>();
                wordRows.add(wordRowsHeader);
                for (Entry<String, ComplexityMeasures> entry : sortedMap.entrySet()) {
                    String pos = posMap.get(entry.getKey());
                    String lemma = rootWordMap.get(entry.getKey());
                    String category = categoryMap.get(entry.getKey());
                    if (StringUtils.isBlank(lemma)) {
                        Node node = getNode("variants", entry.getKey(), languageId);
                        if (null == node)
                            node = getNode("lemma", entry.getKey(), languageId);
                        pos = getPOSValue(node, languageId);
                        lemma = getLemmaValue(node, languageId);
                        category = getCategoryValue(node, languageId);
                        posMap.put(entry.getKey(), pos);
                        categoryMap.put(entry.getKey(), category);
                        rootWordMap.put(entry.getKey(), lemma);
                    }
                    String[] wordRow = new String[wordRowsHeader.length];
                    wordRow[0] = entry.getKey();
                    wordRow[1] = pos;
                    wordRow[2] = entry.getValue().getOrthographic_complexity() + "";
                    wordRow[3] = entry.getValue().getPhonologic_complexity() + "";
                    wordRow[4] = category;
                    wordRow[5] = lemma;
                    wordRows.add(wordRow);
                    Integer count = (null == frequencyMap.get(entry.getKey()) ? 0 : frequencyMap.get(entry.getKey()));
                    count += (null == wordFrequency.get(entry.getKey()) ? 1 : wordFrequency.get(entry.getKey()));
                    frequencyMap.put(entry.getKey(), count);
                        
                    Integer storyCount = (null == storyCountMap.get(entry.getKey()) ? 0 : storyCountMap.get(entry.getKey()));
                    storyCount += 1;
                    storyCountMap.put(entry.getKey(), storyCount);
                }
                wordRowMap.put(filename, wordRows);
                rows.add(row);
            }
            Map<String, Object> response = new HashMap<String, Object>();
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
            StringWriter sWriter = new StringWriter();
            CSVPrinter writer = new CSVPrinter(sWriter, csvFileFormat);
            writer.printRecords(rows);
            response.put("summary", sWriter.toString());
            writer.close();
            sWriter.close();

            Map<String, Object> wordResponse = new HashMap<String, Object>();
            for (Entry<String, List<String[]>> entry : wordRowMap.entrySet()) {
                sWriter = new StringWriter();
                CSVPrinter writer1 = new CSVPrinter(sWriter, csvFileFormat);
                writer1.printRecords(entry.getValue());
                wordResponse.put(entry.getKey(), sWriter.toString());
                writer1.close();
                sWriter.close();
            }
            response.put("groups", wordResponse);
            
            List<String[]> summaryRows = new ArrayList<String[]>();
            summaryRows.add(totalWordsHeader);
            ComplexityMeasuresComparator comparator = new ComplexityMeasuresComparator(complexityMap);
            Map<String, ComplexityMeasures> summaryMap = new TreeMap<String,ComplexityMeasures>(comparator);
            summaryMap.putAll(complexityMap);
            for (Entry<String, ComplexityMeasures> entry : summaryMap.entrySet()) {
                String[] row = new String[totalWordsHeader.length];
                row[0] = entry.getKey();
                row[1] = rootWordMap.get(entry.getKey());
                row[2] = (null == frequencyMap.get(entry.getKey()) ? "1" : "" + frequencyMap.get(entry.getKey()));
                row[3] = (null == storyCountMap.get(entry.getKey()) ? "1" : "" + storyCountMap.get(entry.getKey()));
                row[4] = posMap.get(entry.getKey());
                row[5] = entry.getValue().getOrthographic_complexity() + "";
                row[6] = entry.getValue().getPhonologic_complexity() + "";
                row[7] = categoryMap.get(entry.getKey());
                summaryRows.add(row);
            }
            sWriter = new StringWriter();
            CSVPrinter writer2 = new CSVPrinter(sWriter, csvFileFormat);
            writer2.printRecords(summaryRows);
            response.put("words", sWriter.toString());
            writer2.close();
            sWriter.close();
            
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static Node getNode(String prop, String value, String languageId) {
        Transaction tx = null;
        Node neo4jNode = null;
        try {
            GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(languageId);
            tx = graphDb.beginTx();
            SearchCriteria sc = new SearchCriteria();
            sc.setObjectType("Word");
            sc.addMetadata(MetadataCriterion.create(Arrays.asList(new Filter(prop, SearchConditions.OP_IN, Arrays.asList(value)))));
            sc.setResultSize(1);
            String query = sc.getQuery();
            Map<String, Object> params = sc.getParams();
            Result result = graphDb.execute(query, params);
            if (null != result) {
                if (result.hasNext()) {
                    Map<String, Object> map = result.next();
                    Object o = map.values().iterator().next();
                    if (o instanceof Node) {
                        neo4jNode = (Node) o;
                    }
                }
                result.close();
            }
            tx.success();
        } catch (Exception e) {
            e.printStackTrace();
            if (null != tx)
                tx.failure();
        } finally {
            if (null != tx)
                tx.close();
        }
        return neo4jNode;
    }
    
    private static String getLemmaValue(Node neo4jNode, String languageId) {
        Transaction tx = null;
        String lemma = null;
        try {
            GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(languageId);
            tx = graphDb.beginTx();
            if (null != neo4jNode) {
                if (neo4jNode.hasProperty(IWordnetConstants.ATTRIB_LEMMA))
                    lemma = (String) neo4jNode.getProperty(IWordnetConstants.ATTRIB_LEMMA);
            }
            tx.success();
        } catch (Exception e) {
            e.printStackTrace();
            if (null != tx)
                tx.failure();
        } finally {
            if (null != tx)
                tx.close();
        }
        return lemma;
    }
    
    private static String getThresholdLevelValue(Node neo4jNode, String languageId) {
        Transaction tx = null;
        String level = null;
        try {
            GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(languageId);
            tx = graphDb.beginTx();
            if (null != neo4jNode) {
                if (neo4jNode.hasProperty(IWordnetConstants.ATTRIB_THRESHOLD_LEVEL))
                    level = (String) neo4jNode.getProperty(IWordnetConstants.ATTRIB_THRESHOLD_LEVEL);
            }
            tx.success();
        } catch (Exception e) {
            e.printStackTrace();
            if (null != tx)
                tx.failure();
        } finally {
            if (null != tx)
                tx.close();
        }
        return level;
    }
    
    private static String getCategoryValue(Node neo4jNode, String languageId) {
        Transaction tx = null;
        String category = null;
        try {
            GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(languageId);
            tx = graphDb.beginTx();
            if (null != neo4jNode) {
                if (neo4jNode.hasProperty(IWordnetConstants.ATTRIB_CATEGORY))
                    category = (String) neo4jNode.getProperty(IWordnetConstants.ATTRIB_CATEGORY);
            }
            tx.success();
        } catch (Exception e) {
            e.printStackTrace();
            if (null != tx)
                tx.failure();
        } finally {
            if (null != tx)
                tx.close();
        }
        return category;
    }
    
    private static Double getWordComplexityValue(Node neo4jNode, String languageId) {
        Transaction tx = null;
        Double value = null;
        try {
            GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(languageId);
            tx = graphDb.beginTx();
            if (null != neo4jNode) {
                if (neo4jNode.hasProperty(IWordnetConstants.ATTRIB_WORD_COMPLEXITY))
                    value = (Double) neo4jNode.getProperty(IWordnetConstants.ATTRIB_WORD_COMPLEXITY);
            }
            tx.success();
        } catch (Exception e) {
            e.printStackTrace();
            if (null != tx)
                tx.failure();
        } finally {
            if (null != tx)
                tx.close();
        }
        return value;
    }
    
    private static String getPOSValue(Node neo4jNode, String languageId) {
        Transaction tx = null;
        String posValue = null;
        try {
            GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(languageId);
            tx = graphDb.beginTx();
            if (null != neo4jNode) {                
                if (neo4jNode.hasProperty(IWordnetConstants.ATTRIB_POS)) {
                    String[] pos = (String[]) neo4jNode.getProperty(IWordnetConstants.ATTRIB_POS);
                    if (null != pos && pos.length > 0) {
                        for (String str : pos) {
                            if (WordnetUtil.isStandardPOS(str)) {
                                posValue = str.trim().toLowerCase();
                                break;
                            }
                        }
                        if (StringUtils.isBlank(posValue))
                            posValue = WordnetUtil.getPosValue(pos[0]);
                    }
                }
            }
            tx.success();
        } catch (Exception e) {
            e.printStackTrace();
            if (null != tx)
                tx.failure();
        } finally {
            if (null != tx)
                tx.close();
        }
        return posValue;
    }
    
    public static ParagraphComplexity getTextComplexity(String language, String text) {
        if (!SyllableMap.isLanguageEnabled(language))
            return null;
        if (StringUtils.isNotBlank(text)) {
            List<String> tokens = LanguageUtil.getTokens(text);
            Map<String, Integer> syllableCountMap = new HashMap<String, Integer>();
            Map<String, Integer> wordFrequency = new HashMap<String, Integer>();
            Map<String, WordComplexity> wordComplexities = new HashMap<String, WordComplexity>();
            Map<String, Double> wcMap = new HashMap<String, Double>();
            if (null != tokens && !tokens.isEmpty()) {
                for (String word : tokens) {
                    WordComplexity wc = WordMeasures.getWordComplexity(language, word);
                    wordComplexities.put(word, wc);
                    Integer count = null == wordFrequency.get(word) ? 0 : wordFrequency.get(word);
                    count += 1;
                    wordFrequency.put(word, count);
                    syllableCountMap.put(word, wc.getCount());
                }
                //WordUtil wordUtil = new WordUtil();
                //wcMap = wordUtil.getWordComplexity(tokens, language);
            }
            ParagraphComplexity pc = new ParagraphComplexity();
            pc.setText(text);
            pc.setWordFrequency(wordFrequency);
            pc.setWordComplexityMap(wcMap);
            pc.setSyllableCountMap(syllableCountMap);
            computeMeans(pc, wordComplexities, wcMap);
            return pc;
        } else {
            return null;
        }
    }
    
    private static List<Map<String, String>> getSuitableGradeSummaryInfo(String languageId, Double value){
    	List<com.ilimi.graph.dac.model.Node> suitableGrade = GradeComplexityCache.getInstance().getSuitableGrades(languageId, value);
        List<Map<String, String>> suitableGradeSummary = new ArrayList<Map<String,String>>();
        if(suitableGrade!=null)
	        for(com.ilimi.graph.dac.model.Node sg : suitableGrade){
	        	Map<String, String> gradeInfo = new HashMap<>();
	        	gradeInfo.put("Grade Level", (String) sg.getMetadata().get("gradeLevel"));
	        	gradeInfo.put("Language Level", (String) sg.getMetadata().get("languageLevel"));
	        	suitableGradeSummary.add(gradeInfo);
	        }
        return suitableGradeSummary;
    }

    private static void computeMeans(ParagraphComplexity pc, Map<String, WordComplexity> wordComplexities, Map<String, Double> wcMap) {
        int count = 0;
        double orthoComplexity = 0;
        double phonicComplexity = 0;
        double wordComplexity = 0;
        Map<String, ComplexityMeasures> wordMeasures = new HashMap<String, ComplexityMeasures>();
        for (Entry<String, WordComplexity> entry : wordComplexities.entrySet()) {
            WordComplexity wc = entry.getValue();
            orthoComplexity += wc.getOrthoComplexity();
            phonicComplexity += wc.getPhonicComplexity();
            double wcValue = (null == wcMap.get(entry.getKey()) ? 0 : wcMap.get(entry.getKey()).doubleValue());
            wordComplexity += wcValue;
            count += wc.getCount();
            wordMeasures.put(entry.getKey(), new ComplexityMeasures(wc.getOrthoComplexity(), wc.getPhonicComplexity()));
        }
        pc.setWordCount(wordComplexities.size());
        pc.setSyllableCount(count);
        pc.setWordMeasures(wordMeasures);
        pc.setWordComplexityMap(wcMap);
        pc.setTotalOrthoComplexity(formatDoubleValue(orthoComplexity));
        pc.setTotalPhonicComplexity(formatDoubleValue(phonicComplexity));
        pc.setTotalWordComplexity(formatDoubleValue(wordComplexity));
        pc.setMeanOrthoComplexity(formatDoubleValue(orthoComplexity / count));
        pc.setMeanPhonicComplexity(formatDoubleValue(phonicComplexity / count));
        pc.setMeanWordComplexity(formatDoubleValue(wordComplexity / count));
        double totalComplexity = orthoComplexity + phonicComplexity;
        pc.setMeanComplexity(formatDoubleValue(totalComplexity / count));
    }

    private static Double formatDoubleValue(Double d) {
        if (null != d) {
            try {
                BigDecimal bd = new BigDecimal(d);
                bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
                return bd.doubleValue();
            } catch (Exception e) {
            }
        }
        return d;
    }
}

@SuppressWarnings("rawtypes")
class ComplexityMeasuresComparator implements Comparator {
    private Map map;
    public ComplexityMeasuresComparator(Map map) {
        this.map = map;
    }
    @Override
    public int compare(Object o1, Object o2) {
        ComplexityMeasures c1 = (ComplexityMeasures) map.get(o1);
        ComplexityMeasures c2 = (ComplexityMeasures) map.get(o2);
        Double complexity1 = c1.getOrthographic_complexity() + c1.getPhonologic_complexity();
        Double complexity2 = c2.getOrthographic_complexity() + c2.getPhonologic_complexity();
        return complexity2.compareTo(complexity1);
    }
    
}
