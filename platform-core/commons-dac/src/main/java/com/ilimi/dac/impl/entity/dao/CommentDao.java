package com.ilimi.dac.impl.entity.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ilimi.dac.hibernate.BaseHibernateDao;
import com.ilimi.dac.impl.entity.CommentEntity;

public class CommentDao extends BaseHibernateDao<CommentEntity, Integer>{

    @Autowired
    private JdbcTemplate jdbcTemplate = null;
    
}
