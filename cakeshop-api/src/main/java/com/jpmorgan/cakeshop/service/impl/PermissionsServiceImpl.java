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
        }

        return details;
    }

    @Override
    public String addOrg(String id, String enodeId, String accountId, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_addOrg", new Object[]{id, enodeId, accountId, f});

        LOG.info("addorg {}", res);
        if (res == null) {
            throw new APIException("Could not add org: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String approveOrg(String id, String enodeId, String accountId, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_approveOrg", new Object[]{id, enodeId, accountId, f});

        LOG.info("approve {}", res);
        if (res == null) {
            throw new APIException("Could not approve org: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String updateOrgStatus(String id, int action, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_updateOrgStatus", new Object[]{id, action, f});

        LOG.info("update org status {}", res);
        if (res == null) {
            throw new APIException("Could not update org status: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String approveOrgStatus(String id, int action, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_approveOrgStatus", new Object[]{id, action, f});

        LOG.info("approve org status {}", res);
        if (res == null) {
            throw new APIException("Could not approve org status: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String addSubOrg(String id, String parentId, String enodeId, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_addSubOrg", new Object[]{parentId, id, enodeId, f});

        LOG.info("approve org status {}", res);
        if (res == null) {
            throw new APIException("Could not approve org status: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String addNewRole(String id, String roleId, int access, boolean isVoter, boolean isAdmin, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_addNewRole", new Object[]{id, roleId, access, isVoter, isAdmin, f});

        LOG.info("add new role {}", res);
        if (res == null) {
            throw new APIException("Could not add new role: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String removeRole(String id, String roleId, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_removeRole", new Object[]{id, roleId, f});

        LOG.info("remove new role {}", res);
        if (res == null) {
            throw new APIException("Could not remove role: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String addAccount(String acctId, String id, String roleId, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_addAccountToOrg", new Object[]{acctId, id, roleId, f});

        LOG.info("add account {}", res);
        if (res == null) {
            throw new APIException("Could not add account: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String changeAccountRole(String acctId, String id, String roleId, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_changeAccountRole", new Object[]{acctId, id, roleId, f});

        LOG.info("change account {}", res);
        if (res == null) {
            throw new APIException("Could not change account: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String updateAccount(String acctId, String id, int action, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_updateAccountStatus", new Object[]{acctId, id, action, f});

        LOG.info("update account {}", res);
        if (res == null) {
            throw new APIException("Could not update account: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String assignAdmin(String acctId, String id, String roleId, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_assignAdminRole", new Object[]{id, acctId, roleId, f});

        LOG.info("assign admin {}", res);
        if (res == null) {
            throw new APIException("Could not assign admin: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String approveAdmin(String acctId, String id, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_approveAdminRole", new Object[]{id, acctId, f});

        LOG.info("approve admin {}", res);
        if (res == null) {
            throw new APIException("Could not approve admin: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String addNode(String id, String enodeId, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_addNode", new Object[]{id, enodeId, f});

        LOG.info("add node {}", res);
        if (res == null) {
            throw new APIException("Could not add node: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String updateNode(String id, String enodeId, int action, Object f) throws APIException {

        Map<String, Object> res = gethService.executeGethCall("quorumPermission_updateNodeStatus", new Object[]{id, enodeId, action, f});

        LOG.info("update node {}", res);
        if (res == null) {
            throw new APIException("Could not update node: " + id);
        }

        return (String) res.get(SIMPLE_RESULT);
    }
}
