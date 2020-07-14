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
import org.apache.commons.lang3.StringUtils;
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
        err.setTitle("Block not found");
        res.addError(err);
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "enodeId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = false, value = "acct that will be org admin acct", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/addOrg")
    public ResponseEntity<APIResponse> addPermissionsOrg(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");

        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isBlank(jsonRequest.getEnodeId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'enodeId'")),
                HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isBlank(jsonRequest.getAccountId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'accountId'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.addOrg(jsonRequest.getId(), jsonRequest.getEnodeId(), jsonRequest.getAccountId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }


    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "enodeId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = false, value = "acct that will be org admin acct", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/approveOrg")
    public ResponseEntity<APIResponse> approvePermissionsOrg(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");

        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isBlank(jsonRequest.getEnodeId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'enodeId'")),
                HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isBlank(jsonRequest.getAccountId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'accountId'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.approveOrg(jsonRequest.getId(), jsonRequest.getEnodeId(), jsonRequest.getAccountId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "action", required = false, value = "complete enode id", dataType = "java.lang.Integer", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/updateOrgStatus")
    public ResponseEntity<APIResponse> updatePermissionsOrgStatus(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");

        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.updateOrgStatus(jsonRequest.getId(), jsonRequest.getAction(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "action", required = false, value = "complete enode id", dataType = "java.lang.Integer", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/approveOrgStatus")
    public ResponseEntity<APIResponse> approvePermissionsOrgStatus(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");

        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.approveOrgStatus(jsonRequest.getId(), jsonRequest.getAction(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "parentId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "enodeId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/addSubOrg")
    public ResponseEntity<APIResponse> addSubOrg(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");

        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.addSubOrg(jsonRequest.getId(), jsonRequest.getParentId(), jsonRequest.getEnodeId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "roleId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "access", required = false, value = "complete enode id", dataType = "java.lang.Integer", paramType = "body"),
        @ApiImplicitParam(name = "isVoter", required = false, value = "complete enode id", dataType = "java.lang.Boolean", paramType = "body"),
        @ApiImplicitParam(name = "isAdmin", required = false, value = "complete enode id", dataType = "java.lang.Boolean", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/addNewRole")
    public ResponseEntity<APIResponse> addNewRole(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");

        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.addNewRole(jsonRequest.getId(), jsonRequest.getRoleId(), jsonRequest.getAccess(), jsonRequest.isVoter(), jsonRequest.isAdmin(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "roleId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/removeRole")
    public ResponseEntity<APIResponse> removeRole(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");

        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.removeRole(jsonRequest.getId(), jsonRequest.getRoleId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "roleId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/addAccount")
    public ResponseEntity<APIResponse> addAccount(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");

        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.addAccount(jsonRequest.getAccountId(), jsonRequest.getId(), jsonRequest.getRoleId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "roleId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/changeAccount")
    public ResponseEntity<APIResponse> changeAccountRole(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");
        LOG.info(jsonRequest.getId());
        LOG.info(jsonRequest.getRoleId());
        LOG.info(jsonRequest.getAccountId());

        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.changeAccountRole(jsonRequest.getAccountId(), jsonRequest.getId(), jsonRequest.getRoleId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "action", required = false, value = "complete enode id", dataType = "java.lang.Integer", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/updateAccount")
    public ResponseEntity<APIResponse> updateAccount(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");


        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.updateAccount(jsonRequest.getAccountId(), jsonRequest.getId(), jsonRequest.getAction(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "roleId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/assignAdmin")
    public ResponseEntity<APIResponse> assignAdmin(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");
        LOG.info(jsonRequest.getId());
        LOG.info(jsonRequest.getRoleId());
        LOG.info(jsonRequest.getAccountId());

        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.assignAdmin(jsonRequest.getAccountId(), jsonRequest.getId(), jsonRequest.getRoleId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "accountId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/approveAdmin")
    public ResponseEntity<APIResponse> approveAdmin(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");
        LOG.info(jsonRequest.getId());
        LOG.info(jsonRequest.getRoleId());
        LOG.info(jsonRequest.getAccountId());

        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.approveAdmin(jsonRequest.getAccountId(), jsonRequest.getId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "enodeId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/addNode")
    public ResponseEntity<APIResponse> addNode(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");

        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.approveAdmin(jsonRequest.getId(), jsonRequest.getEnodeId(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "unique org identifier", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "enodeId", required = false, value = "complete enode id", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "action", required = false, value = "complete enode id", dataType = "java.lang.Integer", paramType = "body"),
        @ApiImplicitParam(name = "f", required = false, value = "acct that will be org admin acct", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/updateNode")
    public ResponseEntity<APIResponse> updateNode(@RequestBody PermissionsPostJsonRequest jsonRequest) throws APIException {

        LOG.info("ADDIN PERMISSION ORGS");

        if (StringUtils.isBlank(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                HttpStatus.BAD_REQUEST);
        }

        LOG.info("from {}", jsonRequest.getF());

        String added = permissionsService.updateNode(jsonRequest.getId(), jsonRequest.getEnodeId(), jsonRequest.getAction(), jsonRequest.getF());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

}
