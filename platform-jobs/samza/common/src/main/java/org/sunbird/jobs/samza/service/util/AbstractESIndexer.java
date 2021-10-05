/**
 * 
 */
package org.sunbird.jobs.samza.service.util;

/**
 * @author pradyumna
 *
 */
public abstract class AbstractESIndexer {

	/**
	 * 
	 */
	public AbstractESIndexer() {
		init();
	}

	protected abstract void init();
}
