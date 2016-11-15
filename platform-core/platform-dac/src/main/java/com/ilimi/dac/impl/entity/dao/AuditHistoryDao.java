package com.ilimi.dac.impl.entity.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ilimi.dac.hibernate.BaseHibernateDao;
import com.ilimi.dac.impl.entity.AuditHistoryEntity;

@SuppressWarnings("unchecked")
public class AuditHistoryDao extends BaseHibernateDao<AuditHistoryEntity, Integer> {

    @SuppressWarnings("unused")
	@Autowired
    private JdbcTemplate jdbcTemplate = null;
    
}
