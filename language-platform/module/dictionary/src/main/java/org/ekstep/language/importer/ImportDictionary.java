package org.ekstep.language.importer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ekstep.language.enums.Enums.ObjectType;
import org.ekstep.language.models.DictionaryObject;
import org.ekstep.language.models.SynsetModel;
import org.ekstep.language.models.WordModel;

import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;

public class ImportDictionary {

	private static ILogger LOGGER = PlatformLogManager.getLogger();

	private static final String CSV_SEPARATOR = ",";

	private static final Map<String, String> synsetPartOfSpeechMap = new HashMap<String, String>();
	{
		synsetPartOfSpeechMap.put("01", "Noun");
		synsetPartOfSpeechMap.put("02", "Adjective");
		synsetPartOfSpeechMap.put("03", "Verb");
		synsetPartOfSpeechMap.put("04", "Adverb");
	}

	@SuppressWarnings("unchecked")
	protected DictionaryObject transformIndoWordNetData(String languageId, String sourceType, InputStream stream) {
		double startTime = System.currentTimeMillis();
		System.out.println("Language-Platform | Importer | IWN | Start Time : " + startTime);
		// TODO: All the Codes should come from Configuration
		final String HYPERNYM_CODE = "1102";
		final String ANTONYM_CODE = "0000";
		final String HYPONYM_CODE = "1103";
		final String HOLONYM_CODE = "1150";
		final String MERONYM_CODE = "1140";

		final String MULTI_SYNSET_SAPERATOR = ",";

		// Indices in Source WordNet File
		final int IDX_MEMBER_WORDS = 3;
		final int IDX_ID_IN_SOURCE = 0;
		final int IDX_POS_IN_SOURCE = 1;

		BufferedReader br = null;
		Reader reader = null;
		String line = "";
		String csvSplitBy = " ";
		String csvExampleUsageSplitBy = "\u007c";
		String csvMemberWordSplitBy = ":";
		String synsetMemberWords = null;
		String[] synsetDetails = null;
		String[] synsetMemberWordList = null;
		String[] meaningAndUsage = null;
		List<WordModel> lstWord = new ArrayList<WordModel>();
		List<SynsetModel> lstSynset = new ArrayList<SynsetModel>();
		DictionaryObject dictionaryObject = new DictionaryObject();
		try {
			reader = new InputStreamReader(stream, "UTF8");
			br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				try {

					/***********************************************************************************
					 * 1. Split Each line through '|' a. First part will be
					 * synset and relation i. Split it through ' ' and will get
					 * info like (id, counts and synset) ii. Split [4] through
					 * ':' will give all the word of same sysnset b. Second part
					 * will contain meaning and usage i. Split it with ':'
					 ***********************************************************************************/

					// TODO: All the index ids should be a part of configuration
					SynsetModel synset = new SynsetModel();
					synsetDetails = line.split(csvSplitBy);
					synsetMemberWords = synsetDetails[IDX_MEMBER_WORDS];
					Pattern p = Pattern.compile(csvExampleUsageSplitBy, Pattern.LITERAL);
					meaningAndUsage = p.split(line);
					synset.setIdentifier(languageId + ':' + "S:" + synsetDetails[IDX_ID_IN_SOURCE]);
					// synset.setIdentifier(languageId + ':' + "S:" +
					// synsetDetails[899]);
					synset.setLanguageId(languageId);
					synset.setWordMember(synsetDetails[IDX_MEMBER_WORDS]);
					synset.setSourceType(sourceType.toString());
					synset.setIdInSource(synsetDetails[IDX_ID_IN_SOURCE]);
					synset.setUsage(meaningAndUsage[1].split(":")[1]);
					synset.setMeaning(meaningAndUsage[1].split(":")[0]);
					synset.setAntonymSynsetId(getSynsetIdByRelation(ANTONYM_CODE, line, ObjectType.Synset, languageId));
					synset.setHyponymSynsetId(getSynsetIdByRelation(HYPONYM_CODE, line, ObjectType.Synset, languageId));
					synset.setHypernymSynsetId(
							getSynsetIdByRelation(HYPERNYM_CODE, line, ObjectType.Synset, languageId));
					synset.setHolonymSynsetId(getSynsetIdByRelation(HOLONYM_CODE, line, ObjectType.Synset, languageId));
					synset.setMeronymSynsetId(getSynsetIdByRelation(MERONYM_CODE, line, ObjectType.Synset, languageId));
					synset.setPartOfSpeech(synsetPartOfSpeechMap.get(synsetDetails[IDX_POS_IN_SOURCE]));
					synsetMemberWordList = synsetMemberWords.split(csvMemberWordSplitBy);
					String CSVMemeberWordsId = "";
					for (int i = 0; i < synsetMemberWordList.length; i++) {
						final String currentWord = synsetMemberWordList[i];
						// TODO: Make the below search value for field as
						// generic method using reflections
						// Check for Duplicate Word
						Predicate condition = new Predicate() {
							public boolean evaluate(Object word) {
								return ((WordModel) word).getWordLemma().trim().equals(currentWord);
							}
						};
						// Search for duplicate word in list
						List<WordModel> results = (List<WordModel>) CollectionUtils.select(lstWord, condition);

						// New Word
						if (results.size() <= 0) {
							WordModel word = new WordModel();
							CSVMemeberWordsId += (languageId + ':' + "W:" + synsetDetails[IDX_ID_IN_SOURCE] + i)
									+ CSV_SEPARATOR;
							word.setIdentifier(languageId + ':' + "W:" + synsetDetails[IDX_ID_IN_SOURCE] + i);
							word.setLanguageId(languageId);
							word.setWordLemma(synsetMemberWordList[i].trim());
							word.setSourceType(sourceType.toString());
							word.setIdInSource(synsetDetails[IDX_ID_IN_SOURCE]);
							word.setUsage(meaningAndUsage[1]);
							word.setMeaning(meaningAndUsage[0]);
							word.setMemberOfSynsetId(languageId + ':' + "S:" + synsetDetails[IDX_ID_IN_SOURCE]);
							word.setAntonymOfWordId(
									getSynsetIdByRelation(ANTONYM_CODE, line, ObjectType.Word, languageId));
							word.setHyponymOfWordId(
									getSynsetIdByRelation(HYPONYM_CODE, line, ObjectType.Word, languageId));
							word.setHypernymOfWordId(
									getSynsetIdByRelation(HYPERNYM_CODE, line, ObjectType.Word, languageId));
							word.setHomonymOfWordId(
									getSynsetIdByRelation(HOLONYM_CODE, line, ObjectType.Word, languageId));
							word.setMeronymOfWordId(
									getSynsetIdByRelation(MERONYM_CODE, line, ObjectType.Word, languageId));
							lstWord.add(word);
						} else {
							// Duplicate Word
							System.out.println("Importer Module | Duplicate Word - " + currentWord);
							String identifier = null;
							for (WordModel result : results) {
								identifier = result.getIdentifier();
								if (null != identifier) {
									for (WordModel word : lstWord) {
										if (word.getIdentifier().equals(identifier)) {
											word.setMemberOfSynsetId(word.getMemberOfSynsetId() + MULTI_SYNSET_SAPERATOR
													+ languageId + ':' + "S:" + synsetDetails[IDX_ID_IN_SOURCE]);
											CSVMemeberWordsId += word.getIdentifier() + CSV_SEPARATOR;
											break;
										}
									}
								}
							}
						}
					}
					synset.setWordMember(CSVMemeberWordsId);
					lstSynset.add(synset);
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					continue;
				}
			}
			dictionaryObject.setLstSynset(lstSynset);
			dictionaryObject.setLstWord(lstWord);
			writeSynsetsToCSV(lstSynset);
			writeWordsToCSV(lstWord);
			double endTime = System.currentTimeMillis();
			System.out.println("Language-Platform | Importer | IWN | End Time : " + endTime);
			System.out.println("Language-Platform | Importer | IWN | Total Elapsed Time : " + (endTime - startTime));
			return dictionaryObject;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != br)
					br.close();
				if (null != reader)
					reader.close();
			} catch (IOException e) {
				LOGGER.log("Error! While Closing the Input Stream.", e.getMessage(), e);
			}
		}

		System.out.println("Done");
		return null;
	}

	public DictionaryObject transformData(String languageId, String sourceType, InputStream stream) {
		if (!isValidLanguageId(languageId)) {
			throw new IllegalArgumentException("Invalid Language Id.");
		} else if (!isValidSourceType(sourceType)) {
			throw new IllegalArgumentException("Invalid Data Source Type.");
		} else {
			DictionaryObject dictionaryObject = new DictionaryObject();
			switch (sourceType) {
			case "IndoWordNet":
				dictionaryObject = transformIndoWordNetData(languageId, sourceType, stream);
				break;

			default:
				break;
			}
			return dictionaryObject;
		}
	}

	protected boolean isValidLanguageId(String languageId) {
		try {
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean isValidSourceType(String sourceType) {
		try {
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void writeWordsToCSV(List<WordModel> wordList) throws IOException {
		String fileName = "Language_Platform_Words.csv";
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream("\\data\\" + fileName), StandardCharsets.UTF_8));
		bw.write("identifier,Lemma,objectType");
		bw.newLine();
		for (WordModel word : wordList) {
			StringBuffer oneLine = new StringBuffer();
			oneLine.append(word.getIdentifier() == null ? "" : StringEscapeUtils.escapeCsv(word.getIdentifier()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(word.getWordLemma() == null ? "" : StringEscapeUtils.escapeCsv(word.getWordLemma()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(ObjectType.Word.toString());
			bw.write(oneLine.toString());
			bw.newLine();
		}
		bw.flush();
		bw.close();
	}

	private static void writeSynsetsToCSV(List<SynsetModel> synsetList) {
		String fileName = "Language_Platform_Synsets.csv";
		try (BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream("\\data\\" + fileName), StandardCharsets.UTF_8))) {
			bw.write(
					"identifier,rel:synonym,rel:hasAntonym,rel:hasHyponym,rel:hasMeronym,rel:hasHolonym,rel:hasHypernym,gloss,exampleSentences,pos,objectType");
			bw.newLine();
			for (SynsetModel synset : synsetList) {
				StringBuffer oneLine = new StringBuffer();
				oneLine.append(
						synset.getIdentifier() == null ? "" : StringEscapeUtils.escapeCsv(synset.getIdentifier()));
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(
						synset.getWordMember() == null ? "" : StringEscapeUtils.escapeCsv(synset.getWordMember()));
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(synset.getAntonymSynsetId() == null ? ""
						: StringEscapeUtils.escapeCsv(synset.getAntonymSynsetId()));
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(synset.getHyponymSynsetId() == null ? ""
						: StringEscapeUtils.escapeCsv(synset.getHyponymSynsetId()));
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(synset.getMeronymSynsetId() == null ? ""
						: StringEscapeUtils.escapeCsv(synset.getMeronymSynsetId()));
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(synset.getHolonymSynsetId() == null ? ""
						: StringEscapeUtils.escapeCsv(synset.getHolonymSynsetId()));
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(synset.getHypernymSynsetId() == null ? ""
						: StringEscapeUtils.escapeCsv(synset.getHypernymSynsetId()));
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(synset.getMeaning() == null ? "" : StringEscapeUtils.escapeCsv(synset.getMeaning()));
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(synset.getUsage() == null ? "" : StringEscapeUtils.escapeCsv(synset.getUsage()));
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(
						synset.getPartOfSpeech() == null ? "" : StringEscapeUtils.escapeCsv(synset.getPartOfSpeech()));
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(ObjectType.Synset.toString());
				bw.write(oneLine.toString());
				bw.newLine();
			}
			bw.flush();
		} catch (UnsupportedEncodingException e) {
			LOGGER.log("Error! Unsupported File Encoding.", e.getMessage(), e);
		} catch (FileNotFoundException e) {
			LOGGER.log("Error! File Does not Exist.", e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.log("Error! While Handling the File.", e.getMessage(), e);
		}
	}

	protected String getSynsetIdByRelation(String relationId, String synsetDetail, ObjectType objectType,
			String languageId) {
		try {
			String id = null;
			relationId = ' ' + relationId + ' ';
			switch (objectType) {
			case Word:
				if (synsetDetail.indexOf(relationId) > 0) {
					int index = synsetDetail.indexOf(relationId);
					index += relationId.length();
					String[] tokens = synsetDetail.substring(index, synsetDetail.length()).trim().split(" ");
					id = tokens[0];
					if (id != null || id != "") {
						id = languageId + ':' + "W:" + id;
					}
					return id;
				}
				break;

			case Synset:
				if (synsetDetail.indexOf(relationId) > 0) {
					int index = synsetDetail.indexOf(relationId);
					index += relationId.length();
					String[] tokens = synsetDetail.substring(index, synsetDetail.length()).trim().split(" ");
					id = tokens[0];
					if (id != null || id != "") {
						id = languageId + ':' + "S:" + id;
					}
					return id;
				}
				break;
			default:
				break;
			}
			return id;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
