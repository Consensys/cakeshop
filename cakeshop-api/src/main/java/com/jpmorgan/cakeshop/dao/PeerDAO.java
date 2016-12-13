package com.jpmorgan.cakeshop.dao;

import com.jpmorgan.cakeshop.model.Peer;

import java.util.ArrayList;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PeerDAO extends BaseDAO {

    @Transactional
    public Peer getById(String id) {
        if (null != getCurrentSession()) {
            return getCurrentSession().get(Peer.class, id);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<Peer> list() {
        if (null != getCurrentSession()) {
            Criteria c = getCurrentSession().createCriteria(Peer.class);
            return c.list();
        }
        return new ArrayList<>();
    }

    @Transactional
    public void save(Peer peer) {
        if (null != getCurrentSession()) {
            getCurrentSession().merge(peer);
        }
    }

    @Transactional
    public void save(List<Peer> peers) {
        if (null != getCurrentSession()) {
            Session session = getCurrentSession();
            for (int i = 0; i < peers.size(); i++) {
                Peer peer = peers.get(i);
                session.save(peer);
                if (i % BATCH_SIZE == 0) {
                    session.flush();
                    session.clear();
                }
            }
            session.flush();
            session.clear();
        }
    }

    @Override
    @Transactional
    public void reset() {
        if (null != getCurrentSession()) {
            Session session = getCurrentSession();
            session.createSQLQuery("TRUNCATE TABLE PEERS").executeUpdate();
            session.flush();
            session.clear();
        }
    }

}
