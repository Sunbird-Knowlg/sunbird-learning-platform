package org.ekstep.language.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.cache.exception.GraphCacheErrorCodes;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * The class WordCacheUtil , provides functionality to cache word and arphabet
 * mapping into redis to retrieve arpabet from redis store based on any
 * given word or similar sound word set from redis store
 * 
 * @author Karthik
 */
public class WordCacheUtil {

	/** The LOGGER. */
	private static ILogger LOGGER = new PlatformLogger(WordCacheUtil.class.getName());

	/** The jedis pool. */
	private static JedisPool jedisPool;

	/** The max connections. */
	private static int maxConnections = 128;

	/** The host. */
	private static String host = "localhost";

	/** The port. */
	private static int port = 6379;

	/** The index. */
	private static int index = 0;

	/** The Constant WORD. */
	private static final String WORD = "WORD";

	/** The Constant ARPABET. */
	private static final String ARPABET = "ARPABET";

	/** The Constant KEY_SEPARATOR. */
	private static final String KEY_SEPARATOR = ":";

	/**
	 * Gets the word key to store/look word into redis cache .
	 *
	 * @param word
	 *            the word
	 * 
	 * @return the word key
	 */
	private static String getWordKey(String word) {
		return WORD + KEY_SEPARATOR + word;
	}

	/**
	 * Gets the arpabet key.
	 *
	 * @param arpabet
	 *            the arpabet
	 * 
	 * @return the arpabet key
	 */
	private static String getArpabetKey(String arpabet) {
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

	/**
	 * Gets the redis conncetion.
	 *
	 * @return the redis conncetion
	 */
	public static Jedis getRedisConncetion() {
		try {
			Jedis jedis = jedisPool.getResource();
			if (index > 0)
				jedis.select(index);
			return jedis;
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_CONNECTION_ERROR.name(), e.getMessage());
		}

	}

	/**
	 * Return redis connection.
	 *
	 * @param jedis
	 *            the jedis
	 */
	private static void returnConnection(Jedis jedis) {
		try {
			if (null != jedis)
				jedisPool.returnResource(jedis);
		} catch (Exception e) {
			throw new ServerException(GraphCacheErrorCodes.ERR_CACHE_CONNECTION_ERROR.name(), e.getMessage());
		}
	}

	/**
	 * Load word arpabet collection into redis from file input stream.
	 *
	 * @param wordsArpabetsStream
	 *            the words arpabets stream
	 */
	public static void loadWordArpabetCollection(InputStream wordsArpabetsStream) {
		LOGGER.log("inside loadWordArpabetCollection");
		Map<String, String> wordArpabetCacheMap = parseInputStream(wordsArpabetsStream);
		if (wordArpabetCacheMap.size() > 0) {
			loadRedisCache(wordArpabetCacheMap);
		}
		LOGGER.log("completed loadWordArpabetCollection");
	}

	/**
	 * Parses the input stream and convert them into Map of word and arpabet
	 * pairs.
	 *
	 * @param stream
	 *            the stream
	 * 
	 * @return the map
	 */
	private static Map<String, String> parseInputStream(InputStream stream) {
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = "  ";
		final int IDX_WORD_IN_SOURCE = 0;
		final int IDX_EQU_ARPABETS_IN_SOURCE = 1;
		String wordDetails[];
		Map<String, String> wordArpabetCacheMap = new HashMap<>();
		int count = 0;
		try {
			Reader reader = new InputStreamReader(stream, "UTF8");
			br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				count++;
				try {
					wordDetails = line.split(csvSplitBy);
					String word = wordDetails[IDX_WORD_IN_SOURCE];
					String equ_arpabets = wordDetails[IDX_EQU_ARPABETS_IN_SOURCE];
					// remove numeric character from arpabet sequence
					equ_arpabets = removeNumericChar(equ_arpabets);
					wordArpabetCacheMap.put(word, equ_arpabets);
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					continue;
				}
			}
			System.out.println(
					count + " words in input file, " + wordArpabetCacheMap.size() + " words get loaded into cache map");
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
		return wordArpabetCacheMap;
	}

	/**
	 * Load Map of word and arpabet pairs into redis cache.
	 *
	 * @param cacheMap
	 *            the cache map
	 */
	private static void loadRedisCache(Map<String, String> cacheMap) {
		Jedis jedis = getRedisConncetion();
		try {
			for (Entry<String, String> wordEntry : cacheMap.entrySet()) {
				String word = wordEntry.getKey();
				String arphabetsOfWord = wordEntry.getValue();

				// cache word to arpabets mapping
				String wordKey = getWordKey(word);
				jedis.set(wordKey, arphabetsOfWord);

				// cache words into starting of arpabets set
				String arpabets[] = arphabetsOfWord.split("\\s");
				String arpabetKey = getArpabetKey(arpabets[0]);
				jedis.sadd(arpabetKey, word);

			}
		} catch (Exception e) {
			throw new ServerException("ERR_CACHE_LOAD_WORDARPABETS_MAP", e.getMessage());
		} finally {
			returnConnection(jedis);
		}
	}

	/**
	 * Removes the numeric char.
	 *
	 * @param wordInArbabets
	 *            the word in arbabets
	 * 
	 * @return the string
	 */
	private static String removeNumericChar(String wordInArbabets) {
		return wordInArbabets.replaceAll("\\d", "");
	}

	/**
	 * Checks for split char.
	 *
	 * @param str
	 *            the str
	 * 
	 * @return true, if successful
	 */
	private static boolean hasSplitChar(String str) {
		if (str.matches("([a-zA-Z]?[\\s-_&]?[a-zA-Z]?)+"))
			return true;
		return false;
	}

	/**
	 * Builds the compound word.
	 *
	 * @param str
	 *            the input string
	 * 
	 * @return the string
	 */
	private static String buildCompoundWord(String str) {
		// if string has white-spaces in between
		str = str.replaceAll("(\\s)?&(\\s)?", "-");
		// if string has underscore
		str = str.replaceAll("_", "-");
		// if string has white-space in between
		str = str.replaceAll("\\s", "-");
		return str;
	}

	/**
	 * Gets the arpabets.
	 *
	 * @param word
	 *            the word
	 * 
	 * @return the arpabets
	 */
	public static String getArpabets(String word) {
		LOGGER.log("GetArpabets - word " + word);

		Jedis jedis = getRedisConncetion();
		word = word.toUpperCase();
		String wordKey = getWordKey(word);
		String arpabetsOfWord = null;
		try {
			arpabetsOfWord = jedis.get(wordKey);
			if (StringUtils.isBlank(arpabetsOfWord)) {
				// check word has any split character to represent compound-word
				if (hasSplitChar(word)) {
					word = buildCompoundWord(word);
					wordKey = getWordKey(word);
					arpabetsOfWord = jedis.get(wordKey);
					if (StringUtils.isBlank(arpabetsOfWord)) {
						// check for least possibility of compound word like
						// "subway" instead of "sub-way"
						word = word.replaceAll("-", "");
						wordKey = getWordKey(word);
						arpabetsOfWord = jedis.get(wordKey);
					}
				}
			}
		} catch (Exception e) {
			throw new ServerException("ERR_CACHE_GET_WORDARPABETS_MAP", e.getMessage());
		} finally {
			returnConnection(jedis);
		}

		LOGGER.log("GetArpabets - arpabets " + arpabetsOfWord);
		return arpabetsOfWord;
	}

	/**
	 * Gets the similar sound words.
	 *
	 * @param word
	 *            the word
	 * 
	 * @return the similar sound words
	 */
	public static Set<String> getSimilarSoundWords(String word) {
		LOGGER.log("GetSimilarSoundWords - word " + word);

		Jedis jedis = getRedisConncetion();
		String Arpabets = getArpabets(word);
		Set<String> similarSoundWords = null;
		if (Arpabets == null)
			return similarSoundWords;
		try {
			String[] arpabetArr = Arpabets.split("\\s");
			String arpabetsKey = getArpabetKey(arpabetArr[0]);
			similarSoundWords = jedis.smembers(arpabetsKey);
		} catch (Exception e) {
			throw new ServerException("ERR_CACHE_GET_SIMILARSOUNDWORDSET_MAP", e.getMessage());
		} finally {
			returnConnection(jedis);
		}
		LOGGER.log("GetSimilarSoundWords - similarSoundWords size " + similarSoundWords.size());

		return similarSoundWords;
	}
}
