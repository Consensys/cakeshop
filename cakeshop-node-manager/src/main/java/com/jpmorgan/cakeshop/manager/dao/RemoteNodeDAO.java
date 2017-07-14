package com.jpmorgan.cakeshop.manager.dao;

import com.jpmorgan.cakeshop.manager.db.entity.RemoteNode;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

@Repository
public class RemoteNodeDAO extends BaseDAO {

    public void insert(RemoteNode node) {
        getCurrentSession().save(node);
    }

    public void update(RemoteNode node) {
        getCurrentSession().update(node);
    }

    public RemoteNode getRemoteNode(String id) {
        Criteria criteria = getCurrentSession().createCriteria(RemoteNode.class);
        criteria.add(Restrictions.eq("id", id));
        return (RemoteNode) criteria.uniqueResult();
    }

    public List<RemoteNode> getRemoteNodesList() {
        return getCurrentSession().createCriteria(RemoteNode.class).list();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
