package com.jpmorgan.cakeshop.manager.service.impl;

import com.jpmorgan.cakeshop.manager.dao.RemoteNodeDAO;
import com.jpmorgan.cakeshop.manager.db.entity.RemoteNode;
import com.jpmorgan.cakeshop.manager.service.SaveNodeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaveNodeServiceImpl implements SaveNodeService {

    @Autowired
    private RemoteNodeDAO dao;

    @Override
    @Transactional
    public void insert(RemoteNode node) {
        dao.insert(node);
    }

    @Override
    @Transactional
    public void update(RemoteNode node) {
        dao.update(node);
    }

    @Override
    @Transactional
    public RemoteNode getNode(String id) {
        return dao.getRemoteNode(id);
    }

    @Override
    @Transactional
    public List<RemoteNode> getRemoteNodesList() {
        return dao.getRemoteNodesList();
    }

}
