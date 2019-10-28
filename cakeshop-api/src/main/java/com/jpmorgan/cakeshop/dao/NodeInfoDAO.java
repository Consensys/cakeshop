package com.jpmorgan.cakeshop.dao;

import com.jpmorgan.cakeshop.model.NodeInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class NodeInfoDAO extends BaseDAO {

    static final Logger LOG = LoggerFactory.getLogger(NodeInfoDAO.class);


    @Transactional
    public NodeInfo getByUrls(String rpcUrl, String transactionManagerUrl) throws IOException {
        if (null != getCurrentSession()) {
            List<NodeInfo> matches = (List<NodeInfo>) getCurrentSession().createCriteria(NodeInfo.class)
                .add(Restrictions.eq("rpcUrl", rpcUrl))
                .add(Restrictions.eq("transactionManagerUrl", transactionManagerUrl))
                .list();

            return matches.isEmpty() ? null : matches.get(0);
        }
        return null;
    }

    @Transactional
    public void save(NodeInfo nodeInfo) throws IOException {
        if (null != getCurrentSession()) {
            getCurrentSession().save(nodeInfo);
        }
    }

    @Transactional
    public void save(List<NodeInfo> nodes) {
        Session currentSession = getCurrentSession();
        if (null != currentSession) {
            for (NodeInfo node : nodes) {
                LOG.info("Saving nodeInfo {}", node);
                currentSession.save(node);
            }
        }
    }

    @Transactional
    public void delete(NodeInfo nodeInfo) throws IOException {
        if (null != getCurrentSession()) {
            LOG.info("Deleting nodeInfo");
            getCurrentSession().delete(nodeInfo);
        }
    }

    @Override
    @Transactional
    public void reset() {
        if (null != getCurrentSession()) {
            Session session = getCurrentSession();
            session.createSQLQuery("TRUNCATE TABLE NODE_INFO").executeUpdate();
            session.flush();
            session.clear();
        }
    }

    @Transactional
    public List<NodeInfo> list() {
        if (null != getCurrentSession()) {
            Session session = getCurrentSession();
            List<NodeInfo> nodes = session.createCriteria(NodeInfo.class).list();
            return nodes;
        }
        return Collections.emptyList();
    }
}
