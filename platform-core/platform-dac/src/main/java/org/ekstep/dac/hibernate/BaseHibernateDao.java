/*
 * Copyright(c) 2013-2014 Canopus Consutling. All rights reserved.
 *
 * This code is intellectual property of Canopus Consutling. The intellectual and technical concepts contained herein
 * may be covered by patents, patents in process, and are protected by trade secret or copyright law. Any unauthorized
 * use of this code without prior approval from Canopus Consutling is prohibited.
 */
package org.ekstep.dac.hibernate;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.ekstep.dac.BaseDataAccessEntity;

/*import org.ekstep.dac.utils.HibernateDAOHelper;
import com.ilimi.mw.dto.ExecutionContext;*/
import com.googlecode.genericdao.dao.hibernate.GenericDAOImpl;
import com.googlecode.genericdao.search.ExampleOptions;
import com.googlecode.genericdao.search.ISearch;
import com.googlecode.genericdao.search.SearchResult;

/**
 * Base utility class for writing DAO when the domain object is known. Provides
 * utility methods to directly work with the hibernate session. DAO
 * implementations can either inherit from this base class and specify the type
 * of domain object, or work with generic hibernate DAO.
 *
 * @param <T>
 *            the generic type
 * @param <ID>
 *            the generic type
 * @author Feroz
 */
public class BaseHibernateDao<T, ID extends Serializable> extends
        GenericDAOImpl<T, ID> {

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#count(com.googlecode
     * .genericdao.search.ISearch)
     */
    @Override
    public int count(ISearch search) {
//        HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.count(search);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#find(java.io.
     * Serializable[])
     */
    @Override
    public T[] find(Serializable... ids) {
//        HibernateDAOHelper.setTenantFilter(this.getSession());
        // No need to call HibernateDAOHelper.validateEntityTenantId(result) as
        // filter works here
        return super.find(ids);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#find(java.io.
     * Serializable)
     */
    @Override
    public T find(Serializable id) {
        T result = super.find(id);
//        HibernateDAOHelper.validateEntityTenantId(result);
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#findAll()
     */
    @Override
    public List<T> findAll() {
//        HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.findAll();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#flush()
     */
    @Override
    public void flush() {
        super.flush();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#getFilterFromExample
     * (java.lang.Object, com.googlecode.genericdao.search.ExampleOptions)
     */
    @Override
    public com.googlecode.genericdao.search.Filter getFilterFromExample(
            T example, ExampleOptions options) {
//        HibernateDAOHelper.setTenantFilter(this.getSession());
        String modifiedBy = null;
        Date modifiedOn = null;
        if (example instanceof BaseDataAccessEntity) {
            BaseDataAccessEntity entity = ((BaseDataAccessEntity) example);
            modifiedBy = entity.getLastModifiedBy();
            modifiedOn = entity.getLastModifiedOn();
            entity.setLastModifiedBy(null);
            entity.setLastModifiedOn(null);
        }
        com.googlecode.genericdao.search.Filter filter = super
                .getFilterFromExample(example, options);
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
     * com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#getFilterFromExample
     * (java.lang.Object)
     */
    @Override
    public com.googlecode.genericdao.search.Filter getFilterFromExample(
            T example) {
//        HibernateDAOHelper.setTenantFilter(this.getSession());
        String modifiedBy = null;
        Date modifiedOn = null;
        if (example instanceof BaseDataAccessEntity) {
            BaseDataAccessEntity entity = ((BaseDataAccessEntity) example);
            modifiedBy = entity.getLastModifiedBy();
            modifiedOn = entity.getLastModifiedOn();
            entity.setLastModifiedBy(null);
            entity.setLastModifiedOn(null);
        }
        com.googlecode.genericdao.search.Filter filter = super
                .getFilterFromExample(example);
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
     * com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#getReference(java
     * .io.Serializable)
     */
    @Override
    public T getReference(Serializable id) {
//        HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.getReference(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#getReferences(java
     * .io.Serializable[])
     */
    @Override
    public T[] getReferences(Serializable... ids) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.getReferences(ids);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#isAttached(java.
     * lang.Object)
     */
    @Override
    public boolean isAttached(T entity) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.isAttached(entity);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#refresh(T[])
     */
    @Override
    public void refresh(T... entities) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        super.refresh(entities);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#remove(T[])
     */
    @Override
    public void remove(T... entities) {
        //HibernateDAOHelper.validateEntityTenantId(entities);
        super.remove(entities);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#remove(java.lang
     * .Object)
     */
    @Override
    public boolean remove(T entity) {
        //HibernateDAOHelper.validateEntityTenantId(entity);
        return super.remove(entity);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#removeById(java.
     * io.Serializable)
     */
    @Override
    public boolean removeById(Serializable id) {
       /* if (!ExecutionContext.getCurrent().isCrossTenant()) {
            T result = super.find(id);
            HibernateDAOHelper.validateEntityTenantId(result);
        }*/
        return super.removeById(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#removeByIds(java
     * .io.Serializable[])
     */
    @Override
    public void removeByIds(Serializable... ids) {
        // Filter works here
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        super.removeByIds(ids);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#save(T[])
     */
    @Override
    public boolean[] save(T... entities) {
        //HibernateDAOHelper.setSystemData(entities);
        return super.save(entities);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#save(java.lang
     * .Object )
     */
    @Override
    public boolean save(T entity) {
        //HibernateDAOHelper.setSystemData(entity);
        return super.save(entity);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#search(com.googlecode
     * .genericdao.search.ISearch)
     */
    @Override
    public List<T> search(ISearch search) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.<T>search(search);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#searchAndCount
     * (com. googlecode.genericdao.search.ISearch)
     */
    @Override
    public SearchResult<T> searchAndCount(ISearch arg0) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.<T>searchAndCount(arg0);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.googlecode.genericdao.dao.hibernate.GenericDAOImpl#searchUnique(com
     * .googlecode.genericdao.search.ISearch)
     */
    @Override
    public T searchUnique(ISearch search) {
        //HibernateDAOHelper.setTenantFilter(this.getSession());
        return super.<T>searchUnique(search);
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
