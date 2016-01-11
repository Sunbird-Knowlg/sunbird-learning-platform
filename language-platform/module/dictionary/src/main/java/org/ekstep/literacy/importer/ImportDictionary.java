package org.ekstep.literacy.importer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.*;

import org.ekstep.literacy.models.WordModel;
import org.ekstep.literacy.enums.Enums.ObjectType;
import org.ekstep.literacy.enums.Enums.SourceType;
import org.ekstep.literacy.models.SynsetModel;

public class ImportDictionary {

	private static final String CSV_SEPARATOR = ",";

//	public static void main(String args[]) {
//		ImportDictionary id = new ImportDictionary();
//		id.importData("hi", SourceType.IndoWordNet, "/Users/ilimi/PerceptronWorkspace/EkStep/Learning-Platform/language-platform/module/dictionary/src/main/resources/data.txt");
//	}

	@SuppressWarnings("unchecked")
	protected void importIndoWordNetData(String languageId, SourceType sourceType, String csvFileUrl) {
		double  startTime = System.currentTimeMillis();
		System.out.println("Language-Platform | Importer | IWN | Start Time : " + startTime);
		// TODO: All the Codes should come from Configuration
		final String HYPERNYM_CODE = "0000";
		final String ANTONYM_CODE = "0000";
		final String HYPONYM_CODE = "0000";
		final String HOMONYM_CODE = "0000";
		final String MERONYM_CODE = "0000";

		final String MULTI_SYNSET_SAPERATOR = "|";

		//Indices in Source WordNet File
		final int IDX_MEMBER_WORDS = 3;
		final int IDX_ID_IN_SOURCE = 0;

		BufferedReader br = null;
		String line = "";
		String csvSplitBy = " ";
		String csvExampleUsageSplitBy = "|";
		String csvMemberWordSplitBy = ":";
		String synsetMemberWords = null;
		String[] synsetDetails = null;
		String[] synsetMemberWordList = null;
		String[] meaningAndUsage = null;
		List<WordModel> wordList = new ArrayList<WordModel>();
		List<SynsetModel> synsetList = new ArrayList<SynsetModel>();

		try {

			br = new BufferedReader(new FileReader(csvFileUrl));
			while ((line = br.readLine()) != null) {

				/***********************************************************************************
				 * 1. Split Each line through '|'
				 * 		a. First part will be synset and relation
				 * 			i. Split it through ' ' and will get info like (id, counts and synset)
				 * 			ii. Split [4] through ':' will give all the word of same sysnset
				 * 		b. Second part will contain meaning and usage
				 * 			i. Split it with ':'
				 ***********************************************************************************/

				// TODO: All the index ids should be a part of configuration

				SynsetModel synset = new SynsetModel();
				synsetDetails = line.split(csvSplitBy);
				synsetMemberWords = synsetDetails[IDX_MEMBER_WORDS];
				meaningAndUsage = line.split(csvExampleUsageSplitBy);
				synset.setIdentifier(languageId + ':' + "S:" + synsetDetails[IDX_ID_IN_SOURCE]);
				synset.setLanguageId(languageId);
				synset.setWordMember(synsetDetails[IDX_MEMBER_WORDS]);
				synset.setSourceType(sourceType.toString());
				synset.setIdInSource(synsetDetails[IDX_ID_IN_SOURCE]);
				synset.setUsage(meaningAndUsage[1]);
				synset.setMeaning(meaningAndUsage[0]);
				synset.setAntonymSynsetId(getSynsetIdByRelation(ANTONYM_CODE, line, ObjectType.Synset));
				synset.setHyponymSynsetId(getSynsetIdByRelation(HYPONYM_CODE, line, ObjectType.Synset));
				synset.setHypernymSynsetId(getSynsetIdByRelation(HYPERNYM_CODE, line, ObjectType.Synset));
				synset.setHomonymSynsetId(getSynsetIdByRelation(HOMONYM_CODE, line, ObjectType.Synset));
				synset.setMeronymSynsetId(getSynsetIdByRelation(MERONYM_CODE, line, ObjectType.Synset));
				synsetList.add(synset);
				synsetMemberWordList = synsetMemberWords.split(csvMemberWordSplitBy);
				for (int i = 0; i < synsetMemberWordList.length; i++) {
					final String currentWord = synsetMemberWordList[i];
					// TODO: Make the below search value for field as generic method using reflections
					Predicate condition = new Predicate() {
						   public boolean evaluate(Object word) {
						        return ((WordModel)word).getWordLemma().trim().equals(currentWord);
						   }
					};
					List<WordModel> results = (List<WordModel>) CollectionUtils.select(wordList, condition);

					if (results.size() <= 0) {
						WordModel word = new WordModel();
						word.setIdentifier(languageId + ':' + "W:" + synsetDetails[IDX_ID_IN_SOURCE] + i);
						word.setLanguageId(languageId);
						word.setWordLemma(synsetMemberWordList[i].trim());
						word.setSourceType(sourceType.toString());
						word.setIdInSource(synsetDetails[IDX_ID_IN_SOURCE]);
						word.setUsage(meaningAndUsage[1]);
						word.setMeaning(meaningAndUsage[0]);
						word.setMemberOfSynsetId(languageId + ':' + "S:" + synsetDetails[IDX_ID_IN_SOURCE]);
						word.setAntonymOfWordId(getSynsetIdByRelation(ANTONYM_CODE, line, ObjectType.Word));
						word.setHyponymOfWordId(getSynsetIdByRelation(HYPONYM_CODE, line, ObjectType.Word));
						word.setHypernymOfWordId(getSynsetIdByRelation(HYPERNYM_CODE, line, ObjectType.Word));
						word.setHomonymOfWordId(getSynsetIdByRelation(HOMONYM_CODE, line, ObjectType.Word));
						word.setMeronymOfWordId(getSynsetIdByRelation(MERONYM_CODE, line, ObjectType.Word));
						wordList.add(word);
					} else {
						System.out.println("Importer Module | Duplicate Word - " + currentWord);
						String identifier = null;
						for (WordModel result : results) {
							identifier = result.getIdentifier();
							if (null != identifier) {
								for (WordModel word : wordList) {
									if (word.getIdentifier().equals(identifier)) {
										word.setMemberOfSynsetId(word.getMemberOfSynsetId() + MULTI_SYNSET_SAPERATOR + languageId + ':' + "S:" + synsetDetails[IDX_ID_IN_SOURCE]);
									}
								}
							}
						}
					}
				}
			}
			writeSynsetsToCSV(synsetList);
			writeWordsToCSV(wordList);
			double endTime = System.currentTimeMillis();
			System.out.println("Language-Platform | Importer | IWN | End Time : " + endTime);
			System.out.println("Language-Platform | Importer | IWN | Total Elapsed Time : " + (endTime - startTime));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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

		System.out.println("Done");
	}

	protected void importData(String languageId, SourceType sourceType, String csvFileUrl) {
		if (!isValidLanguageId(languageId)) {
			throw new IllegalArgumentException("Invalid Language Id.");
		}else if (!isValidSourceType(sourceType)) {
			throw new IllegalArgumentException("Invalid Data Source Type.");
		}else{
			try {
				switch (sourceType) {
				case IndoWordNet:
					importIndoWordNetData(languageId, sourceType, csvFileUrl);
					break;

				default:
					break;
				}
			} catch(Exception e) {}
		}
	}

	protected boolean isValidLanguageId(String languageId) {
		try {
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean isValidSourceType(SourceType sourceType) {
		try {
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

    private static void writeWordsToCSV(List<WordModel> wordList)
    {
        try
        {
        	String fileName = "Language_Platform_Words.csv";
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
            bw.write("identifier, rel:isMemberOf, rel:hasAntonym, rel:hasHyponym, rel:hasMeronym, rel:hasHomonym, rel:hasHypernym, Lemma, objectType, code, description, source, languageIdentifier");
            bw.newLine();
            for (WordModel word : wordList)
            {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(word.getIdentifier() == null ? "" : word.getIdentifier());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(word.getMemberOfSynsetId() == null ? "" : word.getMemberOfSynsetId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(word.getAntonymOfWordId() == null ? "" : word.getAntonymOfWordId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(word.getHyponymOfWordId() == null ? "" : word.getHyponymOfWordId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(word.getMeronymOfWordId() == null ? "" : word.getMeronymOfWordId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(word.getHomonymOfWordId() == null ? "" : word.getHomonymOfWordId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(word.getHypernymOfWordId() == null ? "" : word.getHypernymOfWordId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(word.getWordLemma() == null ? "" : word.getWordLemma());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(ObjectType.Word.toString());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(word.getIdentifier() == null ? "" : word.getIdentifier());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(word.getMeaning() == null ? "" : word.getMeaning());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(word.getSourceType() == null ? "" : word.getSourceType());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(word.getLanguageId() == null ? "" : word.getLanguageId());
                oneLine.append(CSV_SEPARATOR);
                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        }
        catch (UnsupportedEncodingException e) {}
        catch (FileNotFoundException e){}
        catch (IOException e){}
    }

    private static void writeSynsetsToCSV(List<SynsetModel> synsetList)
    {
        try
        {
        	String fileName = "Language_Platform_Synsets.csv";
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
            bw.write("identifier, rel:isAntonymOf, rel:hasHyponym, rel:hasMeronym, rel:hasHomonym, rel:hasHypernym, wordMember, objectType, code, description, source, languageIdentifier");
            bw.newLine();
            for (SynsetModel synset : synsetList)
            {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(synset.getIdentifier() == null ? "" : synset.getIdentifier());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(synset.getAntonymSynsetId() == null ? "" : synset.getIdentifier());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(synset.getHyponymSynsetId() == null ? "" : synset.getAntonymSynsetId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(synset.getMeronymSynsetId() == null ? "" : synset.getMeronymSynsetId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(synset.getHomonymSynsetId() == null ? "" : synset.getHomonymSynsetId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(synset.getHypernymSynsetId() == null ? "" : synset.getHypernymSynsetId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(synset.getWordMember() == null ? "" : synset.getWordMember());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(ObjectType.Synset.toString());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(synset.getIdentifier() == null ? "" : synset.getIdentifier());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(synset.getMeaning() == null ? "" : synset.getMeaning());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(synset.getSourceType() == null ? "" : synset.getSourceType());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(synset.getLanguageId() == null ? "" : synset.getLanguageId());
                oneLine.append(CSV_SEPARATOR);
                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        }
        catch (UnsupportedEncodingException e) {}
        catch (FileNotFoundException e){}
        catch (IOException e){}
    }

	protected String getSynsetIdByRelation(String relationId, String synsetDetail, ObjectType objectType ) {
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
					  return id;
				}
				break;

			case Synset:
				if (synsetDetail.indexOf(relationId) > 0) {
					  int index = synsetDetail.indexOf(relationId);
					  index += relationId.length();
					  String[] tokens = synsetDetail.substring(index, synsetDetail.length()).trim().split(" ");
					  id = tokens[0];
					  return id;
				}
				break;
			default:
				break;
			}
			return id;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
