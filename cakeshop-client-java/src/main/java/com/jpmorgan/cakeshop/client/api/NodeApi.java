package com.jpmorgan.cakeshop.client.api;

import com.jpmorgan.cakeshop.client.ApiClient;
import com.jpmorgan.cakeshop.client.model.Node;
import com.jpmorgan.cakeshop.client.model.req.NodeUpdateCommand;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;
import com.jpmorgan.cakeshop.client.model.res.SimpleResult;

import feign.Headers;
import feign.RequestLine;
import java.util.List;

public interface NodeApi extends ApiClient.Api {

    /**
     * Get node info and status
     *
     * @return APIResponse<APIData<Node>>
     */
    @RequestLine("POST /node/get")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<APIData<Node>, Node> get();

    @RequestLine("POST /node/update")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<APIData<Node>, Node> update(NodeUpdateCommand command);

    @RequestLine("POST /node/start")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<APIData<SimpleResult>, Boolean> start();

    @RequestLine("POST /node/stop")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<APIData<SimpleResult>, Boolean> stop();

    @RequestLine("POST /node/restart")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<APIData<SimpleResult>, Boolean> restart();

    @RequestLine("POST /node/reset")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<APIData<SimpleResult>, Boolean> reset();

    @RequestLine("POST /node/constellation/list")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<APIData<SimpleResult>, Boolean> constellationList();

    @RequestLine("POST /node/constellation/add")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<APIData<Node>, Node> addConstellation(NodeUpdateCommand command);

    @RequestLine("POST /node/constellation/remove")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<APIData<Node>, Node> removeConstellationNode(NodeUpdateCommand command);

    @RequestLine("POST /node/constellation/stop")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<APIData<SimpleResult>, Boolean> stopConstellation();

    @RequestLine("POST /node/constellation/start")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<APIData<SimpleResult>, Boolean> startConstellation();

    @RequestLine("POST /node/peers/add")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<APIData<SimpleResult>, Boolean> addPeer(NodeUpdateCommand command);

    @RequestLine("POST /node/peers")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<List, Boolean> peers();

    @RequestLine("POST /node/settings/reset")
    @Headers({"Content-type: application/json", "Accepts: application/json",})
    APIResponse<APIData<SimpleResult>, Boolean> resetNode();

}
