package com.ilimi.dac.impl.entity.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ilimi.dac.hibernate.BaseHibernateDao;
import com.ilimi.dac.impl.entity.AuditLogEntity;

/**
 * 
 * @author Mahesh
 *
 */

public class AuditLogDao extends BaseHibernateDao<AuditLogEntity, Integer> {

    @Autowired
    private JdbcTemplate jdbcTemplate = null;

}
