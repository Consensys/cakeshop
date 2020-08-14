package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import org.web3j.quorum.methods.response.permissioning.*;
import java.util.List;

public interface PermissionsService {

    OrgDetails get(String id) throws APIException;

    List<PermissionOrgInfo> get() throws APIException;

    String addOrg(String id, String enodeId, String accountId, String from) throws APIException;

    String approveOrg(String id, String enodeId, String accountId, String from) throws APIException;

    String updateOrgStatus(String id, int action, String from) throws APIException;

    String approveOrgStatus(String id, int action, String from) throws APIException;

    String addSubOrg(String id, String parentId, String enodeId, String from) throws APIException;

    String addNewRole(String id, String roleId, int access, boolean isVoter, boolean isAdmin, String from) throws APIException;

    String removeRole(String id, String roleId, String from) throws APIException;

    String addAccount(String acctId, String id, String roleId, String from) throws APIException;

    String changeAccountRole(String acctId, String id, String roleId, String from) throws APIException;

    String updateAccount(String acctId, String id, int action, String from) throws APIException;

    String assignAdmin(String acctId, String id, String roleId, String from) throws APIException;

    String approveAdmin(String acctId, String id, String from) throws APIException;

    String addNode(String id, String enodeId, String from) throws APIException;

    String updateNode(String id, String enodeId, int action, String from) throws APIException;

    String recoverAcct(String id, String accountId, String from) throws APIException;

    String approveAcct(String id, String accountId, String from) throws APIException;

    String recoverNode(String id, String enodeId, String from) throws APIException;

    String approveNode(String id, String enodeId, String from) throws APIException;

}
