package org.ekstep.language.batch.mgr.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.batch.mgr.IWordnetCSVManager;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.util.BaseLanguageManager;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;

/**
 * The Class WordnetCSVManagerImpl.
 *
 * @author rayulu, amarnath
 */
@Component
public class WordnetCSVManagerImpl extends BaseLanguageManager implements IWordnetCSVManager {

	/** The Constant NEW_LINE_SEPARATOR. */
	private static final String NEW_LINE_SEPARATOR = "\n";

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(IWordnetCSVManager.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.batch.mgr.IWordnetCSVManager#createWordnetCitations(
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public Response createWordnetCitations(String languageId, String wordsCSV) {
		CSVFormat format = CSVFormat.DEFAULT;
		List<CSVRecord> wordRecords = readCSV(format, wordsCSV);
		if (null != wordRecords && !wordRecords.isEmpty()) {
			try {
				Map<String, String> wordnetIdMap = getWordNetIdMap(wordRecords);
				addCitationIndexes(languageId, wordnetIdMap);
				LOGGER.info("Citations created for " + wordnetIdMap.size() + " words");
			} catch (Exception e) {
				throw new ServerException("ERROR_ADDING_WORDNET_CITATIONS", e.getMessage(), e);
			}
		}
		return OK("status", "OK");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.batch.mgr.IWordnetCSVManager#addWordnetIndexes(java.
	 * lang.String, java.lang.String)
	 */
	@Override
	public Response addWordnetIndexes(String languageId, String wordsCSV) {
		CSVFormat format = CSVFormat.DEFAULT;
		List<CSVRecord> wordRecords = readCSV(format, wordsCSV);
		if (null != wordRecords && wordRecords.size() > 1) {
			try {
				Map<String, String> wordnetIdMap = new HashMap<String, String>();
				for (int i = 1; i < wordRecords.size(); i++) {
					CSVRecord record = wordRecords.get(i);
					String id = record.get(0);
					String lemma = record.get(1);
					if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(lemma))
						wordnetIdMap.put(id, lemma);
				}
				LOGGER.info("New indexes to be added: " + wordnetIdMap.size());
				addWordIndexes(languageId, wordnetIdMap);
				LOGGER.info("Indexes created for " + wordnetIdMap.size() + " words");
			} catch (Exception e) {
				throw new ServerException("ERROR_ADDING_WORDNET_CITATIONS", e.getMessage(), e);
			}
		}
		return OK("status", "OK");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.batch.mgr.IWordnetCSVManager#replaceWordnetIds(java.
	 * lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Response replaceWordnetIds(String languageId, String wordsCSV, String synsetCSV, String outputDir) {
		CSVFormat format = CSVFormat.DEFAULT;
		List<CSVRecord> wordRecords = readCSV(format, wordsCSV);
		List<CSVRecord> synsetRecords = readCSV(format, synsetCSV);
		if (null != wordRecords && !wordRecords.isEmpty()) {
			try {
				Map<String, String> wordnetIdMap = getWordNetIdMap(wordRecords);
				Map<String, String> wordIdMap = new HashMap<String, String>();
				Map<String, String> rootWordMap = new HashMap<String, String>();
				// Set<String> newWordnetIds = new HashSet<String>();
				Map<String, String> newWordnetIds = new HashMap<String, String>();
				getIndexInfo(languageId, wordnetIdMap, wordIdMap, rootWordMap, newWordnetIds);
				writeNewWordNetIds(newWordnetIds, outputDir);
				LOGGER.info("New Words CSV wrting completed");
				writeUpdatedWordsCSV(wordRecords, wordIdMap, rootWordMap, outputDir);
				LOGGER.info("Updated Words CSV wrting completed");
				writeUpdatedSynsetsCSV(synsetRecords, wordIdMap, outputDir);
				LOGGER.info("Updated Synsets CSV wrting completed");
			} catch (Exception e) {
				throw new ServerException("ERROR_ADDING_WORDNET_CITATIONS", e.getMessage(), e);
			}
		}
		return OK("status", "OK");
	}

	/**
	 * Read CSV.
	 *
	 * @param format
	 *            the format
	 * @param path
	 *            the path
	 * @return the list
	 */
	private List<CSVRecord> readCSV(CSVFormat format, String path) {
		try {
			File f = new File(path);
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isReader = new InputStreamReader(fis);
			CSVParser csvReader = new CSVParser(isReader, format);
			List<CSVRecord> recordsList = csvReader.getRecords();
			isReader.close();
			csvReader.close();
			return recordsList;
		} catch (Exception e) {
			throw new ServerException("ERROR_READING_CSV", e.getMessage(), e);
		}
	}

	/**
	 * Gets the word net id map.
	 *
	 * @param wordRecords
	 *            the word records
	 * @return the word net id map
	 */
	private Map<String, String> getWordNetIdMap(List<CSVRecord> wordRecords) {
		Map<String, String> wordnetIdMap = new HashMap<String, String>();
		if (null != wordRecords && !wordRecords.isEmpty()) {
			for (CSVRecord record : wordRecords) {
				if (record.size() >= 3) {
					String id = record.get(0);
					String lemma = record.get(2);
					if (StringUtils.isNotBlank(lemma) && StringUtils.isNotBlank(id))
						wordnetIdMap.put(lemma, id);
				}
			}
		}
		return wordnetIdMap;
	}

	/**
	 * Adds the citation indexes.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordnetIdMap
	 *            the wordnet id map
	 */
	private void addCitationIndexes(String languageId, Map<String, String> wordnetIdMap) {
		if (null != wordnetIdMap && !wordnetIdMap.isEmpty()) {
			Set<String> keys = wordnetIdMap.keySet();
			List<String> words = new ArrayList<String>(keys);
			int start = 0;
			int batch = 100;
			if (batch > words.size())
				batch = words.size();
			while (start < words.size()) {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for (int i = start; i < batch; i++) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("word", words.get(i));
					map.put("sourceType", "wordnets");
					map.put("source", "IndoWordnet");
					list.add(map);
				}
				Request langReq = getLanguageRequest(languageId, LanguageActorNames.INDEXES_ACTOR.name(),
						LanguageOperations.addCitationIndex.name());
				langReq.put(LanguageParams.citations.name(), list);
				getLanguageResponse(langReq, LOGGER);
				start += 100;
				batch += 100;
				if (batch > words.size())
					batch = words.size();
			}
		}
	}

	/**
	 * Adds the word indexes.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordnetIdMap
	 *            the wordnet id map
	 */
	private void addWordIndexes(String languageId, Map<String, String> wordnetIdMap) {
		if (null != wordnetIdMap && !wordnetIdMap.isEmpty()) {
			Set<String> keys = wordnetIdMap.keySet();
			List<String> words = new ArrayList<String>(keys);
			int start = 0;
			int batch = 100;
			if (batch > words.size())
				batch = words.size();
			while (start < words.size()) {
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();
				for (int i = start; i < batch; i++) {
					String id = words.get(i);
					String lemma = wordnetIdMap.get(id);
					if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(lemma)) {
						Map<String, String> map = new HashMap<String, String>();
						map.put("word", lemma);
						map.put("id", id);
						list.add(map);
					}
				}
				Request langReq = getLanguageRequest(languageId, LanguageActorNames.INDEXES_ACTOR.name(),
						LanguageOperations.addWordIndex.name());
				langReq.put(LanguageParams.words.name(), list);
				getLanguageResponse(langReq, LOGGER);
				start += 100;
				batch += 100;
				if (batch > words.size())
					batch = words.size();
			}
		}
	}

	/**
	 * Gets the index info.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordnetIdMap
	 *            the wordnet id map
	 * @param wordIdMap
	 *            the word id map
	 * @param rootWordMap
	 *            the root word map
	 * @param newWordnetIds
	 *            the new wordnet ids
	 * @return the index info
	 */
	@SuppressWarnings("unchecked")
	private void getIndexInfo(String languageId, Map<String, String> wordnetIdMap, Map<String, String> wordIdMap,
			Map<String, String> rootWordMap, Map<String, String> newWordnetIds) {
		if (null != wordnetIdMap && !wordnetIdMap.isEmpty()) {
			Map<String, Object> indexResults = new HashMap<String, Object>();
			Set<String> keys = wordnetIdMap.keySet();
			List<String> words = new ArrayList<String>(keys);
			int start = 0;
			int batch = 100;
			if (batch > words.size())
				batch = words.size();
			while (start < words.size()) {
				List<String> list = new ArrayList<String>();
				for (int i = start; i < batch; i++) {
					list.add(words.get(i));
				}
				Request langReq = getLanguageRequest(languageId, LanguageActorNames.INDEXES_ACTOR.name(),
						LanguageOperations.getIndexInfo.name());
				langReq.put(LanguageParams.words.name(), list);
				Response langRes = getLanguageResponse(langReq, LOGGER);
				if (!checkError(langRes)) {
					Map<String, Object> map = (Map<String, Object>) langRes.get(LanguageParams.index_info.name());
					if (null != map && !map.isEmpty())
						indexResults.putAll(map);
				}
				start += 100;
				batch += 100;
				if (batch > words.size())
					batch = words.size();
			}
			for (Entry<String, String> entry : wordnetIdMap.entrySet()) {
				String lemma = entry.getKey();
				String wordnetId = entry.getValue();
				Map<String, Object> index = (Map<String, Object>) indexResults.get(lemma);
				if (null != index && !index.isEmpty()) {
					String wordId = (String) index.get("wordId");
					String root = (String) index.get("rootWord");
					if (StringUtils.isNotBlank(wordId))
						wordIdMap.put(wordnetId, wordId);
					if (StringUtils.isNotBlank(root))
						rootWordMap.put(wordnetId, root);
				} else
					newWordnetIds.put(wordnetId, lemma);
			}
		}
	}

	/**
	 * Write new word net ids.
	 *
	 * @param newWordnetIds
	 *            the new wordnet ids
	 * @param outputDir
	 *            the output dir
	 */
	private void writeNewWordNetIds(Map<String, String> newWordnetIds, String outputDir) {
		if (null != newWordnetIds && newWordnetIds.size() > 0) {
			try {
				String[] headerRows = new String[] { "identifier", "lemma" };
				List<String[]> rows = new ArrayList<String[]>();
				rows.add(headerRows);
				for (Entry<String, String> entry : newWordnetIds.entrySet()) {
					if (StringUtils.isNotBlank(entry.getKey())) {
						String[] row = new String[2];
						row[0] = entry.getKey();
						row[1] = entry.getValue();
						rows.add(row);
					}
				}
				CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
				FileWriter fw = new FileWriter(new File(outputDir + File.separator + "new_words.csv"));
				CSVPrinter writer = new CSVPrinter(fw, csvFileFormat);
				writer.printRecords(rows);
				writer.close();
			} catch (Exception e) {
				throw new ServerException("ERROR_WRITING_WORDS_CSV", e.getMessage(), e);
			}
		}
	}

	/**
	 * Write updated words CSV.
	 *
	 * @param wordRecords
	 *            the word records
	 * @param wordIdMap
	 *            the word id map
	 * @param rootWordMap
	 *            the root word map
	 * @param outputDir
	 *            the output dir
	 */
	private void writeUpdatedWordsCSV(List<CSVRecord> wordRecords, Map<String, String> wordIdMap,
			Map<String, String> rootWordMap, String outputDir) {
		if (null != wordRecords && wordRecords.size() > 0) {
			try {
				String[] headerRows = new String[] { "identifier", "objectType", "lemma", "sources", "sourceTypes" };
				List<String[]> rows = new ArrayList<String[]>();
				rows.add(headerRows);
				CSVRecord headerRecord = wordRecords.get(0);
				int headersize = headerRecord.size();
				for (int i = 1; i < wordRecords.size(); i++) {
					CSVRecord record = wordRecords.get(i);
					if (record.size() == headersize) {
						String[] row = new String[5];
						String wordnetId = record.get(0);
						String lemma = record.get(2);
						String wordId = wordIdMap.get(wordnetId);
						if (StringUtils.isNotBlank(wordId))
							row[0] = wordId;
						else
							row[0] = wordnetId;
						row[1] = record.get(1);
						String root = rootWordMap.get(wordnetId);
						if (StringUtils.isNotBlank(root))
							row[2] = root;
						else
							row[2] = lemma;
						row[3] = "IndoWordnet";
						row[4] = "wordnets";
						rows.add(row);
					}
				}
				CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
				FileWriter fw = new FileWriter(new File(outputDir + File.separator + "words.csv"));
				CSVPrinter writer = new CSVPrinter(fw, csvFileFormat);
				writer.printRecords(rows);
				writer.close();
			} catch (Exception e) {
				throw new ServerException("ERROR_WRITING_WORDS_CSV", e.getMessage(), e);
			}
		}
	}

	/**
	 * Write updated synsets CSV.
	 *
	 * @param synsetRecords
	 *            the synset records
	 * @param wordIdMap
	 *            the word id map
	 * @param outputDir
	 *            the output dir
	 */
	private void writeUpdatedSynsetsCSV(List<CSVRecord> synsetRecords, Map<String, String> wordIdMap,
			String outputDir) {
		if (null != synsetRecords && synsetRecords.size() > 0) {
			try {
				String[] headerRows = new String[] { "identifier", "objectType", "rel:synonym", "rel:hasAntonym",
						"rel:hasHyponym", "rel:hasMeronym", "rel:hasHolonym", "rel:hasHypernym", "gloss",
						"exampleSentences", "pos" };
				List<String[]> rows = new ArrayList<String[]>();
				rows.add(headerRows);
				CSVRecord headerRecord = synsetRecords.get(0);
				int headersize = headerRecord.size();
				Map<String, Integer> headerMap = new HashMap<String, Integer>();
				for (int i = 0; i < headerRecord.size(); i++) {
					headerMap.put(headerRecord.get(i), i);
				}
				for (int i = 1; i < synsetRecords.size(); i++) {
					CSVRecord record = synsetRecords.get(i);
					if (record.size() == headersize) {
						String[] row = new String[11];
						for (int j = 0; j < headerRows.length; j++) {
							String header = headerRows[j];
							if (header.startsWith("rel:")) {
								String prevValue = record.get(headerMap.get(header));
								String newValue = "";
								if (StringUtils.isNotBlank(prevValue)) {
									String[] arr = prevValue.split(",");
									for (String id : arr) {
										String wordId = wordIdMap.get(id);
										if (StringUtils.isNotBlank(wordId))
											newValue += wordId;
										else
											newValue += id;
										newValue += ",";
									}
								}
								row[j] = newValue;
							} else {
								row[j] = record.get(headerMap.get(header));
							}
						}
						rows.add(row);
					}
				}
				CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
				FileWriter fw = new FileWriter(new File(outputDir + File.separator + "synsets.csv"));
				CSVPrinter writer = new CSVPrinter(fw, csvFileFormat);
				writer.printRecords(rows);
				writer.close();
			} catch (Exception e) {
				throw new ServerException("ERROR_WRITING_SYNSET_CSV", e.getMessage(), e);
			}
		}
	}
}
