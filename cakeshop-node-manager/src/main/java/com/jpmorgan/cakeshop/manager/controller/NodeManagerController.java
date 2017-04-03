package com.jpmorgan.cakeshop.manager.controller;

import com.jpmorgan.cakeshop.client.model.Node;
import com.jpmorgan.cakeshop.client.model.req.NodeUpdateCommand;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;
import com.jpmorgan.cakeshop.manager.model.json.NodeJsonRequest;
import com.jpmorgan.cakeshop.manager.service.NodeManagerService;

import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/node", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class NodeManagerController {

    @Autowired
    private NodeManagerService service;

    @RequestMapping({"/get"})
    protected ResponseEntity<APIResponse<APIData<Node>, Node>> get(@RequestBody NodeJsonRequest jsonRequest) {
        APIResponse<APIData<Node>, Node> resposnse = service.get(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2());
        return new ResponseEntity<>(resposnse, OK);
    }

    @RequestMapping("/update")
    public ResponseEntity<APIResponse> update(@RequestBody NodeJsonRequest jsonRequest) {

        NodeUpdateCommand command = new NodeUpdateCommand()
                .extraParams(jsonRequest.getExtraParams())
                .blockMakerAccount(jsonRequest.getBlockMakerAccount())
                .logLevel(jsonRequest.getLogLevel())
                .networkId(jsonRequest.getNetworkId())
                .commitingTransactions(jsonRequest.getCommittingTransactions())
                .genesisBlock(jsonRequest.getGenesisBlock())
                .voterAccount(jsonRequest.getVoterAccount())
                .minBlockTime(jsonRequest.getMinBlockTime())
                .maxBlockTime(jsonRequest.getMaxBlockTime());
        APIResponse resposnse = service.update(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2(), command);
        return new ResponseEntity<>(resposnse, OK);
    }

    @RequestMapping("/start")
    protected @ResponseBody
    ResponseEntity<APIResponse> start(@RequestBody NodeJsonRequest jsonRequest) {
        APIResponse resposnse = service.start(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2());
        return new ResponseEntity<>(resposnse, OK);
    }

    @RequestMapping("/stop")
    protected @ResponseBody
    ResponseEntity<APIResponse> stop(@RequestBody NodeJsonRequest jsonRequest) {
        APIResponse resposnse = service.stop(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2());
        return new ResponseEntity<>(resposnse, OK);
    }

    @RequestMapping("/restart")
    protected @ResponseBody
    ResponseEntity<APIResponse> restart(@RequestBody NodeJsonRequest jsonRequest) {
        APIResponse resposnse = service.restart(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2());
        return new ResponseEntity<>(resposnse, OK);
    }

    @RequestMapping("/reset")
    protected @ResponseBody
    ResponseEntity<APIResponse> reset(@RequestBody NodeJsonRequest jsonRequest) {
        APIResponse resposnse = service.reset(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2());
        return new ResponseEntity<>(resposnse, OK);
    }

    @RequestMapping("/settings/reset")
    protected @ResponseBody
    ResponseEntity<APIResponse> resetNode(@RequestBody NodeJsonRequest jsonRequest) {
        APIResponse resposnse = service.resetNode(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2());
        return new ResponseEntity<>(resposnse, OK);
    }

    @RequestMapping("/peers/add")
    public ResponseEntity<APIResponse> addPeer(@RequestBody NodeJsonRequest jsonRequest) {
        NodeUpdateCommand command = new NodeUpdateCommand().address(jsonRequest.getAddress());
        APIResponse resposnse = service.addPeer(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2(), command);
        return new ResponseEntity<>(resposnse, OK);
    }

    @RequestMapping("/peers")
    public ResponseEntity<String> peers(@RequestBody NodeJsonRequest jsonRequest) {
        APIResponse resposnse = service.peers(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2());
        return new ResponseEntity<>(resposnse.toString(), OK);
    }

    @RequestMapping("/constellation/list")
    protected @ResponseBody
    ResponseEntity<APIResponse> getConstellationList(@RequestBody NodeJsonRequest jsonRequest) {
        APIResponse resposnse = service.constellationList(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2());
        return new ResponseEntity<>(resposnse, OK);
    }

    @RequestMapping("/constellation/add")
    protected @ResponseBody
    ResponseEntity<APIResponse> addConstellation(@RequestBody NodeJsonRequest jsonRequest) {
        NodeUpdateCommand command = new NodeUpdateCommand().constellationNode(jsonRequest.getConstellationNode());
        APIResponse resposnse = service.addConstellation(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2(), command);
        return new ResponseEntity<>(resposnse, OK);
    }

    @RequestMapping("/constellation/remove")
    protected @ResponseBody
    ResponseEntity<APIResponse> removeConstellation(@RequestBody NodeJsonRequest jsonRequest) {
        NodeUpdateCommand command = new NodeUpdateCommand().constellationNode(jsonRequest.getConstellationNode());
        APIResponse resposnse = service.addConstellation(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2(), command);
        return new ResponseEntity<>(resposnse, OK);
    }

    @RequestMapping("/constellation/stop")
    protected @ResponseBody
    ResponseEntity<APIResponse> stopConstellation(@RequestBody NodeJsonRequest jsonRequest) {
        APIResponse resposnse = service.stopConstellation(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2());
        return new ResponseEntity<>(resposnse, OK);
    }

    @RequestMapping("/constellation/start")
    protected @ResponseBody
    ResponseEntity<APIResponse> startConstellation(@RequestBody NodeJsonRequest jsonRequest) {
        APIResponse resposnse = service.startConstellation(jsonRequest.getCakeshopUrl(), jsonRequest.getCred1(), jsonRequest.getCred2());
        return new ResponseEntity<>(resposnse, OK);
    }

}
