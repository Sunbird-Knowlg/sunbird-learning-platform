package org.sunbird.graph.model;

import org.sunbird.common.dto.Request;

/**
 * @author rayulu
 * 
 */
public interface IPropertyContainer {

    /**
     * Returns the property value associated with the given key. The value is of
     * one of the valid property types, i.e. a Java primitive, a String or an
     * array of any of the valid types. If there's no property associated with
     * key, null value is returned.
     * 
     * @param key
     *            - the property key
     * @return the property value associated with the given key
     */
    void getProperty(Request request);

    /**
     * Removes the property associated with the given key and returns the old
     * value. If there's no property associated with the key, null will be
     * returned.
     * 
     * @param key
     *            - the property key
     * @return the property value that used to be associated with the given key
     */
    void removeProperty(Request request);

    /**
     * Sets the property value for the given key to value. The property value
     * must be one of the valid property types, i.e:
     * <ul>
     * <li>boolean or boolean[]</li>
     * <li>int or int[]</li>
     * <li>long or long[]</li>
     * <li>float or float[]</li>
     * <li>double or double[]</li>
     * <li>String or String[]</li>
     * </ul>
     * null is not an accepted property value.
     * 
     * @param key
     *            - the key with which the new property value will be associated
     * @param value
     *            - the new property value, of one of the valid property types
     */
    void setProperty(Request request);
    
    void delete(Request request);

    void create(Request request);
}
