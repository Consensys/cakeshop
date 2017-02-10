package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.bean.GethConfigBean;
import com.jpmorgan.cakeshop.config.JsonMethodArgumentResolver.JsonBodyParam;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.APIData;
import com.jpmorgan.cakeshop.model.APIError;
import com.jpmorgan.cakeshop.model.APIResponse;
import com.jpmorgan.cakeshop.model.Node;
import com.jpmorgan.cakeshop.model.NodeSettings;
import com.jpmorgan.cakeshop.model.Peer;
import com.jpmorgan.cakeshop.model.RequestModel;
import com.jpmorgan.cakeshop.model.TransactionRequest;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.NodeService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Samer Falah
 */
@RestController
@RequestMapping(value = "/api/node",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class NodeController extends BaseController {

    @Autowired
    private GethHttpService gethService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContractService contractService;
    @Autowired
    private GethConfigBean gethConfig;

    @RequestMapping({"/get"})
    protected ResponseEntity<APIResponse> doGet() throws APIException {

        Node node = nodeService.get();

        APIResponse apiResponse = new APIResponse();
        apiResponse.setData(new APIData(node.getId(), "node", node));

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @RequestMapping("/update")
    public ResponseEntity<APIResponse> update(
            @JsonBodyParam(required = false) String logLevel,
            @JsonBodyParam(required = false) String networkId,
            @JsonBodyParam(required = false) String identity,
            @JsonBodyParam(required = false) Object committingTransactions,
            @JsonBodyParam(required = false) String extraParams,
            @JsonBodyParam(required = false) String genesisBlock,
            @JsonBodyParam(required = false) String blockMakerAccount,
            @JsonBodyParam(required = false) String voterAccount,
            @JsonBodyParam(required = false) Integer minBlockTime,
            @JsonBodyParam(required = false) Integer maxBlockTime) throws APIException {

        Boolean isMining;
        NodeSettings nodeSettings = new NodeSettings()
                .extraParams(extraParams)
                .genesisBlock(genesisBlock)
                .blockMakerAccount(blockMakerAccount)
                .voterAccount(voterAccount)
                .minBlockTime(minBlockTime)
                .maxBlockTime(maxBlockTime);

        try {

            if (!StringUtils.isEmpty(logLevel)) {
                nodeSettings.logLevel(Integer.parseInt(logLevel));
            }

            if (!StringUtils.isEmpty(networkId)) {
                nodeSettings.networkId(Integer.parseInt(networkId));
            }

            if (committingTransactions != null) {
                if (committingTransactions instanceof String && StringUtils.isNotBlank((String) committingTransactions)) {
                    isMining = Boolean.parseBoolean((String) committingTransactions);
                } else {
                    isMining = (Boolean) committingTransactions;
                }
                nodeSettings.setIsMining(isMining);
            }

            if (StringUtils.isNotBlank(nodeSettings.getBlockMakerAccount()) && (StringUtils.isNotBlank(gethConfig.getBlockMaker())
                    && !nodeSettings.getBlockMakerAccount().contentEquals(gethConfig.getBlockMaker()))) {
                updateVoteContract("addBlockMaker", new Object[]{nodeSettings.getBlockMakerAccount()});
                updateVoteContract("removeBlockMaker", new Object[]{gethConfig.getBlockMaker()});
            } else if (StringUtils.isNotBlank(nodeSettings.getBlockMakerAccount()) && StringUtils.isBlank(gethConfig.getBlockMaker())) {
                updateVoteContract("addBlockMaker", new Object[]{nodeSettings.getBlockMakerAccount()});
            }

            if (StringUtils.isNotBlank(nodeSettings.getVoterAccount()) && (StringUtils.isNotBlank(gethConfig.getVoteAccount())
                    && !nodeSettings.getVoterAccount().contentEquals(gethConfig.getVoteAccount()))) {
                updateVoteContract("addVoter", new Object[]{nodeSettings.getVoterAccount()});
                updateVoteContract("removeVoter", new Object[]{gethConfig.getVoteAccount()});
            } else if (StringUtils.isNotBlank(nodeSettings.getVoterAccount()) && StringUtils.isBlank(gethConfig.getVoteAccount())) {
                updateVoteContract("addVoter", new Object[]{nodeSettings.getVoterAccount()});
            }

            nodeService.update(nodeSettings);

            return doGet();

        } catch (NumberFormatException ne) {
            APIError err = new APIError();
            err.setStatus("400");
            err.setTitle("Input Formatting Error");

            APIResponse res = new APIResponse();
            res.addError(err);

            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping("/peers/add")
    public ResponseEntity<APIResponse> addPeer(@JsonBodyParam String address) throws APIException {
        if (StringUtils.isBlank(address)) {
            return new ResponseEntity<>(
                    new APIResponse().error(new APIError().title("Missing param 'address'")),
                    HttpStatus.BAD_REQUEST);
        }
        boolean added = nodeService.addPeer(address);
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @RequestMapping("/peers")
    public ResponseEntity<APIResponse> peers() throws APIException {
        List<Peer> peers = nodeService.peers();
        List<APIData> data = new ArrayList<>();
        if (peers != null && !peers.isEmpty()) {
            for (Peer peer : peers) {
                data.add(new APIData(peer.getId(), "peer", peer));
            }
        }

        APIResponse res = new APIResponse().data(data);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @RequestMapping("/start")
    protected @ResponseBody
    ResponseEntity<APIResponse> startGeth() {
        Boolean started = gethService.start();
        return new ResponseEntity<>(APIResponse.newSimpleResponse(started), HttpStatus.OK);
    }

    @RequestMapping("/stop")
    protected @ResponseBody
    ResponseEntity<APIResponse> stopGeth() {
        Boolean stopped = gethService.stop();
        gethService.deletePid();
        return new ResponseEntity<>(APIResponse.newSimpleResponse(stopped), HttpStatus.OK);
    }

    @RequestMapping("/restart")
    protected @ResponseBody
    ResponseEntity<APIResponse> restartGeth() {
        Boolean stopped = gethService.stop();
        Boolean deleted = gethService.deletePid();
        Boolean restarted = false;
        if (stopped && deleted) {
            restarted = gethService.start();
        }
        return new ResponseEntity<>(APIResponse.newSimpleResponse(restarted), HttpStatus.OK);
    }

    @RequestMapping("/reset")
    protected @ResponseBody
    ResponseEntity<APIResponse> resetGeth() {
        Boolean reset = gethService.reset();
        return new ResponseEntity<>(APIResponse.newSimpleResponse(reset), HttpStatus.OK);
    }

    @RequestMapping("/settings/reset")
    protected @ResponseBody
    ResponseEntity<APIResponse> resetNodeInfo() {
        Boolean reset = nodeService.reset();
        return new ResponseEntity<>(APIResponse.newSimpleResponse(reset), HttpStatus.OK);
    }

    private void updateVoteContract(String method, Object[] args) throws APIException {
        String address = gethConfig.getVoteContractAddress();
        contractService.transact(address, null, method, args);
    }

}
