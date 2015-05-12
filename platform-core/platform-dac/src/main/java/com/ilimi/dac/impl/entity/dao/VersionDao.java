package com.ilimi.dac.impl.entity.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ilimi.dac.hibernate.BaseHibernateDao;
import com.ilimi.dac.impl.entity.VersionEntity;

public class VersionDao extends BaseHibernateDao<VersionEntity, Integer>{

    @Autowired
    private JdbcTemplate jdbcTemplate = null;
}
