package org.ekstep.graph.service.util;

import java.util.ArrayList;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;

/**
 * @author Azhar
 */

public class PlatformInMemoryCacheUtil<K, T> {

	private long timeToLive;
	private LRUMap platformCacheMap;

	protected class PlatformCacheObject {
		public long lastAccessed = System.currentTimeMillis();
		public T value;

		protected PlatformCacheObject(T value) {
			this.value = value;
		}
	}

	public PlatformInMemoryCacheUtil(long platformTimeToLive, final long platformTimerInterval, int maxItems) {
		this.timeToLive = platformTimeToLive * 1000;

		platformCacheMap = new LRUMap(maxItems);

		if (timeToLive > 0 && platformTimerInterval > 0) {

			Thread t = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(platformTimerInterval * 1000);
						} catch (InterruptedException ex) {
//							PlatformLogger.log("Error! Thread Sleep Interrupted in Platform In-Memory Cache.",
//									platformTimerInterval, ex);
						}
						cleanup();
					}
				}
			});

			t.setDaemon(true);
			t.start();
		}
	}

	public void put(K key, T value) {
		synchronized (platformCacheMap) {
			platformCacheMap.put(key, new PlatformCacheObject(value));
		}
	}

	@SuppressWarnings("unchecked")
	public T get(K key) {
		synchronized (platformCacheMap) {
			PlatformCacheObject c = (PlatformCacheObject) platformCacheMap.get(key);

			if (c == null)
				return null;
			else {
				c.lastAccessed = System.currentTimeMillis();
				return c.value;
			}
		}
	}

	public void remove(K key) {
		synchronized (platformCacheMap) {
			platformCacheMap.remove(key);
		}
	}

	public int size() {
		synchronized (platformCacheMap) {
			return platformCacheMap.size();
		}
	}

	@SuppressWarnings("unchecked")
	public void cleanup() {

		long now = System.currentTimeMillis();
		ArrayList<K> deleteKey = null;

		synchronized (platformCacheMap) {
			MapIterator itr = platformCacheMap.mapIterator();

			deleteKey = new ArrayList<K>((platformCacheMap.size() / 2) + 1);
			K key = null;
			PlatformCacheObject c = null;

			while (itr.hasNext()) {
				key = (K) itr.next();
				c = (PlatformCacheObject) itr.getValue();

				if (c != null && (now > (timeToLive + c.lastAccessed))) {
					deleteKey.add(key);
				}
			}
		}

		for (K key : deleteKey) {
			synchronized (platformCacheMap) {
				platformCacheMap.remove(key);
			}

			Thread.yield();
		}
	}
}
