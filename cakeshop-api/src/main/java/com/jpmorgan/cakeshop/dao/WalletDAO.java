package com.jpmorgan.cakeshop.dao;

import com.jpmorgan.cakeshop.model.Account;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class WalletDAO extends BaseDAO {

    private static final Logger LOG = LoggerFactory.getLogger(WalletDAO.class);

    @SuppressWarnings("unchecked")
    @Transactional
    public List<Account> list() {
        if (null != getCurrentSession()) {
            Criteria criteria = getCurrentSession().createCriteria(Account.class);
            return criteria.list();
        }
        return new ArrayList<>();
    }

    @Transactional
    public void save(Account account) {
        if (null != getCurrentSession()) {
            getCurrentSession().merge(account);
        } else {
            LOG.warn("Session is null, not saving account"); // TODO
        }
    }

    @Override
    @Transactional
    public void reset() {
        if (null != getCurrentSession()) {
            Session session = getCurrentSession();
            session.createSQLQuery("TRUNCATE TABLE ACCOUNTS").executeUpdate();
            session.flush();
            session.clear();
        }
    }

}
