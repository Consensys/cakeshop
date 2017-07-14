package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.bean.GethConfigBean;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.APIData;
import com.jpmorgan.cakeshop.model.APIError;
import com.jpmorgan.cakeshop.model.APIResponse;
import com.jpmorgan.cakeshop.model.ContractABI;
import com.jpmorgan.cakeshop.model.Node;
import com.jpmorgan.cakeshop.model.NodeSettings;
import com.jpmorgan.cakeshop.model.Peer;
import com.jpmorgan.cakeshop.model.TransactionRequest;
import com.jpmorgan.cakeshop.model.json.NodePostJsonRequest;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.NodeService;
import com.jpmorgan.cakeshop.util.FileUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Samer Falah
 */
@RestController
@RequestMapping(value = "/api/node", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class NodeController extends BaseController {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NodeController.class);

    @Autowired
    private GethHttpService gethService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContractService contractService;
    @Autowired
    private GethConfigBean gethConfig;

    private ContractABI voterAbi;

    public NodeController() throws IOException {
        voterAbi = ContractABI.fromJson(FileUtils.readClasspathFile("contracts/BlockVoting.sol.json"));
    }

    @RequestMapping({"/get"})
    protected ResponseEntity<APIResponse> doGet() throws APIException {

        Node node = nodeService.get();

        APIResponse apiResponse = new APIResponse();
        apiResponse.setData(new APIData(node.getId(), "node", node));

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "extraParams", required = false, value = "Extra params to start geth", dataType = "java.lang.String", paramType = "body")
        , 
        @ApiImplicitParam(name = "genesisBlock", required = false, value = "Genesis block", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "blockMakesAccount", required = false, value = "Block maker account (Quorum only)", dataType = "java.lang.Integer", paramType = "body")
        ,
        @ApiImplicitParam(name = "voterAccount", required = false, value = "Voter account (Quorum only)", dataType = "java.lang.String", paramType = "body")
        , 
        @ApiImplicitParam(name = "minBlockTime", required = false, value = "Minimial time to generete new Block (Quorum only)", dataType = "java.lang.Integer", paramType = "body")
        ,
        @ApiImplicitParam(name = "maxBlockTime", required = false, value = "Maximum time to generete new Block (Quorum only)", dataType = "java.lang.Integer", paramType = "body")
        ,
        @ApiImplicitParam(name = "logLevel", required = false, value = "Log verbosity level", dataType = "java.lang.String", paramType = "body")
        , 
        @ApiImplicitParam(name = "networkId", required = false, value = "Network Id", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "committingTransactions", required = false, value = "Commit transactions true/false", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/update")
    public ResponseEntity<APIResponse> update(@RequestBody NodePostJsonRequest jsonRequest) throws APIException {

        Boolean isMining;
        NodeSettings nodeSettings = new NodeSettings().extraParams(jsonRequest.getExtraParams()).genesisBlock(jsonRequest.getGenesisBlock())
                .blockMakerAccount(jsonRequest.getBlockMakerAccount()).voterAccount(jsonRequest.getVoterAccount()).minBlockTime(jsonRequest.getMinBlockTime())
                .maxBlockTime(jsonRequest.getMaxBlockTime());

        try {

            if (!StringUtils.isEmpty(jsonRequest.getLogLevel())) {
                nodeSettings.logLevel(Integer.parseInt(jsonRequest.getLogLevel()));
            }

            if (!StringUtils.isEmpty(jsonRequest.getNetworkId())) {
                nodeSettings.networkId(Integer.parseInt(jsonRequest.getNetworkId()));
            }

            if (jsonRequest.getCommittingTransactions() != null) {

                if (jsonRequest.getCommittingTransactions() instanceof String
                        && StringUtils.isNotBlank((String) jsonRequest.getCommittingTransactions())) {
                    isMining = Boolean.parseBoolean((String) jsonRequest.getCommittingTransactions());
                } else {
                    isMining = (Boolean) jsonRequest.getCommittingTransactions();
                }
                nodeSettings.setIsMining(isMining);
            }

            if (StringUtils.isNotBlank(nodeSettings.getBlockMakerAccount())
                    && (StringUtils.isNotBlank(gethConfig.getBlockMaker())
                    && !nodeSettings.getBlockMakerAccount().contentEquals(gethConfig.getBlockMaker()))) {
                updateVoteContract(gethConfig.getBlockMaker(), "addBlockMaker",
                        new Object[]{nodeSettings.getBlockMakerAccount()});
                updateVoteContract(nodeSettings.getBlockMakerAccount(), "removeBlockMaker",
                        new Object[]{gethConfig.getBlockMaker()});
            }

            if (StringUtils.isNotBlank(nodeSettings.getVoterAccount())
                    && (StringUtils.isNotBlank(gethConfig.getVoteAccount())
                    && !nodeSettings.getVoterAccount().contentEquals(gethConfig.getVoteAccount()))) {
                updateVoteContract(gethConfig.getVoteAccount(), "addVoter",
                        new Object[]{nodeSettings.getVoterAccount()});
                updateVoteContract(nodeSettings.getVoterAccount(), "removeVoter",
                        new Object[]{gethConfig.getVoteAccount()});
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

    @ApiImplicitParams({
        @ApiImplicitParam(name = "address", required = false, value = "Required. External node address to add", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/peers/add")
    public ResponseEntity<APIResponse> addPeer(@RequestBody NodePostJsonRequest jsonRequest) throws APIException {
        if (StringUtils.isBlank(jsonRequest.getAddress())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'address'")),
                    HttpStatus.BAD_REQUEST);
        }
        boolean added = nodeService.addPeer(jsonRequest.getAddress());
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

    @RequestMapping("/constellation/list")
    protected @ResponseBody
    ResponseEntity<APIResponse> getConstellationList() throws APIException {
        Map<String, Object> constellations = nodeService.getConstellationNodes();
        return new ResponseEntity<>(APIResponse.newSimpleResponse(constellations), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "constellationNode", required = false, value = "Required. External constellation address(Quorum only)", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/constellation/add")
    protected @ResponseBody
    ResponseEntity<APIResponse> addConstellation(@RequestBody NodePostJsonRequest jsonRequest)
            throws APIException {
        nodeService.addConstellationNode(jsonRequest.getConstellationNode());
        return doGet();
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "constellationNode", required = false, value = "Required. External constellation address(Quorum only)", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/constellation/remove")
    protected @ResponseBody
    ResponseEntity<APIResponse> removeConstellation(@RequestBody NodePostJsonRequest jsonRequest)
            throws APIException {
        nodeService.removeConstellationNode(jsonRequest.getConstellationNode());
        return doGet();
    }

    @RequestMapping("/constellation/stop")
    protected @ResponseBody
    ResponseEntity<APIResponse> stopConstellation() throws APIException {
        Boolean stopped = gethService.stopConstellation();
        gethConfig.setConstellationEnabled(false);
        try {
            gethConfig.save();
        } catch (IOException ex) {
            throw new APIException(ex);
        }
        return new ResponseEntity<>(APIResponse.newSimpleResponse(stopped), HttpStatus.OK);
    }

    @RequestMapping("/constellation/start")
    protected @ResponseBody
    ResponseEntity<APIResponse> startConstellation() throws APIException {
        Boolean started = gethService.startConstellation();
        gethConfig.setConstellationEnabled(true);
        try {
            gethConfig.save();
        } catch (IOException ex) {
            throw new APIException(ex);
        }
        return new ResponseEntity<>(APIResponse.newSimpleResponse(started), HttpStatus.OK);
    }

    private void updateVoteContract(String from, String method, Object[] args) throws APIException {
        String address = gethConfig.getVoteContractAddress();
        TransactionRequest request = new TransactionRequest(from, address, voterAbi, method, args, false);
        contractService.transact(request);
        try {
            TimeUnit.SECONDS.sleep(gethConfig.getMaxBlockTime());
        } catch (InterruptedException ex) {
            LOG.error(from, ex);
        }
    }

}
