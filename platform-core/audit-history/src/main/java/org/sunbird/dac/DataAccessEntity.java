/*
 * Copyright(c) 2013-2014 Canopus Consutling. All rights reserved.
 *
 * This code is intellectual property of Canopus Consutling. The intellectual and technical concepts contained herein
 * may be covered by patents, patents in process, and are protected by trade secret or copyright law. Any unauthorized
 * use of this code without prior approval from Canopus Consutling is prohibited.
 */
package org.sunbird.dac;

import java.io.Serializable;
import java.util.Date;

/**
 * Base class for all data access entities. The data access entities are mapped
 * to the database using the underlying ORM infrastructure or JDBC directly.
 *
 * @author Feroz
 */
public interface DataAccessEntity extends Serializable {

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Integer getId();

    /**
     * Sets the id.
     *
     * @param id
     *            the id to set
     */
    public void setId(Integer id);

    /**
     * Gets the last modified by.
     *
     * @return the lastModifiedBy
     */
    public String getLastModifiedBy();

    /**
     * Sets the last modified by.
     *
     * @param lastModifiedBy
     *            the lastModifiedBy to set
     */
    public void setLastModifiedBy(String lastModifiedBy);

    /**
     * Gets the last modified on.
     *
     * @return the lastModifiedOn
     */
    public Date getLastModifiedOn();

    /**
     * Sets the last modified on.
     *
     * @param lastModifiedOn
     *            the lastModifiedOn to set
     */
    public void setLastModifiedOn(Date lastModifiedOn);
}
