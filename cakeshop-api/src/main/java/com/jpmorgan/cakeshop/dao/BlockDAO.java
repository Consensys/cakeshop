package com.jpmorgan.cakeshop.dao;

import com.jpmorgan.cakeshop.model.Block;

import java.math.BigInteger;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class BlockDAO extends BaseDAO {

    @Transactional
    public Block getById(String id) {
        if (null != getCurrentSession()) {
            return getCurrentSession().get(Block.class, id);
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Transactional
    public Block getByNumber(BigInteger number) {
        Block block = null;
        if (null != getCurrentSession()) {
            Criteria c = getCurrentSession().createCriteria(Block.class);
            c.add(Restrictions.eq("number", number));
            List list = c.list();

            if (list == null || list.isEmpty()) {
                return null;
            }

            block = (Block) list.get(0);
            if (null != getCurrentSession()) {
                Hibernate.initialize(block.getTransactions());
                Hibernate.initialize(block.getUncles());
            }
        }
        return block;
    }

    @SuppressWarnings("rawtypes")
    @Transactional
    public Block getLatest() {
        if (null != getCurrentSession()) {
            Criteria c = getCurrentSession().createCriteria(Block.class);
            c.setProjection(Projections.max("number"));
            List list = c.list();

            if (list == null || list.isEmpty() || list.get(0) == null) {
                return null;
            }

            return getByNumber((BigInteger) list.get(0));
        } else {
            return null;
        }
    }

    @Transactional
    public void save(Block block) {
        if (null != getCurrentSession()) {
            getCurrentSession().save(block);
        }
    }

    @Override
    @Transactional
    public void reset() {
        if (null != getCurrentSession()) {
            Session session = getCurrentSession();
            session.createSQLQuery("TRUNCATE TABLE BLOCK_TRANSACTIONS").executeUpdate();
            session.createSQLQuery("TRUNCATE TABLE BLOCK_UNCLES").executeUpdate();
            session.createSQLQuery("TRUNCATE TABLE BLOCKS").executeUpdate();
            session.flush();
            session.clear();
        }
    }

}
