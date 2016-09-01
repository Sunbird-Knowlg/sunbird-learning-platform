package org.ekstep.language.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.model.CitationBean;
import org.ekstep.language.model.WordInfoBean;
import org.ekstep.language.util.Constants;
import org.ekstep.language.util.PropertiesUtil;
import org.ekstep.language.util.WordUtil;

import com.ilimi.common.exception.ClientException;

/**
 * SSFParser parses files in Simple Shakti Format(SSF) and retrieves citations,
 * word Info and word index documents and loads them into ElasticSearch (ES).
 * 
 * @author Amarnath
 * 
 */
public class SSFParser {

	/** The sentence splitter. */
	private static String SENTENCE_SPLITTER = " ";

	/** The attributes separator. */
	private static String ATTRIBUTES_SEPARATOR = ",";

	/** The default token count after word. */
	private static int defaultTokenCountAfterWord = Integer
			.parseInt(PropertiesUtil.getProperty("defaultTokenCountAfterWord"));

	/** The ignore start words. */
	private static String[] ignoreStartWords;

	/** The tag names. */
	private static String[] tagNames;

	/** The discard tokens. */
	private static String[] discardTokens;

	/** The attributes tag identifier. */
	private static String attributesTagIdentifier;

	/** The special char reg ex. */
	private static String specialCharRegEx = PropertiesUtil.getProperty("specialCharRegEx");

	/** The number reg ex. */
	private static String numberRegEx = PropertiesUtil.getProperty("numberRegEx");

	/** The word util. */
	private static WordUtil wordUtil = new WordUtil();

	// Gets properties required for processing
	static {
		// all words starting with any of these properties will be ignored
		String ignoreStartWordsList = PropertiesUtil.getProperty("ignoreStartWordsList");
		ignoreStartWords = ignoreStartWordsList.split(",");

		// tag names in the in SSF file
		String tagNamesList = PropertiesUtil.getProperty("tagNamesList");
		tagNames = tagNamesList.split(",");

		// tokens that need to be discarded
		String discardTokensList = PropertiesUtil.getProperty("discardTokensList");
		discardTokens = discardTokensList.split(",");

		// Tag that identifies the word properties
		attributesTagIdentifier = PropertiesUtil.getProperty("attributesTagIdentifier");
	}

	/**
	 * Parses the SSF files, recursively if the file path is a folder.
	 *
	 * @param filePath
	 *            the file path
	 * @param sourceType
	 *            the source type
	 * @param source
	 *            the source
	 * @param grade
	 *            the grade
	 * @param skipCitations
	 *            the skip citations
	 * @param languageId
	 *            the language id
	 * @throws Exception
	 *             the exception
	 */
	public static void parseSsfFiles(String filePath, String sourceType, String source, String grade,
			boolean skipCitations, String languageId) throws Exception {
		final File file = new File(filePath);
		if (file.isDirectory()) {
			for (final File fileEntry : file.listFiles()) {
				parseSsfFiles(fileEntry.getAbsolutePath(), sourceType, source, grade, skipCitations, languageId);
			}
		} else {
			parseSsfFile(file.getAbsolutePath(), sourceType, source, grade, skipCitations, languageId);
		}
	}

	/**
	 * Parses the SSF file, forms and adds citation, word index and word info
	 * index documents into ES.
	 *
	 * @param filePath
	 *            the file path
	 * @param sourceType
	 *            the source type
	 * @param source
	 *            the source
	 * @param grade
	 *            the grade
	 * @param skipCitations
	 *            the skip citations
	 * @param languageId
	 *            the language id
	 * @throws Exception
	 *             the exception
	 */
	public static void parseSsfFile(String filePath, String sourceType, String source, String grade,
			boolean skipCitations, String languageId) throws Exception {
		String sentence = null;
		BufferedReader br = null;
		try {
			File file = new File(filePath);
			String fileName = file.getName();

			// remove unwanted characters from the file
			fixFileFormat(filePath);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));

			// process file one line at a time
			while ((sentence = br.readLine()) != null) {

				// create and add indexes
				wordUtil.addIndexesToElasticSearch(
						processSentence(sentence, sourceType, source, grade, skipCitations, fileName, languageId),
						languageId);
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	/**
	 * Fixes file by removing unwanted characters and split data logically.
	 *
	 * @param fileName
	 *            the file name
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static void fixFileFormat(String fileName) throws IOException {
		Path path = Paths.get(fileName);
		Charset charset = StandardCharsets.UTF_8;
		String content = new String(Files.readAllBytes(path), charset);
		content = content.replaceAll("\n", "");
		content = content.replaceAll("<Sentence", "\n<Sentence");
		content = content.replaceAll("\\x{A0}", " ");
		Files.write(path, content.getBytes(charset));
	}

	/**
	 * Process a line form the file and create index documents.
	 *
	 * @param sentence
	 *            the sentence
	 * @param sourceType
	 *            the source type
	 * @param source
	 *            the source
	 * @param grade
	 *            the grade
	 * @param skipCitations
	 *            the skip citations
	 * @param fileName
	 *            the file name
	 * @param languageId
	 *            the language id
	 * @return the map
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("rawtypes")
	private static Map<String, List> processSentence(String sentence, String sourceType, String source, String grade,
			boolean skipCitations, String fileName, String languageId) throws Exception {

		// split the line by "space"
		String[] sentenceTokens = sentence.split(SENTENCE_SPLITTER);
		
		//enhance the sentence tokens
		ArrayList<String> enhancedSentenceTokens = enhanceSentenceTokens(sentenceTokens);
		boolean wordFound = false;
		String word = null;
		String pos = null;
		int tokenCountAfterWord = 0;
		Map<String, List> indexes = new HashMap<String, List>();
		List<CitationBean> citationList = new ArrayList<CitationBean>();
		List<WordInfoBean> wordInfoList = new ArrayList<WordInfoBean>();
		for (String token : enhancedSentenceTokens) {
			tokenCountAfterWord++;
			
			//get the pos
			if (isTagName(token)) {
				if (wordFound && word != null) {
					pos = token;
				}
				continue;
			}
			
			//ignore the ignore key words
			if (ignoreToken(token)) {
				continue;
			}
			
			//ignore numbers
			if (isNumber(token)) {
				continue;
			}
			
			//Discard if no word properties are found or if the token is marked to be a discarded
			if (discardWord(token) || (wordFound && tokenCountAfterWord > defaultTokenCountAfterWord)) {
				wordFound = false;
				word = null;
				pos = null;
				continue;
			}
			
			//Discard if Special Character
			if (isSpecialCharacter(token)) {
				continue;
			}
			
			//if it is an attribute tag, extract word properties
			if (token.startsWith(attributesTagIdentifier)) {
				if (wordFound && word != null) {
					wordFound = false;
					String[] afTokens = token.split("=");
					if (afTokens[1] != null) {
						try {
							String[] afAttributes = afTokens[1].split(ATTRIBUTES_SEPARATOR);
							String rootWord = cleanAttribute(afAttributes[Constants.TAG_INDEX_ROOT_WORD]);
							String category = cleanAttribute(afAttributes[Constants.TAG_INDEX_CATEGORY]);
							String gender = cleanAttribute(afAttributes[Constants.TAG_INDEX_GENDER]);
							String number = cleanAttribute(afAttributes[Constants.TAG_INDEX_NUMBER]);
							String pers = cleanAttribute(afAttributes[Constants.TAG_INDEX_PERS]);
							String wordCase = cleanAttribute(afAttributes[Constants.TAG_INDEX_CASE]);
							String inflection = cleanAttribute(afAttributes[Constants.TAG_INDEX_INFLECTION]);
							String rts = cleanAttribute(afAttributes[Constants.TAG_INDEX_RTS]);
							if (rootWord != null && !rootWord.isEmpty()) {
								if (!skipCitations) {
									// create citation bean
									CitationBean citationObj = new CitationBean(word, rootWord, pos,
											wordUtil.getFormattedDateTime(System.currentTimeMillis()), sourceType,
											source, grade, fileName);
									citationList.add(citationObj);
								}
								
								//create word info bean
								WordInfoBean wordInfo = new WordInfoBean(word, rootWord, pos, category, gender, number,
										pers, wordCase, inflection, rts);
								wordInfoList.add(wordInfo);
							}

							word = null;
							pos = null;
							tokenCountAfterWord = 0;
						} catch (IndexOutOfBoundsException e) {
							throw new ClientException(LanguageErrorCodes.ERR_INVALID_CONTENT.name(),
									"Word attributes does not contain all required data.");
						}
					}
				}
				continue;
			}

			if (wordFound && word != null && tokenCountAfterWord == 1) {
				pos = token;
				continue;
			}
			
			//if the token is not rejected till this point, treat it as a word
			wordFound = true;
			word = token;
			tokenCountAfterWord = 0;
		}
		if (!skipCitations) {
			indexes.put(Constants.CITATION_INDEX_COMMON_NAME, citationList);
		}
		indexes.put(Constants.WORD_INFO_INDEX_COMMON_NAME, wordInfoList);
		return indexes;
	}

	/**
	 * Discard token if it matches the discard tokens criteria
	 *
	 * @param token
	 *            the token
	 * @return true, if successful
	 */
	private static boolean discardWord(String token) {
		if (ArrayUtils.contains(discardTokens, token)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if is special character.
	 *
	 * @param token
	 *            the token
	 * @return true, if is special character
	 */
	private static boolean isSpecialCharacter(String token) {
		if (token.matches(specialCharRegEx)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if is number.
	 *
	 * @param token
	 *            the token
	 * @return true, if is number
	 */
	private static boolean isNumber(String token) {
		if (token.matches(numberRegEx)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if token is to be ignored.
	 *
	 * @param token
	 *            the token
	 * @return true, if successful
	 */
	private static boolean ignoreToken(String token) {
		for (String ignoreStr : ignoreStartWords) {
			if (token.startsWith(ignoreStr)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if is tag name.
	 *
	 * @param token
	 *            the token
	 * @return true, if is tag name
	 */
	private static boolean isTagName(String token) {
		if (ArrayUtils.contains(tagNames, token)) {
			return true;
		}
		return false;
	}

	/**
	 * Clean attribute.
	 *
	 * @param attribute
	 *            the attribute
	 * @return the string
	 */
	private static String cleanAttribute(String attribute) {
		attribute = attribute.replace("'", "");
		attribute = attribute.replace("<", "");
		attribute = attribute.replace(">", "");
		attribute = attribute.replace("0", "");
		return attribute;
	}

	/**
	 * Enhance sentence tokens.
	 *
	 * @param sentenceTokens
	 *            the sentence tokens
	 * @return the array list
	 */
	private static ArrayList<String> enhanceSentenceTokens(String[] sentenceTokens) {
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
