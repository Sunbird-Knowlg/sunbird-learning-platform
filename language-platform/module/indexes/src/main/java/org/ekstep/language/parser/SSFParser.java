package org.ekstep.language.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.model.CitationBean;
import org.ekstep.language.util.ElasticSearchUtil;
import org.ekstep.language.util.ParserUtil;
import org.ekstep.language.util.WordUtil;

public class SSFParser {

	private static String SENTENCE_SPLITTER = " ";
	private static String ATTRIBUTES_SEPARATOR = ",";
	private static int defaultTokenCountAfterWord = 4;
	private static String[] ignoreStartWords;
	private static String[] tagNames;
	private static String[] discardTokens;
	private static Logger logger = LogManager.getLogger(SSFParser.class
			.getName());
	private static String attributesTagIdentifier;
	private static String specialCharRegEx = "^([$&+,:;=?@#|!]*)$";
	private static String numberRegEx = "^([+-]?\\d*\\.?\\d*)$";
	private static WordUtil wordUtil = new WordUtil();

	static {
		String ignoreStartWordsList = "<Sentence,id=,<fs,head=,case_name=,paradigm=,name=,inf=";
		ignoreStartWords = ignoreStartWordsList.split(",");
		String tagNamesList = "NN,NST,PRP,DEM,VM,VAUX,JJ,RB,PSP,RP,CC,WQ,QF,QC,QO,CL,INTF,INJ,NEG,SYM,*C,RDP,ECH,UNK,NP,VGF,VGNF,VGINF,VGNN,JJP,RBP,NEGP,CCP,FRAGP,BLK";
		tagNames = tagNamesList.split(",");
		String discardTokensList = "NNP,((,))";
		discardTokens = discardTokensList.split(",");
		attributesTagIdentifier = "af";
	}

	public static void parseSsfFilesFolder(String folderPath,
			String sourceType, String grade, String languageId) {
		final File folder = new File(folderPath);
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				parseSsfFilesFolder(fileEntry.getAbsolutePath(), sourceType,
						grade, languageId);
			} else {
				parseSsfFile(fileEntry.getAbsolutePath(), sourceType, grade,
						languageId);
			}
		}
	}

	public static void parseSsfFile(String fileName, String sourceType,
			String grade, String languageId) {
		String sentence = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					fileName), "UTF8"));
			while ((sentence = br.readLine()) != null) {
				addCitationsAndWordIndexToElasticSearch(
						processSentence(sentence, sourceType, grade),
						languageId);
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
	}

	public static void main(String args[]) {
		String fileName = "C:\\data\\ssfFile.txt";
		parseSsfFile(fileName, "Books", "1", "ta");
	}

	private static void addCitationsAndWordIndexToElasticSearch(
			List<CitationBean> citations, String language) {
		try {
			String citationIndexName = Constants.CITATION_INDEX_COMMON_NAME
					+ "_" + language;
			String wordIndexName = Constants.WORD_INDEX_COMMON_NAME + "_"
					+ language;
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<String> citiationIndexes = new ArrayList<String>();
			ArrayList<String> wordIndexes = new ArrayList<String>();
			ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
			for (CitationBean citation : citations) {
				String citationJson = mapper.writeValueAsString(citation);
				citiationIndexes.add(citationJson);

				/*
				 * String wordIdentifier = wordUtil.getWordIdentifier(language,
				 * citation.getRootWord()); String wordIndexJson =
				 * getWordIndex(citation.getRootWord(), citation.getRootWord(),
				 * wordIdentifier, mapper); // TODO modify later if
				 * (wordIndexJson != null) { // wordIndexes.add(wordIndexJson);
				 * }
				 * 
				 * wordIndexJson = null; wordIdentifier = null; wordIdentifier =
				 * wordUtil.getWordIdentifier(language, citation.getWord());
				 * wordIndexJson = getWordIndex(citation.getWord(),
				 * citation.getRootWord(), wordIdentifier, mapper); // TODO
				 * modify later if (wordIndexJson != null) { //
				 * wordIndexes.add(wordIndexJson); }
				 */
			}
			elasticSearchUtil.bulkIndexWithAutoGenerateIndexId(
					citationIndexName, Constants.CITATION_INDEX_TYPE,
					citiationIndexes);
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

	private static String getWordIndex(String word, String rootWord,
			String wordIdentifier, ObjectMapper mapper)
			throws JsonGenerationException, JsonMappingException, IOException {
		String wordIndexJson = null;
		if (wordIdentifier != null) {
			Map<String, String> wordIndex = new HashMap<String, String>();
			wordIndex.put("word", word);
			wordIndex.put("rootWord", rootWord);
			wordIndex.put("id", wordIdentifier);
			wordIndexJson = mapper.writeValueAsString(wordIndex);
		}
		return wordIndexJson;
	}

	private static List<CitationBean> processSentence(String sentence,
			String sourceType, String grade) {
		String[] sentenceTokens = sentence.split(SENTENCE_SPLITTER);
		ArrayList<String> enhancedSentenceTokens = enhanceSentenceTokens(sentenceTokens);
		boolean wordFound = false;
		String word = null;
		int tokenCountAfterWord = 0;
		List<CitationBean> citationList = new ArrayList<CitationBean>();
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

							CitationBean citationObj = new CitationBean(word,
									rootWord, category,
									ParserUtil.getFormattedDateTime(System
											.currentTimeMillis()), sourceType,
									grade);
							citationList.add(citationObj);

							word = null;
							tokenCountAfterWord = 0;
						} catch (IndexOutOfBoundsException e) {
							logger.error("Word attributes does not contain all required data.");
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
		return citationList;
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
			if (!token.isEmpty()) {
				enhancedSentenceTokens.add(token);
			}
		}
		return enhancedSentenceTokens;
	}
}
