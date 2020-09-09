package com.jpmorgan.cakeshop.service.impl;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.service.PermissionsService;
import com.jpmorgan.cakeshop.service.GethHttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;


import org.web3j.quorum.methods.response.permissioning.*;
import org.web3j.quorum.methods.request.*;
import org.web3j.protocol.core.Request;

import java.math.BigInteger;
import java.util.Map;
import java.util.List;

import static com.jpmorgan.cakeshop.service.impl.GethHttpServiceImpl.SIMPLE_RESULT;

@Service
public class PermissionsServiceImpl implements PermissionsService {

    static final Logger LOG = LoggerFactory.getLogger(com.jpmorgan.cakeshop.service.impl.PermissionsServiceImpl.class);

    @Autowired
    private GethHttpService gethService;

    @Override
    public OrgDetails get(String id) throws APIException {

        OrgDetailsInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionGetOrgDetails(id).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getOrgDetails();
        } catch (IOException e) {
            throw new APIException("Could not retrieve Permission Org details: " + e.getMessage());
        }
    }

    @Override
    public List<PermissionOrgInfo> get() throws APIException {
        PermissionOrgList list = null;
        try {
            list = gethService.getQuorumService().quorumPermissionGetOrgList().send();
            if(list.hasError()) {
                throw new APIException(list.getError().getMessage());
            }
            return list.getPermissionOrgList();
        } catch (IOException e) {
            throw new APIException("Could not retrieve Permission Org List: " + e.getMessage());
        }
    }

    @Override
    public String addOrg(String id, String enodeId, String accountId, String from) throws APIException {
        LOG.info("Add new permissions org {}", id);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionAddOrg(id, enodeId, accountId, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not add org:  " + e.getMessage());
        }
    }

    @Override
    public String approveOrg(String id, String enodeId, String accountId, String from) throws APIException {
        LOG.info("Approve new permissions org {}", id);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionApproveOrg(id, enodeId, accountId, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not approve org: " + e.getMessage());
        }
    }

    @Override
    public String updateOrgStatus(String id, int action, String from) throws APIException {
        LOG.info("update permissions org {} status: {}", id, action);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionUpdateOrgStatus(id, action, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not update org status: " + e.getMessage());
        }

    }

    @Override
    public String approveOrgStatus(String id, int action, String from) throws APIException {
        LOG.info("approve permissions org {} status: {}", id, action);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionApproveOrgStatus(id, action, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not approve org status: " + e.getMessage());
        }
    }

    @Override
    public String addSubOrg(String id, String parentId, String enodeId, String from) throws APIException {
        LOG.info("add new suborg {} to parent {}", id, parentId);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionAddSubOrg(parentId, id, enodeId, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not add sub org: " + e.getMessage());
        }
    }

    @Override
    public String addNewRole(String id, String roleId, int access, boolean isVoter, boolean isAdmin, String from) throws APIException {
        LOG.info("add new permissions role {} to org {}", roleId, id);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionAddNewRole(id, roleId, access, isVoter, isAdmin, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not add new role: " + e.getMessage());
        }
    }

    @Override
    public String removeRole(String id, String roleId, String from) throws APIException {
        LOG.info("remove new role {} from org {}", roleId, id);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionRemoveRole(id, roleId, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not remove role: " + e.getMessage());
        }
    }

    @Override
    public String addAccount(String acctId, String id, String roleId, String from) throws APIException {
        LOG.info("add account {} to org {}", acctId, id);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionAddAccountToOrg(acctId, id, roleId, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not add account: " + e.getMessage());
        }
    }

    @Override
    public String changeAccountRole(String acctId, String id, String roleId, String from) throws APIException {
        LOG.info("change account role to {} for acct {}", roleId, acctId);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionChangeAccountRole(acctId, id, roleId, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (Exception e) {
            throw new APIException("Could not change account role for acct: " + e.getMessage());
        }
    }

    @Override
    public String updateAccount(String acctId, String id, int action, String from) throws APIException {
        LOG.info("update account {} status to {} ", acctId, action);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionUpdateAccountStatus(id, acctId, action, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not update account: " + e.getMessage());
        }
    }

    @Override
    public String assignAdmin(String acctId, String id, String roleId, String from) throws APIException {
        LOG.info("assign admin to account {}", acctId);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionAssignAdminRole(id, acctId, roleId, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not assign admin for acct: " + e.getMessage());
        }
    }

    @Override
    public String approveAdmin(String acctId, String id, String from) throws APIException {
        LOG.info("approve assignment of admin for account {}", acctId);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionApproveAdminRole(id, acctId, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not approve admin for acct: " + e.getMessage());
        }
    }

    @Override
    public String addNode(String id, String enodeId, String from) throws APIException {
        LOG.info("add new node {} to org {}", enodeId, id);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionAddNode(id, enodeId, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not add node: " + e.getMessage());
        }
    }

    @Override
    public String updateNode(String id, String enodeId, int action, String from) throws APIException {
        LOG.info("update status {} of node {}", action, enodeId);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionUpdateNodeStatus(id, enodeId, action, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not update node: " + e.getMessage());
        }
    }

    @Override
    public String recoverAcct(String id, String accountId, Object from) throws APIException {
        LOG.info("recover blacklisted account {}", accountId);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_recoverBlackListedAccount", new Object[]{id, accountId, from});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not recover acct: " + accountId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String approveAcct(String id, String accountId, Object from) throws APIException {
        LOG.info("approve recovery of blacklisted account {}", accountId);
        Map<String, Object> res = gethService.executeGethCall("quorumPermission_approveBlackListedAccountRecovery", new Object[]{id, accountId, from});

        if (res == null || res.get(SIMPLE_RESULT) == null) {
            throw new APIException("Could not approve recover of acct: " + accountId);
        }

        return (String) res.get(SIMPLE_RESULT);
    }

    @Override
    public String recoverNode(String id, String enodeId, String from) throws APIException {
        LOG.info("recover blacklisted node {}", enodeId);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionRecoverBlackListedNode(id, enodeId, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not recover node: " + e.getMessage());
        }
    }

    @Override
    public String approveNode(String id, String enodeId, String from) throws APIException {
        LOG.info("approve recovery of blacklisted node {}", enodeId);
        ExecStatusInfo res = null;
        try {
            res = gethService.getQuorumService().quorumPermissionApproveBlackListedNodeRecovery(id, enodeId, createPrivateTransaction(from)).send();
            if(res.hasError()) {
                throw new APIException(res.getError().getMessage());
            }
            return res.getExecStatus();
        } catch (IOException e) {
            throw new APIException("Could not approve recover of node: " + e.getMessage());
        }
    }

    private PrivateTransaction createPrivateTransaction(String address) {
        return new PrivateTransaction(address, BigInteger.ZERO, BigInteger.valueOf(4700000), null, BigInteger.ZERO, null, null, null);
    }
}
