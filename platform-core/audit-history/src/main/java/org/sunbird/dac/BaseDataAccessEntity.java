/*
 * Copyright(c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical concepts contained herein
 * may be covered by patents, patents in process, and are protected by trade secret or copyright law. Any unauthorized
 * use of this code without prior approval from Canopus Consulting is prohibited.
 */
package org.sunbird.dac;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
/*import org.sunbird.mw.dto.ExecutionContext;
import org.sunbird.mw.dto.param.HeaderParam;*/

/**
 * Base class for data access entities. Provides implementation of commonly used
 * data access entity methods.
 *
 * @author Feroz
 */

@MappedSuperclass
public abstract class BaseDataAccessEntity implements DataAccessEntity {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3324974247342634122L;

    {
       /* if (!ExecutionContext.getCurrent().isAuditDisabled()) {
            setLastModifiedOn(new Date());
            if (ExecutionContext.getCurrent().getContextValues() != null) {
                setLastModifiedBy((String) ExecutionContext.getCurrent()
                        .getContextValues()
                        .get(HeaderParam.USER_NAME.getParamName()));
            }
        }*/
    }

    /** The last modified by. */
    @Column(name = "LAST_MODIFIED_BY", length = 45)
    private String lastModifiedBy;

    /** The last modified on. */
    @Column(name = "LAST_MODIFIED_ON", length = 45)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedOn;

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (getId() == null) {
            return super.hashCode();
        }
        return getId();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getName() + ":" + getId();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {

        if (other == null) {
            return false;
        }

        if (other.getClass().equals(this.getClass())) {
            BaseDataAccessEntity otherEntity = (BaseDataAccessEntity) other;
            return (this.getId() == otherEntity.getId());
        }

        return true;
    }

    /**
     * Gets the last modified by.
     *
     * @return the lastModifiedBy
     */
    @Override
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * Sets the last modified by.
     *
     * @param lastModifiedBy
     *            the lastModifiedBy to set
     */
    @Override
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /**
     * Gets the last modified on.
     *
     * @return the lastModifiedOn
     */
    @Override
    public Date getLastModifiedOn() {
        return lastModifiedOn;
    }

    /**
     * Sets the last modified on.
     *
     * @param lastModifiedOn
     *            the lastModifiedOn to set
     */
    @Override
    public void setLastModifiedOn(Date lastModifiedOn) {
        this.lastModifiedOn = lastModifiedOn;
    }
}
