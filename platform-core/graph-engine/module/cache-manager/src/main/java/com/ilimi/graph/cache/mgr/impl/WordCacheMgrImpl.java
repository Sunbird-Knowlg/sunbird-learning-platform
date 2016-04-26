package com.ilimi.graph.cache.mgr.impl;

import static com.ilimi.graph.cache.factory.JedisFactory.getRedisConncetion;
import static com.ilimi.graph.cache.factory.JedisFactory.returnConnection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;
import com.ilimi.graph.cache.util.RedisKeyGenerator;
import com.ilimi.graph.dac.enums.GraphDACParams;

import redis.clients.jedis.Jedis;

public class WordCacheMgrImpl {

	public void loadWordArpabetCollection(Request request){
		InputStream wordsArpabetsStream = (InputStream) request.get("wordsArpabetsStream");
		Map<String, String> wordArpabetCacheMap=parseInputStream(wordsArpabetsStream);
		if(wordArpabetCacheMap.size()>0)
			loadRedisCache(wordArpabetCacheMap);
	}
	
	private Map<String, String> parseInputStream(InputStream stream){
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = "  ";
		final int IDX_WORD_IN_SOURCE = 0;
		final int IDX_EQU_ARPABETS_IN_SOURCE = 1;
		String wordDetails[];
		Map<String, String> wordArpabetCacheMap=new HashMap<>();
		int count=0;
        try {
			Reader reader = new InputStreamReader(stream, "UTF8");
			br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				count++;
				try {
					wordDetails=line.split(csvSplitBy);
					String word=wordDetails[IDX_WORD_IN_SOURCE];
					String equ_arpabets=wordDetails[IDX_EQU_ARPABETS_IN_SOURCE];
					equ_arpabets=removeNumericChar(equ_arpabets);
					wordArpabetCacheMap.put(word, equ_arpabets);	
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					continue;
				}
			}
			System.out.println(count+" words in input file, "+wordArpabetCacheMap.size()+" words get loaded into cache map");
		}catch (FileNotFoundException e) {
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
        return wordArpabetCacheMap;
	}
	
	private void loadRedisCache(Map<String, String> cacheMap){
        Jedis jedis = getRedisConncetion();
        try {
	        for(Entry<String,String> wordEntry:cacheMap.entrySet()){
	        	String word=wordEntry.getKey();
	        	String arphabetsOfWord=wordEntry.getValue();
	            String wordKey=RedisKeyGenerator.getWordKey(word);
	            jedis.set(wordKey, arphabetsOfWord);
	        }
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_LOAD_WORDARPABETS_MAP.name(), e.getMessage());
        } finally {
            returnConnection(jedis);
        }
	}

	private String removeNumericChar(String wordInArbabets){
		return wordInArbabets.replaceAll("\\d", "");
	}
	
	
	private boolean hasSplitChar(String str){		
		if(str.matches("([a-zA-Z]?[\\s-_&]?[a-zA-Z]?)+"))
			return true;
		return false;
	}
	
	private String buildCompoundWord(String str){
		str=str.replaceAll("(\\s)?&(\\s)?", "-");
		str=str.replaceAll("_", "-");
		str=str.replaceAll("\\s", "-");
		return str;
	}
	
	public String getArpabetsOfWord(Request request){
		String word=(String) request.get(GraphDACParams.WORD.name());
		Jedis jedis = getRedisConncetion();
		word=word.toUpperCase();
		String wordKey=RedisKeyGenerator.getWordKey(word);
		String arpabetsOfWord=jedis.get(wordKey);
		if(StringUtils.isEmpty(arpabetsOfWord)){
			if(hasSplitChar(word)){
				word=buildCompoundWord(word);
				wordKey=RedisKeyGenerator.getWordKey(word);
				arpabetsOfWord=jedis.get(wordKey);
			}
		}
		
		return arpabetsOfWord;
	}
	

	private Set<String> returnAllEnglishVarna(InputStream is){
		Set<String> varnaSet=new HashSet<String>();
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";
		final int IDX_VARNA_IN_SOURCE = 0;
		int count=0;
		try {
			Reader reader = new InputStreamReader(is, "UTF8");
			br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				if(++count==1)continue;
				String[] wordDetails=line.split(csvSplitBy);
				varnaSet.add(wordDetails[IDX_VARNA_IN_SOURCE]);
			}
		}catch (FileNotFoundException e) {
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
		return varnaSet;
	}
	
	private Set<String> findArpabets(Set<String> arpabetsWordSet){
		Set<String> Arpabets=new HashSet<String>();
		for(String arpabets:arpabetsWordSet){
			String sArr[]=arpabets.split("\\s");
			for(String s:sArr){
				Arpabets.add(s);
			}
		}
		return Arpabets;
	}
	
	public static void main(String[] arg){
		System.out.println("test");
		try{
			InputStream is = new FileInputStream("/Users/karthik/Desktop/Word_PhoneticSpeling.txt");
			WordCacheMgrImpl wordManager=new WordCacheMgrImpl();
			Map<String, String> wordArpabetCacheMap=wordManager.parseInputStream(is);
			Set<String> arpabetsWordSet=new HashSet<String>(wordArpabetCacheMap.values());
			Set<String> arpabetSetFromWords=wordManager.findArpabets(arpabetsWordSet);
			
			InputStream fis = new FileInputStream("/Users/karthik/Documents/workspace/ekstep/language-platform/module/dictionary/src/test/resources/English_Varna.csv");
			Set<String> EnglishVarnaSet=wordManager.returnAllEnglishVarna(fis);
			Set<String> newVarnas=null;
			if(arpabetSetFromWords.size()>EnglishVarnaSet.size()){
				arpabetSetFromWords.removeAll(EnglishVarnaSet);
				newVarnas=arpabetSetFromWords;
			}else{
				if(!EnglishVarnaSet.containsAll(arpabetSetFromWords)){
					EnglishVarnaSet.removeAll(arpabetSetFromWords);
					newVarnas=EnglishVarnaSet;
				}
			}
			
			if(CollectionUtils.isNotEmpty(newVarnas)){
				wordManager.writeToFile(newVarnas);
			}
//			WordCacheMgrImpl impl=new WordCacheMgrImpl();
//			String str="sub_way&hellow";
//			System.out.println(str);
//			if(impl.hasSplitChar(str))
//				str=impl.buildCompoundWord(str);
//			System.out.println(str);

		} catch(Exception ex){
			
		}
	}

	private void writeToFile(Set<String> newVarna){
		Writer writer = null;
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("/Users/karthik/Desktop/English_Arpabets_new.txt"), "utf-8"));
		    for(String varna:newVarna){
			    writer.write(varna+"\n");
		    }
		} catch (IOException ex) {

		} finally {
		   try {
			   writer.close();
		   }
		   catch (Exception ex) {
			   
		   }
		}
	}
}
