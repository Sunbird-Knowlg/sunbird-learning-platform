/*
 * Copyright(c) 2013-2014 Canopus Consutling. All rights reserved.
 *
 * This code is intellectual property of Canopus Consutling. The intellectual and technical concepts contained herein
 * may be covered by patents, patents in process, and are protected by trade secret or copyright law. Any unauthorized
 * use of this code without prior approval from Canopus Consutling is prohibited.
 */
package com.ilimi.dac.hibernate;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/*import com.ilimi.dac.utils.HibernateDAOHelper;
import com.ilimi.mw.dto.ExecutionContext;*/
import com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl;
import com.googlecode.genericdao.search.ExampleOptions;
import com.googlecode.genericdao.search.Filter;
import com.googlecode.genericdao.search.ISearch;
import com.googlecode.genericdao.search.SearchResult;
import com.ilimi.dac.BaseDataAccessEntity;

/**
 * Provides a generalized hibernate DAO which can be used with any domain
 * entity. This class can be directly used if there is no special behavior being
 * defined for the query entity being used.
 *
 * @author Feroz
 */
public class GenericHibernateDao extends GeneralDAOImpl {

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#count(com.googlecode
     * .genericdao.search.ISearch)
     */
    @Override
    public int count(ISearch search) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.count(search);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#find(java.lang
     * .Class , java.io.Serializable)
     */
    @Override
    public <T> T find(Class<T> type, Serializable id) {
        T result = super.find(type, id);
        //HibernateDAOHelper.validateEntityTenantId(result);
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#find(java.lang
     * .Class , java.io.Serializable[])
     */
    @Override
    public <T> T[] find(Class<T> type, Serializable... ids) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        // Here filter working...
        T[] entities = super.find(type, ids);
        return entities;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#findAll(java.lang
     * .Class)
     */
    @Override
    public <T> List<T> findAll(Class<T> type) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.findAll(type);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#flush()
     */
    @Override
    public void flush() {
        super.flush();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#getReference(java
     * .lang.Class, java.io.Serializable)
     */
    @Override
    public <T> T getReference(Class<T> type, Serializable id) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.getReference(type, id);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#getReferences(java
     * .lang.Class, java.io.Serializable[])
     */
    @Override
    public <T> T[] getReferences(Class<T> type, Serializable... ids) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.getReferences(type, ids);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#isAttached(java.
     * lang.Object)
     */
    @Override
    public boolean isAttached(Object entity) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.isAttached(entity);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#refresh(java.lang
     * .Object[])
     */
    @Override
    public void refresh(Object... entities) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        super.refresh(entities);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#remove(java.lang
     * .Object)
     */
    @Override
    public boolean remove(Object entity) {
        //HibernateDAOHelper.validateEntityTenantId(entity);
        return super.remove(entity);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#remove(java.lang
     * .Object[])
     */
    @Override
    public void remove(Object... entities) {
        //HibernateDAOHelper.validateEntityTenantId(entities);
        super.remove(entities);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#removeById(java.
     * lang.Class, java.io.Serializable)
     */
    @Override
    public boolean removeById(Class<?> type, Serializable id) {
        /*if (!ExecutionContext.getCurrent().isCrossTenant()) {
            Object result = find(type, id);
            HibernateDAOHelper.validateEntityTenantId(result);
        }*/
        return super.removeById(type, id);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#removeByIds(java
     * .lang.Class, java.io.Serializable[])
     */
    @Override
    public void removeByIds(Class<?> type, Serializable... ids) {
        // Filter works here
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        super.removeByIds(type, ids);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#save(java.lang
     * .Object )
     */
    @Override
    public boolean save(Object entity) {
        //HibernateDAOHelper.setSystemData(entity);
        return super.save(entity);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#save(java.lang
     * .Object [])
     */
    @Override
    public boolean[] save(Object... entities) {
        //HibernateDAOHelper.setSystemData(entities);
        return super.save(entities);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#search(com.googlecode
     * .genericdao.search.ISearch)
     */
    @Override
    public List search(ISearch search) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.search(search);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#searchAndCount
     * (com. googlecode.genericdao.search.ISearch)
     */
    @Override
    public SearchResult searchAndCount(ISearch search) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.searchAndCount(search);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#searchUnique(com
     * .googlecode.genericdao.search.ISearch)
     */
    @Override
    public Object searchUnique(ISearch search) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.searchUnique(search);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#getFilterFromExample
     * (java.lang.Object)
     */
    @Override
    public Filter getFilterFromExample(Object example) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        String modifiedBy = null;
        Date modifiedOn = null;
        if (example instanceof BaseDataAccessEntity) {
            BaseDataAccessEntity entity = ((BaseDataAccessEntity) example);
            modifiedBy = entity.getLastModifiedBy();
            modifiedOn = entity.getLastModifiedOn();
            entity.setLastModifiedBy(null);
            entity.setLastModifiedOn(null);
        }
        Filter filter = super.getFilterFromExample(example);
        if (example instanceof BaseDataAccessEntity) {
            BaseDataAccessEntity entity = ((BaseDataAccessEntity) example);
            entity.setLastModifiedBy(modifiedBy);
            entity.setLastModifiedOn(modifiedOn);
        }
        return filter;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl#getFilterFromExample
     * (java.lang.Object, com.googlecode.genericdao.search.ExampleOptions)
     */
    @Override
    public Filter getFilterFromExample(Object example, ExampleOptions options) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        String modifiedBy = null;
        Date modifiedOn = null;
        if (example instanceof BaseDataAccessEntity) {
            BaseDataAccessEntity entity = ((BaseDataAccessEntity) example);
            modifiedBy = entity.getLastModifiedBy();
            modifiedOn = entity.getLastModifiedOn();
            entity.setLastModifiedBy(null);
            entity.setLastModifiedOn(null);
        }
        Filter filter = super.getFilterFromExample(example, options);
        if (example instanceof BaseDataAccessEntity) {
            BaseDataAccessEntity entity = ((BaseDataAccessEntity) example);
            entity.setLastModifiedBy(modifiedBy);
            entity.setLastModifiedOn(modifiedOn);
        }
        return filter;
    }

    /**
     * Merge.
     *
     * @param <T>
     *            the generic type
     * @param entity
     *            the entity
     * @return the t
     */
    public <T> T merge(T entity) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super._merge(entity);
    }

}
