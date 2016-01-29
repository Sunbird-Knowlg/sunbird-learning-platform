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
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.model.CitationBean;
import org.ekstep.language.util.Constants;
import org.ekstep.language.util.PropertiesUtil;
import org.ekstep.language.util.WordUtil;

public class SSFParser {

	private static String SENTENCE_SPLITTER = " ";
	private static String ATTRIBUTES_SEPARATOR = ",";
	private static int defaultTokenCountAfterWord = Integer
			.parseInt(PropertiesUtil.getProperty("defaultTokenCountAfterWord"));
	private static String[] ignoreStartWords;
	private static String[] tagNames;
	private static String[] discardTokens;
	private static Logger logger = LogManager.getLogger(SSFParser.class
			.getName());
	private static String attributesTagIdentifier;
	private static String specialCharRegEx = PropertiesUtil
			.getProperty("specialCharRegEx");
	private static String numberRegEx = PropertiesUtil
			.getProperty("numberRegEx");
	private static WordUtil wordUtil = new WordUtil();

	static {
		String ignoreStartWordsList = PropertiesUtil
				.getProperty("ignoreStartWordsList");
		ignoreStartWords = ignoreStartWordsList.split(",");
		String tagNamesList = PropertiesUtil.getProperty("tagNamesList");
		tagNames = tagNamesList.split(",");
		String discardTokensList = PropertiesUtil
				.getProperty("discardTokensList");
		discardTokens = discardTokensList.split(",");
		attributesTagIdentifier = PropertiesUtil
				.getProperty("attributesTagIdentifier");
	}

	public static void parseSsfFiles(String folderPath,
			String sourceType, String source, String grade, String languageId) {
		final File file = new File(folderPath);
		if (file.isDirectory()) {
			for (final File fileEntry : file.listFiles()) {
				parseSsfFiles(fileEntry.getAbsolutePath(), sourceType,
						source, grade, languageId);
			}
		} else {
			parseSsfFile(file.getAbsolutePath(), sourceType, source, grade,
					languageId);
		}
	}

	public static void parseSsfFile(String filePath, String sourceType,
			String source, String grade, String languageId) {
		String sentence = null;
		BufferedReader br = null;
		try {
			File file = new File(filePath);
			String fileName = file.getName();
			fixFileFormat(filePath);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					filePath), "UTF8"));
			while ((sentence = br.readLine()) != null) {
				wordUtil.addCitationsAndWordIndexToElasticSearch(
						processSentence(sentence, sourceType, source, grade,
								fileName), languageId);
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

	private static void fixFileFormat(String fileName) throws IOException {
		Path path = Paths.get(fileName);
		Charset charset = StandardCharsets.UTF_8;
		String content = new String(Files.readAllBytes(path), charset);
		content = content.replaceAll("\n", "");
		content = content.replaceAll("<Sentence", "\n<Sentence");
		content = content.replaceAll("\\x{A0}", " ");
		// content = content.replaceAll("\\x{9}", " ");
		Files.write(path, content.getBytes(charset));
	}

	public static void main(String args[]) {
		String fileName = "C:\\data\\testFolder\\test.txt";
		parseSsfFile(fileName, "Books", "Scarlet", "1", "ka");
	}

	private static List<CitationBean> processSentence(String sentence,
			String sourceType, String source, String grade, String fileName) {
		String[] sentenceTokens = sentence.split(SENTENCE_SPLITTER);
		ArrayList<String> enhancedSentenceTokens = enhanceSentenceTokens(sentenceTokens);
		boolean wordFound = false;
		String word = null;
		String pos = null;
		int tokenCountAfterWord = 0;
		List<CitationBean> citationList = new ArrayList<CitationBean>();
		for (String token : enhancedSentenceTokens) {
			tokenCountAfterWord++;

			if (isTagName(token)) {
				if (wordFound && word != null) {
					pos = token;
				}
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
				pos = null;
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
							if (rootWord != null && !rootWord.isEmpty()) {
								CitationBean citationObj = new CitationBean(
										word, rootWord, pos,
										wordUtil.getFormattedDateTime(System
												.currentTimeMillis()),
										sourceType, source, grade, fileName);
								citationList.add(citationObj);
							}

							word = null;
							pos = null;
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
