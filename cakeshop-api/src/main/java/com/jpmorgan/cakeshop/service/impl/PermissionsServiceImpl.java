package com.jpmorgan.cakeshop.service.impl;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.PermissionsDetails;
import com.jpmorgan.cakeshop.model.PermissionsOrgDetails;
import com.jpmorgan.cakeshop.service.PermissionsService;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.util.CakeshopUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.jpmorgan.cakeshop.service.impl.GethHttpServiceImpl.SIMPLE_RESULT;

@Service
public class PermissionsServiceImpl implements PermissionsService {

    static final Logger LOG = LoggerFactory.getLogger(com.jpmorgan.cakeshop.service.impl.PermissionsServiceImpl.class);

    @Autowired
    private GethHttpService gethService;

    @Override
    public PermissionsOrgDetails get(String id) throws APIException {

        Map<String, Object> permissionData = gethService.executeGethCall("quorumPermission_getOrgDetails", new Object[]{id});

        PermissionsOrgDetails details = null;

        try {

            details = CakeshopUtils.convertToObject(permissionData, PermissionsOrgDetails.class);

        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

        return details;
    }

    @Override
    public PermissionsDetails get() throws APIException {

        PermissionsDetails details = null;

        try {

            Map<String, Object> pInfo = gethService.executeGethCall("quorumPermission_orgList");

            details = CakeshopUtils.convertToObject(pInfo, PermissionsDetails.class);

        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new APIException("Could not retrieve Permission Org List ");
        }

        return details;
    }

    @Override
    public String addOrg(String id, String enodeId, String accountId, Object f) throws APIException {
        LOG.info("Add new permissions org {}", id);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_addOrg", new Object[]{id, enodeId, accountId, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not add new permissions org: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String approveOrg(String id, String enodeId, String accountId, Object f) throws APIException {
        LOG.info("Approve new permissions org {}", id);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_approveOrg", new Object[]{id, enodeId, accountId, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not approve org: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String updateOrgStatus(String id, int action, Object f) throws APIException {
        LOG.info("update permissions org {} status", id);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_updateOrgStatus", new Object[]{id, action, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not update org status: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String approveOrgStatus(String id, int action, Object f) throws APIException {
        LOG.info("approve permissions org {} status", id);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_approveOrgStatus", new Object[]{id, action, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not approve org status: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String addSubOrg(String id, String parentId, String enodeId, Object f) throws APIException {
        LOG.info("add new suborg {} to parent {}", id, parentId);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_addSubOrg", new Object[]{parentId, id, enodeId, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not add sub org: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String addNewRole(String id, String roleId, int access, boolean isVoter, boolean isAdmin, Object f) throws APIException {
        LOG.info("add new permissions role {} to org {}", roleId, id);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_addNewRole", new Object[]{id, roleId, access, isVoter, isAdmin, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not add new role: " + roleId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String removeRole(String id, String roleId, Object f) throws APIException {
        LOG.info("remove new role {} from org {}", roleId, id);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_removeRole", new Object[]{id, roleId, f});


        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not remove role: " + roleId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String addAccount(String acctId, String id, String roleId, Object f) throws APIException {
        LOG.info("add account {} to org {}", acctId, id);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_addAccountToOrg", new Object[]{acctId, id, roleId, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not add account: " + acctId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String changeAccountRole(String acctId, String id, String roleId, Object f) throws APIException {
        LOG.info("change account role to {} for acct {}", roleId, acctId);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_changeAccountRole", new Object[]{acctId, id, roleId, f});


        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not change account role for acct: " + acctId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String updateAccount(String acctId, String id, int action, Object f) throws APIException {
        LOG.info("update account {} status to {} ", acctId, action);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_updateAccountStatus", new Object[]{id, acctId, action, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not update account: " + acctId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String assignAdmin(String acctId, String id, String roleId, Object f) throws APIException {
        LOG.info("assign admin to account {}", acctId);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_assignAdminRole", new Object[]{id, acctId, roleId, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not assign admin for acct: " + acctId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String approveAdmin(String acctId, String id, Object f) throws APIException {
        LOG.info("approve assignment of admin for account {}", acctId);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_approveAdminRole", new Object[]{id, acctId, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not approve admin for acct: " + acctId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String addNode(String id, String enodeId, Object f) throws APIException {
        LOG.info("add new node {} to org {}", enodeId, id);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_addNode", new Object[]{id, enodeId, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not add node: " + enodeId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String updateNode(String id, String enodeId, int action, Object f) throws APIException {
        LOG.info("update status of node {}", enodeId);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_updateNodeStatus", new Object[]{id, enodeId, action, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not update node: " + enodeId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String recoverAcct(String id, String accountId, Object f) throws APIException {
        LOG.info("recover blacklisted account {}", accountId);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_recoverBlackListedAccount", new Object[]{id, accountId, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not recover acct: " + accountId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String approveAcct(String id, String accountId, Object f) throws APIException {
        LOG.info("approve recovery of blacklisted account {}", accountId);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_approveBlackListedAccountRecovery", new Object[]{id, accountId, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not approve recover of acct: " + accountId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String recoverNode(String id, String enodeId, Object f) throws APIException {
        LOG.info("recover blacklisted node {}", enodeId);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_recoverBlackListedNode", new Object[]{id, enodeId, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not recover node: " + enodeId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String approveNode(String id, String enodeId, Object f) throws APIException {
        LOG.info("approve recovery of blacklisted node {}", enodeId);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_approveBlackListedNodeRecovery", new Object[]{id, enodeId, f});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not approve recover of node: " + enodeId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }
}
