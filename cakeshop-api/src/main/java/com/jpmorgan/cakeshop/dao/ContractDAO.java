package com.jpmorgan.cakeshop.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.model.ContractInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ContractDAO extends BaseDAO {

    @Autowired
    private ObjectMapper jsonMapper;

    static final Logger LOG = LoggerFactory.getLogger(ContractDAO.class);

    @Transactional
    public Contract getById(String id) throws IOException {
        if (null != getCurrentSession()) {
            ContractInfo contractInfo = getCurrentSession().get(ContractInfo.class, id);
            if (contractInfo != null) {
                return jsonMapper.readValue(contractInfo.contractJson, Contract.class);
            }
        }
        return null;
    }

    @Transactional
    public void save(Contract contract) throws IOException {
        if (null != getCurrentSession()) {
            LOG.info("Saving private contract");
            String contractJson = jsonMapper.writeValueAsString(contract);
            getCurrentSession().save(new ContractInfo(contract.getAddress(), contractJson));
        }
    }

    @Override
    @Transactional
    public void reset() {
        if (null != getCurrentSession()) {
            Session session = getCurrentSession();
            session.createSQLQuery("TRUNCATE TABLE CONTRACTS").executeUpdate();
            session.flush();
            session.clear();
        }
    }

    @Transactional
    public List<Contract> list() {
        if (null != getCurrentSession()) {
            Session session = getCurrentSession();
            return (List<Contract>) session.createCriteria(ContractInfo.class)
                .list()
                .stream()
                .map((contractInfo) -> {
                    try {
                        return jsonMapper
                            .readValue(((ContractInfo) contractInfo).contractJson, Contract.class);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Transactional
    public List<String> listAddresses() {
        if (null != getCurrentSession()) {
            Session session = getCurrentSession();
            return session.createQuery("SELECT address FROM ContractInfo").list();
        }
        return Collections.emptyList();
    }
}
