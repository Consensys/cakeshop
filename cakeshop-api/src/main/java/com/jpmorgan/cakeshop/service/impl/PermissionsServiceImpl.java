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

@Service
public class PermissionsServiceImpl implements PermissionsService {

    static final Logger LOG = LoggerFactory.getLogger(com.jpmorgan.cakeshop.service.impl.BlockServiceImpl.class);

    @Autowired
    private GethHttpService gethService;

    @Override
    public PermissionsOrgDetails get(String id) throws APIException {

        Map<String, Object> permissionData = gethService.executeGethCall("quorumPermission_getOrgDetails", new Object[]{id});

        LOG.info("permission {}", permissionData);

        PermissionsOrgDetails details = null;

        try {

            details = CakeshopUtils.convertToObject(permissionData, PermissionsOrgDetails.class);

            LOG.info("hello");

            LOG.info("all details {}", details);

            LOG.info("rolelist {}", details.getRoleList().get(0).getRoleId());

            Map<String, Object> pInfo = gethService.executeGethCall("quorumPermission_orgList");

            LOG.info("pp {}", pInfo);
            PermissionsDetails info = CakeshopUtils.convertToObject(pInfo, PermissionsDetails.class);

            LOG.info("pinfo {}", info);

            LOG.info("id {}", info.getOrgList().get(0).getFullOrgId());
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

            LOG.info("pp {}", pInfo);
            details = CakeshopUtils.convertToObject(pInfo, PermissionsDetails.class);

            LOG.info("pinfo {}", details);

            LOG.info("id {}", details.getOrgList().get(0).getFullOrgId());
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

        return details;
    }
}
