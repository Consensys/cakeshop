package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.PermissionsDetails;
import com.jpmorgan.cakeshop.model.PermissionsOrgDetails;

public interface PermissionsService {

    PermissionsOrgDetails get(String id) throws APIException;

    PermissionsDetails get() throws APIException;

    String addOrg(String id, String enodeId, String accountId, Object f) throws APIException;

    String approveOrg(String id, String enodeId, String accountId, Object f) throws APIException;

    String updateOrgStatus(String id, int action, Object f) throws APIException;

    String approveOrgStatus(String id, int action, Object f) throws APIException;

    String addSubOrg(String id, String parentId, String enodeId, Object f) throws APIException;

    String addNewRole(String id, String roleId, int access, boolean isVoter, boolean isAdmin, Object f) throws APIException;

    String removeRole(String id, String roleId, Object f) throws APIException;

    String addAccount(String acctId, String id, String roleId, Object f) throws APIException;

    String changeAccountRole(String acctId, String id, String roleId, Object f) throws APIException;

    String updateAccount(String acctId, String id, int action, Object f) throws APIException;

    String assignAdmin(String acctId, String id, String roleId, Object f) throws APIException;

    String approveAdmin(String acctId, String id, Object f) throws APIException;

    String addNode(String id, String enodeId, Object f) throws APIException;

    String updateNode(String id, String enodeId, int action, Object f) throws APIException;

}
