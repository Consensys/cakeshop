package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import org.web3j.quorum.methods.response.permissioning.*;
import java.util.List;

public interface PermissionsService {

    OrgDetails get(String id) throws APIException;

    List<PermissionOrgInfo> get() throws APIException;

    String addOrg(String id, String enodeId, String accountId, String f) throws APIException;

    String approveOrg(String id, String enodeId, String accountId, String f) throws APIException;

    String updateOrgStatus(String id, int action, String f) throws APIException;

    String approveOrgStatus(String id, int action, String f) throws APIException;

    String addSubOrg(String id, String parentId, String enodeId, String f) throws APIException;

    String addNewRole(String id, String roleId, int access, boolean isVoter, boolean isAdmin, String f) throws APIException;

    String removeRole(String id, String roleId, String f) throws APIException;

    String addAccount(String acctId, String id, String roleId, String f) throws APIException;

    String changeAccountRole(String acctId, String id, String roleId, String f) throws APIException;

    String updateAccount(String acctId, String id, int action, String f) throws APIException;

    String assignAdmin(String acctId, String id, String roleId, String f) throws APIException;

    String approveAdmin(String acctId, String id, String f) throws APIException;

    String addNode(String id, String enodeId, String f) throws APIException;

    String updateNode(String id, String enodeId, int action, String f) throws APIException;

    String recoverAcct(String id, String accountId, Object f) throws APIException;

    String approveAcct(String id, String accountId, Object f) throws APIException;

    String recoverNode(String id, String enodeId, String f) throws APIException;

    String approveNode(String id, String enodeId, String f) throws APIException;

}
