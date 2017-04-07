/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.cakeshop.manager.service;

import com.jpmorgan.cakeshop.client.model.Node;
import com.jpmorgan.cakeshop.client.model.req.NodeUpdateCommand;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;
import com.jpmorgan.cakeshop.client.model.res.SimpleResult;

public interface NodeManagerService {

    public APIResponse<APIData<Node>, Node> get(final String cakeshopUrl, final String cred1, final String cred2);

    public APIResponse<APIData<Node>, Node> update(final String cakeshopUrl, final String cred1, final String cred2, final NodeUpdateCommand command);

    public APIResponse<APIData<SimpleResult>, Boolean> start(final String cakeshopUrl, final String cred1, final String cred2);

    public APIResponse<APIData<SimpleResult>, Boolean> stop(final String cakeshopUrl, final String cred1, final String cred2);

    public APIResponse<APIData<SimpleResult>, Boolean> restart(final String cakeshopUrl, final String cred1, final String cred2);

    public APIResponse<APIData<SimpleResult>, Boolean> reset(final String cakeshopUrl, final String cred1, final String cred2);

    public APIResponse<APIData<SimpleResult>, Boolean> constellationList(final String cakeshopUrl, final String cred1, final String cred2);

    public APIResponse<APIData<Node>, Node> addConstellation(final String cakeshopUrl, final String cred1, final String cred2, final NodeUpdateCommand command);

    public APIResponse<APIData<Node>, Node> removeConstellationNode(final String cakeshopUrl, final String cred1, final String cred2, final NodeUpdateCommand command);

    public APIResponse<APIData<SimpleResult>, Boolean> stopConstellation(final String cakeshopUrl, final String cred1, final String cred2);

    public APIResponse<APIData<SimpleResult>, Boolean> startConstellation(final String cakeshopUrl, final String cred1, final String cred2);

    public APIResponse<APIData<SimpleResult>, Boolean> addPeer(final String cakeshopUrl, final String cred1, final String cred2, final NodeUpdateCommand command);

    public APIResponse peers(final String cakeshopUrl, final String cred1, final String cred2);

    public APIResponse<APIData<SimpleResult>, Boolean> resetNode(final String cakeshopUrl, final String cred1, final String cred2);
}
