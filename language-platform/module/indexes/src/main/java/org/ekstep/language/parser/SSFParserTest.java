package org.ekstep.language.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.model.CitationBean;
import org.ekstep.language.model.WordPojo;
import org.ekstep.language.util.ElasticSearchUtil;
import org.ekstep.language.util.WordUtil;

import com.opencsv.CSVWriter;

public class SSFParserTest {

	private static String SENTENCE_SPLITTER = " ";
	private static String ATTRIBUTES_SEPARATOR = ",";
	private static int defaultTokenCountAfterWord = 4;
	private static String[] ignoreStartWords;
	private static String[] tagNames;
	private static String[] discardTokens;
	private static String attributesTagIdentifier;
	private static String specialCharRegEx = "^([$&+,:;=?@#|!]*)$";
	private static String numberRegEx = "^([+-]?\\d*\\.?\\d*)$";
	private static WordUtil wordUtil = new WordUtil();
	private static Set<String> distinctWords = new HashSet<String>();

	static {
		String ignoreStartWordsList = "<Sentence,id=,<fs,head=,case_name=,paradigm=,name=,inf=,poslcat=";
		ignoreStartWords = ignoreStartWordsList.split(",");
		String tagNamesList = "NN,NST,PRP,DEM,VM,VAUX,JJ,RB,PSP,RP,CC,WQ,QF,QC,QO,CL,INTF,INJ,NEG,SYM,*C,RDP,ECH,UNK,NP,VGF,VGNF,VGINF,VGNN,JJP,RBP,NEGP,CCP,FRAGP,BLK";
		tagNames = tagNamesList.split(",");
		String discardTokensList = "NNP,((,))";
		discardTokens = discardTokensList.split(",");
		attributesTagIdentifier = "af";
	}
	
	public static ArrayList<WordPojo> listFilesForFolder(final File folder) {
		ArrayList<WordPojo> wordsList = new ArrayList<WordPojo>();
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	        	wordsList.addAll(listFilesForFolder(fileEntry));
	        } else {
	        	wordsList.addAll(parseSsfFile(fileEntry.getAbsoluteFile().getAbsolutePath(), "Text Books", "ka"));
	        }
	    }
	    return wordsList;
	}
	
	private static void writeWordListToCSV(List<WordPojo> wordList) throws IOException {
		String csvFile = "C:\\data\\SSFFiles\\Word_List_Kannada_Class_1_to_3.csv";
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
		Set<WordPojo> wordSet = new HashSet<WordPojo>(wordList);
		for(WordPojo data: wordSet){
			String[] record = data.getStringArray();
			writer.writeNext(record);
		}
		writer.close();
	}
	
	private static void writeDistinctWordsToCSV(Set<String> distinctWords) throws IOException {
		String csvFile = "C:\\data\\SSFFiles\\Distinct_Root_Word_List_Kannada_Class_1_to_3.csv";
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
		for(String data: distinctWords){
			String[] record = new String[]{data};
			writer.writeNext(record);
		}
		writer.close();
	}
	
	public static void main(String args[]){
		final File folder = new File("C:\\data\\SSFFiles\\output_old\\output");
		ArrayList<WordPojo> wordsList = listFilesForFolder(folder);
		try {
			writeWordListToCSV(wordsList);
			writeDistinctWordsToCSV(distinctWords);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<WordPojo> parseSsfFile(String fileName, String sourceType,
			String language) {
		String sentence = null;
		BufferedReader br = null;
		ArrayList<WordPojo> wordsList = new ArrayList<WordPojo>();
		try {
			File file = new File(fileName);
			br = new BufferedReader(new FileReader(file));
			while ((sentence = br.readLine()) != null) {
				wordsList.addAll(processSentence(sentence, sourceType));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return wordsList;
	}

	@SuppressWarnings("unused")
	private static void addCitationsAndWordIndexToElasticSearch(
			List<CitationBean> citations, String language) {
		try {
			String citationIndexName = Constants.CITATION_INDEX_COMMON_NAME + "_"
					+ language;
			String wordIndexName = Constants.WORD_INDEX_COMMON_NAME + "_"
					+ language;
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<String> citiationIndexes = new ArrayList<String>();
			ArrayList<String> wordIndexes = new ArrayList<String>();
			ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
			for (CitationBean citation : citations) {
				String citationJson = mapper.writeValueAsString(citation);
				citiationIndexes.add(citationJson);

				String word = citation.getRootWord();
				String wordIdentifier = wordUtil.getWordIdentifier(language,
						word);
				if (wordIdentifier == null) {
					word = citation.getWord();
					wordIdentifier = wordUtil.getWordIdentifier(language, word);
				}
				if (wordIdentifier != null) {
					Map<String, String> wordIndex = new HashMap<String, String>();
					wordIndex.put("word", word);
					wordIndex.put("id", wordIdentifier);
					String wordIndexJson = mapper.writeValueAsString(wordIndex);
					wordIndexes.add(wordIndexJson);
				}
			}
			elasticSearchUtil.bulkIndexWithAutoGenerateIndexId(citationIndexName,
					Constants.CITATION_INDEX_TYPE, citiationIndexes);
			elasticSearchUtil.bulkIndexWithAutoGenerateIndexId(wordIndexName,
					Constants.WORD_INDEX_TYPE, wordIndexes);
			elasticSearchUtil.closeClient();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static ArrayList<WordPojo> processSentence(String sentence,
			String sourceType) {
		String[] sentenceTokens = sentence.split(SENTENCE_SPLITTER);
		ArrayList<String> enhancedSentenceTokens = enhanceSentenceTokens(sentenceTokens);
		boolean wordFound = false;
		String word = null;
		int tokenCountAfterWord = 0;
		ArrayList<WordPojo> wordList = new ArrayList<WordPojo>();
		for (String token : enhancedSentenceTokens) {
			tokenCountAfterWord++;

			if (isTagName(token)) {
				continue;
			}
			if (ignoreToken(token)) {
				continue;
			}
			if (isNumber(token)) {
				continue;
			}
			if (discardWord(token)
					|| (wordFound && tokenCountAfterWord > defaultTokenCountAfterWord)) {
				wordFound = false;
				word = null;
				continue;
			}
			if (isSpecialCharacter(token)) {
				continue;
			}
			if (token.startsWith(attributesTagIdentifier)) {
				if (wordFound && word != null) {
					wordFound = false;
					String[] afTokens = token.split("=");
					if (afTokens[1] != null) {
						try {
							String[] afAttributes = afTokens[1]
									.split(ATTRIBUTES_SEPARATOR);
							String rootWord = afAttributes[Constants.TAG_INDEX_ROOT_WORD]
									.replace("'", "");
							String category = afAttributes[Constants.TAG_INDEX_CATEGORY];

							WordPojo wordObj = new WordPojo(word, rootWord);
							wordList.add(wordObj);
							distinctWords.add(rootWord);
							
							word = null;
							tokenCountAfterWord = 0;
						} catch (IndexOutOfBoundsException e) {
							e.printStackTrace();
						}
					}
				}
				continue;
			}
			wordFound = true;
			word = token;
			tokenCountAfterWord = 0;
		}
		return wordList;
	}

	private static boolean discardWord(String token) {
		if (ArrayUtils.contains(discardTokens, token)) {
			return true;
		}
		return false;
	}

	private static boolean isSpecialCharacter(String token) {
		if (token.matches(specialCharRegEx)) {
			return true;
		}
		return false;
	}

	private static boolean isNumber(String token) {
		if (token.matches(numberRegEx)) {
			return true;
		}
		return false;
	}

	private static boolean ignoreToken(String token) {
		for (String ignoreStr : ignoreStartWords) {
			if (token.startsWith(ignoreStr)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isTagName(String token) {
		if (ArrayUtils.contains(tagNames, token)) {
			return true;
		}
		return false;
	}

	private static ArrayList<String> enhanceSentenceTokens(
			String[] sentenceTokens) {
		ArrayList<String> enhancedSentenceTokens = new ArrayList<String>();
		for (String token : sentenceTokens) {
			token = token.replaceAll(" ", "");
			token = token.replaceAll("\t", "");
			token = token.replaceAll("\\s","");
			if (!token.isEmpty()) {
				enhancedSentenceTokens.add(token);
			}
		}
		return enhancedSentenceTokens;
	}
}
