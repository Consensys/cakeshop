package com.jpmorgan.cakeshop.manager.service.impl;

import com.jpmorgan.cakeshop.client.ApiClient;
import com.jpmorgan.cakeshop.client.api.NodeApi;
import com.jpmorgan.cakeshop.client.model.Node;
import com.jpmorgan.cakeshop.client.model.req.NodeUpdateCommand;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;
import com.jpmorgan.cakeshop.client.model.res.SimpleResult;
import com.jpmorgan.cakeshop.manager.service.NodeManagerService;
import feign.auth.BasicAuthRequestInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class NodeManagerServiceImpl implements NodeManagerService {

    @Override
    public APIResponse<APIData<Node>, Node> get(final String cakeshopUrl, final String cred1, final String cred2) {
        return getNodeApi(cakeshopUrl, cred1, cred2).get();
    }

    @Override
    public APIResponse<APIData<Node>, Node> update(final String cakeshopUrl, final String cred1, final String cred2, final NodeUpdateCommand command) {
        return getNodeApi(cakeshopUrl, cred1, cred2).update(command);
    }

    @Override
    public APIResponse<APIData<SimpleResult>, Boolean> start(final String cakeshopUrl, final String cred1, final String cred2) {
        return getNodeApi(cakeshopUrl, cred1, cred2).start();
    }

    @Override
    public APIResponse<APIData<SimpleResult>, Boolean> stop(final String cakeshopUrl, final String cred1, final String cred2) {
        return getNodeApi(cakeshopUrl, cred1, cred2).stop();
    }

    @Override
    public APIResponse<APIData<SimpleResult>, Boolean> restart(final String cakeshopUrl, final String cred1, final String cred2) {
        return getNodeApi(cakeshopUrl, cred1, cred2).restart();
    }

    @Override
    public APIResponse<APIData<SimpleResult>, Boolean> reset(final String cakeshopUrl, final String cred1, final String cred2) {
        return getNodeApi(cakeshopUrl, cred1, cred2).reset();
    }

    @Override
    public APIResponse<APIData<SimpleResult>, Boolean> constellationList(final String cakeshopUrl, final String cred1, final String cred2) {
        return getNodeApi(cakeshopUrl, cred1, cred2).constellationList();
    }

    @Override
    public APIResponse<APIData<Node>, Node> addConstellation(final String cakeshopUrl, final String cred1, final String cred2, final NodeUpdateCommand command) {
        return getNodeApi(cakeshopUrl, cred1, cred2).addConstellation(command);
    }

    @Override
    public APIResponse<APIData<Node>, Node> removeConstellationNode(final String cakeshopUrl, final String cred1, final String cred2, final NodeUpdateCommand command) {
        return getNodeApi(cakeshopUrl, cred1, cred2).removeConstellationNode(command);
    }

    @Override
    public APIResponse<APIData<SimpleResult>, Boolean> stopConstellation(final String cakeshopUrl, final String cred1, final String cred2) {
        return getNodeApi(cakeshopUrl, cred1, cred2).stopConstellation();
    }

    @Override
    public APIResponse<APIData<SimpleResult>, Boolean> startConstellation(final String cakeshopUrl, final String cred1, final String cred2) {
        return getNodeApi(cakeshopUrl, cred1, cred2).startConstellation();
    }

    @Override
    public APIResponse<APIData<SimpleResult>, Boolean> addPeer(final String cakeshopUrl, final String cred1, final String cred2, final NodeUpdateCommand command) {
        return getNodeApi(cakeshopUrl, cred1, cred2).addPeer(command);
    }

    @Override
    public APIResponse peers(final String cakeshopUrl, final String cred1, final String cred2) {
        return getNodeApi(cakeshopUrl, cred1, cred2).peers();
    }

    @Override
    public APIResponse<APIData<SimpleResult>, Boolean> resetNode(final String cakeshopUrl, final String cred1, final String cred2) {
        return getNodeApi(cakeshopUrl, cred1, cred2).resetNode();
    }

    private NodeApi getNodeApi(String cakeshopUrl, String cred1, String cred2) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(cakeshopUrl.concat("/api"));
        if (StringUtils.isNotBlank(cred1) && StringUtils.isNotBlank(cred2)) {
            BasicAuthRequestInterceptor interceptor = new BasicAuthRequestInterceptor(cred1, cred2);
            apiClient.addAuthorization("basic", interceptor);
        }
        return apiClient.buildClient(NodeApi.class);
    }

}
