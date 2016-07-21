package org.ekstep.language.util;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component
public class WordCacheUtil {

    private static JedisPool jedisPool;

    private static int maxConnections = 128;
    private static String host = "localhost";
    private static int port = 6379;
    private static int index = 0;
    
    private static final String WORD="WORD";
    private static final String ARPABET="ARPABET";
    private static final String KEY_SEPARATOR = ":";
    
    private String getWordKey(String word){
    	return WORD + KEY_SEPARATOR + word;
    }

    private String getArpabetKey(String arpabet){
    	return ARPABET + KEY_SEPARATOR + arpabet;
    }
    
    static {
            String redisHost = PropertiesUtil.getProperty("redis.host");
            if (StringUtils.isNotBlank(redisHost))
                host = redisHost;
            String redisPort = PropertiesUtil.getProperty("redis.port");
            if (StringUtils.isNotBlank(redisPort)) {
                port = Integer.parseInt(redisPort);
            }
            String redisMaxConn = PropertiesUtil.getProperty("redis.maxConnections");
            if (StringUtils.isNotBlank(redisMaxConn)) {
                maxConnections = Integer.parseInt(redisMaxConn);
            }
            String dbIndex = PropertiesUtil.getProperty("redis.dbIndex");
            if (StringUtils.isNotBlank(dbIndex)) {
                    index = Integer.parseInt(dbIndex);
            }
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxConnections);
        config.setBlockWhenExhausted(true);
        jedisPool = new JedisPool(config, host, port);
    }

    public Jedis getRedisConncetion() {
        try {
            Jedis jedis = jedisPool.getResource();
            if(index > 0) jedis.select(index);
            return jedis;
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_CONNECTION_ERROR.name(), e.getMessage());
        }

    }

    private void returnConnection(Jedis jedis) {
        try {
            if (null != jedis)
                jedisPool.returnResource(jedis);
        } catch (Exception e) {
            throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_CONNECTION_ERROR.name(), e.getMessage());
        }
    }

	public void loadWordArpabetCollection(InputStream wordsArpabetsStream){
		Map<String, String> wordArpabetCacheMap=parseInputStream(wordsArpabetsStream);
		//Map<String, Set<String>> arpabetToWordSetMap=loadArpabetToWordSetMapping(wordArpabetCacheMap);
		if(wordArpabetCacheMap.size()>0){
			loadRedisCache(wordArpabetCacheMap);
			
		}

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
	            
	        	//cache word to arpabets mapping  
	        	String wordKey=getWordKey(word);
	            jedis.set(wordKey, arphabetsOfWord);
	            
	            //cache words into starting of arpabets set
	            String arpabets[]=arphabetsOfWord.split("\\s");
	            String arpabetKey=getArpabetKey(arpabets[0]);
	            jedis.sadd(arpabetKey, word);

	        }
        } catch (Exception e) {
            throw new ServerException("ERR_CACHE_LOAD_WORDARPABETS_MAP", e.getMessage());
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
	
	public String getArpabets(String word){
		Jedis jedis = getRedisConncetion();
		word=word.toUpperCase();
		String wordKey=getWordKey(word);
		String arpabetsOfWord=null;
		try{
			arpabetsOfWord=jedis.get(wordKey);
			if(StringUtils.isEmpty(arpabetsOfWord)){
				if(hasSplitChar(word)){
					word=buildCompoundWord(word);
					wordKey=getWordKey(word);
					arpabetsOfWord=jedis.get(wordKey);
					if(StringUtils.isEmpty(arpabetsOfWord)){
						word=word.replaceAll("-", "");
						wordKey=getWordKey(word);
						arpabetsOfWord=jedis.get(wordKey);
					}
				}
			}
		} catch (Exception e) {
	        throw new ServerException("ERR_CACHE_GET_WORDARPABETS_MAP", e.getMessage());
	    } finally {
	        returnConnection(jedis);
	    }
		
		return arpabetsOfWord;
	}
	
	public Set<String> getSimilarSoundWords(String word){
		Jedis jedis = getRedisConncetion();
		String Arpabets=getArpabets(word);
		Set<String> similarSoundWords=null;
		if(Arpabets==null)
			return similarSoundWords;
		try{
			String[] arpabetArr=Arpabets.split("\\s");
			String arpabetsKey=getArpabetKey(arpabetArr[0]);
			similarSoundWords=jedis.smembers(arpabetsKey);
		} catch (Exception e) {
	        throw new ServerException("ERR_CACHE_GET_SIMILARSOUNDWORDSET_MAP", e.getMessage());
	    } finally {
	        returnConnection(jedis);
	    }
		
		return similarSoundWords;
	}

	
	public static void main(String[] arg){
		System.out.println("test");
		try{
			InputStream is = new FileInputStream("/Users/karthik/Desktop/Word_PhoneticSpeling.txt");
			WordCacheUtil wordCahceManager=new WordCacheUtil();
			Map<String, String> wordArpabetCacheMap=wordCahceManager.parseInputStream(is);
			Set<String> arpabetsWordSet=new HashSet<String>(wordArpabetCacheMap.values());
			Set<String> arpabetSetFromWords=wordCahceManager.findArpabets(arpabetsWordSet);
			
			InputStream fis = new FileInputStream("/Users/karthik/Documents/workspace/ekstep/language-platform/module/dictionary/src/test/resources/English_Varna.csv");
			Set<String> EnglishVarnaSet=wordCahceManager.returnAllEnglishVarna(fis);
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
				wordCahceManager.writeToFile(newVarnas);
			}
//			WordCacheUtil impl=new WordCacheUtil();
//			String str="sub_way&hellow";
//			System.out.println(str);
//			if(impl.hasSplitChar(str))
//				str=impl.buildCompoundWord(str);
//			System.out.println(str);

		} catch(Exception ex){
			
		}
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
