package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.PermissionsDetails;
import com.jpmorgan.cakeshop.model.PermissionsOrgDetails;

public interface PermissionsService {

    PermissionsOrgDetails get(String id) throws APIException;

    PermissionsDetails get() throws APIException;
}
