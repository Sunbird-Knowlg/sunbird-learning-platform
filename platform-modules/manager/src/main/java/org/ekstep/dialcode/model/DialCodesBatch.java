/**
 * 
 */
package org.ekstep.dialcode.model;

import java.util.Map;

/**
 * @author pradyumna
 *
 */
public class DialCodesBatch {

	Map<Double, String> dialCodes;
	double maxIndex;

	/**
	 * @return the dialCodes
	 */
	public Map<Double, String> getDialCodes() {
		return dialCodes;
	}

	/**
	 * @param dialCodes
	 *            the dialCodes to set
	 */
	public void setDialCodes(Map<Double, String> dialCodes) {
		this.dialCodes = dialCodes;
	}

	/**
	 * @return the maxIndex
	 */
	public double getMaxIndex() {
		return maxIndex;
	}

	/**
	 * @param maxIndex
	 *            the maxIndex to set
	 */
	public void setMaxIndex(double maxIndex) {
		this.maxIndex = maxIndex;
	}

}
