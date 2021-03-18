package org.sunbird.graph.service.util;

import java.util.HashMap;
import java.util.Map;

import org.sunbird.common.Platform;
import org.sunbird.graph.cache.util.CacheKeyGenerator;
import org.sunbird.telemetry.logger.TelemetryManager;

/**
 * The Class LocalCache, is used to cache any object in current java instance
 * local heap until the cached object time to live(ttl) expired as per
 * configured ttl
 *
 * @author karthik
 */
public class LocalCache {

	/** The cache map. */
	private static Map<String, CacheObject> cacheMap = null;

	/** The ttl. */
	private static long ttl = 36000000; // default 10 hour

	/**
	 * The Class CacheObject.
	 *
	 * @author karthik
	 */
	protected static class CacheObject {

		/** The create on. */
		public long createOn = System.currentTimeMillis();

		/** The value. */
		public Object value;

		/**
		 * Instantiates a new cache object.
		 *
		 * @param value
		 *            the value
		 */
		protected CacheObject(Object value) {
			this.value = value;
			TelemetryManager.log("Local cache object value=" + value.toString() + "createOn=" + createOn);
		}
	}

	static {
		init();
	}

	/**
	 * Inits the.
	 */
	public static void init() {
		cacheMap = new HashMap<String, CacheObject>();
		setTTL();
	}

	/**
	 * Gets the.
	 *
	 * @param key
	 *            the key
	 * @return the object
	 */
	public static Object get(String key) {
		CacheObject c = (CacheObject) cacheMap.get(key);

		if (c == null)
			return null;
		else {
			long now = System.currentTimeMillis();
			if (now - c.createOn > ttl) {
				TelemetryManager.log("Local cache object value=" + c.value.toString() + "createOn=" + c.createOn
						+ " is expired now");
				cacheMap.remove(key);
				return null;
			}
			return c.value;
		}
	}

	/**
	 * Sets the.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static void set(String key, Object value) {

		CacheObject cacheObj = new CacheObject(value);
		cacheMap.put(key, cacheObj);
		TelemetryManager.log("Local cache object value=" + cacheObj.value.toString() + "cacheObj.createOn="
				+ cacheObj.createOn + " is set now");
	}

	/**
	 * sets the ttl.
	 *
	 * @return the ttl
	 */
	private static void setTTL() {
		if(Platform.config.hasPath("platform.cache.ttl"))
			ttl = Long.parseLong(Platform.config.getString("platform.cache.ttl"));
	}

	/**
	 * Gets the def node property.
	 *
	 * @param graphId
	 *            the graph id
	 * @param objectType
	 *            the object type
	 * @param nodeProperty
	 *            the node property
	 * @return the def node property
	 */
	public static String getDefNodeProperty(String graphId, String objectType, String nodeProperty) {
		Object value = get(getDefNodePropertyKey(graphId, objectType, nodeProperty));
		return value == null ? null : value.toString();
	}

	/**
	 * Sets the def node property.
	 *
	 * @param graphId
	 *            the graph id
	 * @param objectType
	 *            the object type
	 * @param nodeProperty
	 *            the node property
	 * @param propValue
	 *            the prop value
	 */
	public static void setDefNodeProperty(String graphId, String objectType, String nodeProperty, String propValue) {
		set(getDefNodePropertyKey(graphId, objectType, nodeProperty), propValue);
	}

	/**
	 * Gets the def node property key.
	 *
	 * @param graphId
	 *            the graph id
	 * @param objectType
	 *            the object type
	 * @param nodeProperty
	 *            the node property
	 * @return the def node property key
	 */
	private static String getDefNodePropertyKey(String graphId, String objectType, String nodeProperty) {
		String key = CacheKeyGenerator.getNodePropertyKey(graphId, objectType, nodeProperty);
		return key;
	}
}
