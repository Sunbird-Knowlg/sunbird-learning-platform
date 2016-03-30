package org.ekstep.language.measures;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
import org.ekstep.language.measures.entity.ComplexityMeasures;
import org.ekstep.language.measures.entity.ParagraphComplexity;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.measures.meta.SyllableMap;
import org.ekstep.language.util.LanguageUtil;
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
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            String languageId = "hi";
            String[] headerRows = new String[] { "File Name", "Total Orthographic Complexity", "Avg. Syllable Orthographic Complexity",
                    "Total Phonological Complexity", "Avg. Syllable Phonological Complexity", "Word Count", "Syllable Count"};
            String[] wordRowsHeader = new String[]{"Word", "PoS", "Orthographic Complexity", "Phonological Complexity"};
            String[] totalWordsHeader = new String[]{"Word", "Root Word", "Frequency", "Number of stories", "PoS", "Orthographic Complexity", "Phonological Complexity"};
            List<String> skipFiles = Arrays.asList(new String[]{"complex_text.txt", "hindi_poem.txt", "standard_text.txt"});

            Map<String, String> posMap = new HashMap<String, String>();
            Map<String, String> rootWordMap = new HashMap<String, String>();
            Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
            Map<String, Integer> storyCountMap = new HashMap<String, Integer>();
            Map<String, ComplexityMeasures> complexityMap = new HashMap<String, ComplexityMeasures>();
            Map<String, List<String[]>> wordRowMap = new HashMap<String, List<String[]>>();
            List<String[]> rows = new ArrayList<String[]>();
            rows.add(headerRows);
            File dir = new File("/Users/rayulu/work/EkStep/language_model/texts/pratham_stories");
            File[] files = dir.listFiles();
            for (File f : files) {
                DataInputStream dis = new DataInputStream(new FileInputStream(f));
                byte[] b = new byte[dis.available()];
                dis.readFully(b);
                String s = new String(b);
                ParagraphComplexity pc = getTextComplexity("hi", s);
                System.out.println(
                        f.getName() + ": " + pc.getMeanOrthoComplexity() + ", " + pc.getMeanPhonicComplexity());
                String[] row = new String[headerRows.length];
                row[0] = f.getName();
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
                if (!skipFiles.contains(f.getName()))
                    complexityMap.putAll(wordMeasures);
                
                List<String[]> wordRows = new ArrayList<String[]>();
                wordRows.add(wordRowsHeader);
                for (Entry<String, ComplexityMeasures> entry : sortedMap.entrySet()) {
                    String pos = posMap.get(entry.getKey());
                    String lemma = rootWordMap.get(entry.getKey());
                    if (StringUtils.isBlank(pos)) {
                        Node node = getNode("variants", entry.getKey(), languageId);
                        if (null == node)
                            node = getNode("lemma", entry.getKey(), languageId);
                        pos = getPOSValue(node, languageId);
                        lemma = getLemmaValue(node, languageId);
                        posMap.put(entry.getKey(), pos);
                        rootWordMap.put(entry.getKey(), lemma);
                    }
                    String[] wordRow = new String[wordRowsHeader.length];
                    wordRow[0] = entry.getKey();
                    wordRow[1] = pos;
                    wordRow[2] = entry.getValue().getOrthographic_complexity() + "";
                    wordRow[3] = entry.getValue().getPhonologic_complexity() + "";
                    wordRows.add(wordRow);
                    if (!skipFiles.contains(f.getName())) {
                        Integer count = (null == frequencyMap.get(entry.getKey()) ? 0 : frequencyMap.get(entry.getKey()));
                        count += (null == wordFrequency.get(entry.getKey()) ? 1 : wordFrequency.get(entry.getKey()));
                        frequencyMap.put(entry.getKey(), count);
                        
                        Integer storyCount = (null == storyCountMap.get(entry.getKey()) ? 0 : storyCountMap.get(entry.getKey()));
                        storyCount += 1;
                        storyCountMap.put(entry.getKey(), storyCount);
                    }
                }
                wordRowMap.put(f.getName(), wordRows);
                rows.add(row);
                dis.close();
            }
            
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
            FileWriter fw = new FileWriter(new File("/Users/rayulu/work/EkStep/language_model/texts/pratham_output/pratham_stories.csv"));
            CSVPrinter writer = new CSVPrinter(fw, csvFileFormat);
            writer.printRecords(rows);
            writer.close();
            
            for (Entry<String, List<String[]>> entry : wordRowMap.entrySet()) {
                FileWriter fw1 = new FileWriter(new File("/Users/rayulu/work/EkStep/language_model/texts/pratham_output/" + entry.getKey() + ".csv"));
                CSVPrinter writer1 = new CSVPrinter(fw1, csvFileFormat);
                writer1.printRecords(entry.getValue());
                writer1.close();
            }
            
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
                summaryRows.add(row);
            }
            FileWriter fw2 = new FileWriter(new File("/Users/rayulu/work/EkStep/language_model/texts/pratham_output/summary.csv"));
            CSVPrinter writer2 = new CSVPrinter(fw2, csvFileFormat);
            writer2.printRecords(summaryRows);
            writer2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                if (neo4jNode.hasProperty("lemma")) {
                    lemma = (String) neo4jNode.getProperty("lemma");
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
        return lemma;
    }
    
    private static String getPOSValue(Node neo4jNode, String languageId) {
        Transaction tx = null;
        String posValue = null;
        try {
            GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(languageId);
            tx = graphDb.beginTx();
            if (null != neo4jNode) {
                if (neo4jNode.hasProperty("pos_categories")) {
                    String[] pos = (String[]) neo4jNode.getProperty("pos_categories");
                    if (null != pos && pos.length > 0) {
                        String cat = pos[0];
                        if (StringUtils.equalsIgnoreCase("n", cat) || StringUtils.equalsIgnoreCase("nn", cat))
                            posValue = "noun";
                        else if (StringUtils.equalsIgnoreCase("v", cat) || StringUtils.equalsIgnoreCase("vm", cat))
                            posValue = "verb";
                        else if (StringUtils.equalsIgnoreCase("adj", cat) || StringUtils.equalsIgnoreCase("jj", cat))
                            posValue = "adjective";
                        else if (StringUtils.equalsIgnoreCase("pn", cat))
                            posValue = "pronoun";
                    }
                } 
                if (StringUtils.isBlank(posValue) && neo4jNode.hasProperty("pos")) {
                    String[] pos = (String[]) neo4jNode.getProperty("pos");
                    if (null != pos && pos.length > 0) {
                        String cat = pos[0].toLowerCase();
                        if (StringUtils.equalsIgnoreCase("n", cat) || StringUtils.equalsIgnoreCase("nn", cat))
                            posValue = "noun";
                        else if (StringUtils.equalsIgnoreCase("v", cat) || StringUtils.equalsIgnoreCase("vm", cat))
                            posValue = "verb";
                        else if (StringUtils.equalsIgnoreCase("adj", cat) || StringUtils.equalsIgnoreCase("jj", cat))
                            posValue = "adjective";
                        else if (StringUtils.equalsIgnoreCase("pn", cat))
                            posValue = "pronoun";
                        else
                            posValue = pos[0].toLowerCase();
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
            Map<String, Integer> wordFrequency = new HashMap<String, Integer>();
            Map<String, WordComplexity> wordComplexities = new HashMap<String, WordComplexity>();
            if (null != tokens && !tokens.isEmpty()) {
                for (String word : tokens) {
                    WordComplexity wc = WordMeasures.getWordComplexity(language, word);
                    wordComplexities.put(word, wc);
                    Integer count = null == wordFrequency.get(word) ? 0 : wordFrequency.get(word);
                    count += 1;
                    wordFrequency.put(word, count);
                }
            }
            ParagraphComplexity pc = new ParagraphComplexity();
            pc.setText(text);
            pc.setWordFrequency(wordFrequency);
            computeMeans(pc, wordComplexities);
            return pc;
        } else {
            return null;
        }
    }

    private static void computeMeans(ParagraphComplexity pc, Map<String, WordComplexity> wordComplexities) {
        int count = 0;
        double orthoComplexity = 0;
        double phonicComplexity = 0;
        Map<String, ComplexityMeasures> wordMeasures = new HashMap<String, ComplexityMeasures>();
        for (Entry<String, WordComplexity> entry : wordComplexities.entrySet()) {
            WordComplexity wc = entry.getValue();
            orthoComplexity += wc.getOrthoComplexity();
            phonicComplexity += wc.getPhonicComplexity();
            count += wc.getCount();
            wordMeasures.put(entry.getKey(), new ComplexityMeasures(wc.getOrthoComplexity(), wc.getPhonicComplexity()));
        }
        pc.setWordCount(wordComplexities.size());
        pc.setSyllableCount(count);
        pc.setWordMeasures(wordMeasures);
        pc.setTotalOrthoComplexity(formatDoubleValue(orthoComplexity));
        pc.setTotalPhonicComplexity(formatDoubleValue(phonicComplexity));
        pc.setMeanOrthoComplexity(formatDoubleValue(orthoComplexity / count));
        pc.setMeanPhonicComplexity(formatDoubleValue(phonicComplexity / count));
    }

    private static Double formatDoubleValue(Double d) {
        if (null != d) {
            BigDecimal bd = new BigDecimal(d);
            bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
            return bd.doubleValue();
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
