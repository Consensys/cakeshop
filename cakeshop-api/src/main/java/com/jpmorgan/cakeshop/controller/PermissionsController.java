package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.APIError;
import com.jpmorgan.cakeshop.model.APIData;
import com.jpmorgan.cakeshop.model.APIResponse;
import com.jpmorgan.cakeshop.model.PermissionsInfo;
import com.jpmorgan.cakeshop.model.PermissionsOrgDetails;
import com.jpmorgan.cakeshop.model.json.PermissionsPostJsonRequest;
import com.jpmorgan.cakeshop.service.PermissionsService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/permissions",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class PermissionsController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionsController.class);

    @Autowired
    PermissionsService permissionsService;

    @RequestMapping("/getList")
    public ResponseEntity<APIResponse> getPermissionsDetails() throws APIException {

        List<PermissionsInfo> details = permissionsService.get().getOrgList();

        List<APIData> data = new ArrayList<>();
        if (details != null && !details.isEmpty()) {
            for (PermissionsInfo info : details) {
                data.add(new APIData(info.getFullOrgId(), "permissions", info));
            }
        }

        APIResponse res = new APIResponse().data(data);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "get org details by id", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/getDetails")
    public ResponseEntity<APIResponse> getPermissionsOrgDetails(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        PermissionsOrgDetails details = permissionsService.get(jsonRequest.getId());

        APIResponse res = new APIResponse();

        if (details != null) {
            res.setData(details.toAPIData());
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Permissions Org not found");
        res.addError(err);
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "enodeId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = false, value = "acct that will be org admin acct", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/addOrg")
    public ResponseEntity<APIResponse> addPermissionsOrg(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String added = permissionsService.addOrg(jsonRequest.getId(), jsonRequest.getEnodeId(), jsonRequest.getAccountId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }


    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "enodeId", required = true, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = true, value = "acct that will be org admin acct", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/approveOrg")
    public ResponseEntity<APIResponse> approvePermissionsOrg(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String approved = permissionsService.approveOrg(jsonRequest.getId(), jsonRequest.getEnodeId(), jsonRequest.getAccountId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(approved), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "action", required = true, value = "action to be performed", dataType = "java.lang.Integer", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/updateOrgStatus")
    public ResponseEntity<APIResponse> updatePermissionsOrgStatus(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String updated = permissionsService.updateOrgStatus(jsonRequest.getId(), jsonRequest.getAction(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(updated), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "action", required = true, value = "action to be performed", dataType = "java.lang.Integer", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/approveOrgStatus")
    public ResponseEntity<APIResponse> approvePermissionsOrgStatus(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String approved = permissionsService.approveOrgStatus(jsonRequest.getId(), jsonRequest.getAction(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(approved), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique suborg identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "parentId", required = true, value = "parent org id of new suborg", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "enodeId", required = true, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/addSubOrg")
    public ResponseEntity<APIResponse> addSubOrg(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String added = permissionsService.addSubOrg(jsonRequest.getId(), jsonRequest.getParentId(), jsonRequest.getEnodeId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "roleId", required = true, value = "unique role id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "access", required = true, value = "account level access", dataType = "java.lang.Integer", paramType = "body"),
        @ApiImplicitParam(name = "voter", required = true, value = "true if voting role", dataType = "boolean", paramType = "body"),
        @ApiImplicitParam(name = "admin", required = true, value = "true if admin role", dataType = "boolean", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/addNewRole")
    public ResponseEntity<APIResponse> addNewRole(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String added = permissionsService.addNewRole(jsonRequest.getId(), jsonRequest.getRoleId(), jsonRequest.getAccess(), jsonRequest.getVoter(), jsonRequest.getAdmin(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "roleId", required = true, value = "unique role id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/removeRole")
    public ResponseEntity<APIResponse> removeRole(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String removed = permissionsService.removeRole(jsonRequest.getId(), jsonRequest.getRoleId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(removed), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = true, value = "org id to which role belongs", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "roleId", required = true, value = "unique role id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/addAccount")
    public ResponseEntity<APIResponse> addAccount(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String added = permissionsService.addAccount(jsonRequest.getAccountId(), jsonRequest.getId(), jsonRequest.getRoleId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = true, value = "unique account id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "roleId", required = true, value = "unique role id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/changeAccount")
    public ResponseEntity<APIResponse> changeAccountRole(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String changed = permissionsService.changeAccountRole(jsonRequest.getAccountId(), jsonRequest.getId(), jsonRequest.getRoleId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(changed), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = true, value = "org id to which role belongs", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "action", required = true, value = "action to be performed", dataType = "java.lang.Integer", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/updateAccount")
    public ResponseEntity<APIResponse> updateAccount(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String updated = permissionsService.updateAccount(jsonRequest.getAccountId(), jsonRequest.getId(), jsonRequest.getAction(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(updated), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = true, value = "unique account id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "roleId", required = true, value = "unique role id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/assignAdmin")
    public ResponseEntity<APIResponse> assignAdmin(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String assigned = permissionsService.assignAdmin(jsonRequest.getAccountId(), jsonRequest.getId(), jsonRequest.getRoleId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(assigned), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = true, value = "unique account id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/approveAdmin")
    public ResponseEntity<APIResponse> approveAdmin(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String approved = permissionsService.approveAdmin(jsonRequest.getAccountId(), jsonRequest.getId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(approved), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = true, value = "unique account id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/recoverAcct")
    public ResponseEntity<APIResponse> recoverAcct(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String recovered = permissionsService.recoverAcct(jsonRequest.getId(), jsonRequest.getAccountId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(recovered), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = true, value = "unique account id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/approveAcct")
    public ResponseEntity<APIResponse> approveAcct(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String approved = permissionsService.approveAcct(jsonRequest.getId(), jsonRequest.getAccountId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(approved), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "enodeId", required = true, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/addNode")
    public ResponseEntity<APIResponse> addNode(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String added = permissionsService.addNode(jsonRequest.getId(), jsonRequest.getEnodeId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "enodeId", required = true, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "action", required = true, value = "action to be performed", dataType = "java.lang.Integer", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/updateNode")
    public ResponseEntity<APIResponse> updateNode(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String updated = permissionsService.updateNode(jsonRequest.getId(), jsonRequest.getEnodeId(), jsonRequest.getAction(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(updated), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "enodeId", required = true, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/recoverNode")
    public ResponseEntity<APIResponse> recoverNode(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String recovered = permissionsService.recoverNode(jsonRequest.getId(), jsonRequest.getEnodeId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(recovered), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = true, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "enodeId", required = true, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = true, value = "acct sending the request", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/approveNode")
    public ResponseEntity<APIResponse> approveNode(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {
        String approved = permissionsService.approveNode(jsonRequest.getId(), jsonRequest.getEnodeId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(approved), HttpStatus.OK);
    }

}
