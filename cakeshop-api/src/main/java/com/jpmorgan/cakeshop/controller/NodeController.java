package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.bean.GethConfig;
import com.jpmorgan.cakeshop.dao.NodeInfoDAO;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.*;
import com.jpmorgan.cakeshop.model.json.NodePostJsonRequest;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.NodeService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
    private RestTemplate restTemplate;

    @Autowired
    private GethConfig gethConfig;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private NodeInfoDAO nodeInfoDAO;

    public NodeController() throws IOException {
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
        @ApiImplicitParam(name = "logLevel", required = false, value = "Log verbosity level", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "networkId", required = false, value = "Network Id", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "committingTransactions", required = false, value = "Commit transactions true/false", dataType = "java.lang.Object", paramType = "body")
    })
    @RequestMapping("/update")
    public ResponseEntity<APIResponse> update(@RequestBody NodePostJsonRequest jsonRequest) throws APIException {

        Boolean isMining;
        NodeSettings nodeSettings = new NodeSettings().extraParams(jsonRequest.getExtraParams()).genesisBlock(jsonRequest.getGenesisBlock());

        try {

            if (!StringUtils.isEmpty(jsonRequest.getLogLevel())) {
                nodeSettings.logLevel(Integer.parseInt(jsonRequest.getLogLevel()));
            }

            if (!StringUtils.isEmpty(jsonRequest.getNetworkId())) {
                nodeSettings.networkId(Long.parseLong(jsonRequest.getNetworkId()));
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

    @ApiImplicitParams({
        @ApiImplicitParam(name = "address", required = false, value = "Required. External node address to remove", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/peers/remove")
    public ResponseEntity<APIResponse> removePeer(@RequestBody NodePostJsonRequest jsonRequest) throws APIException {
        if (StringUtils.isBlank(jsonRequest.getAddress())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'address'")),
                HttpStatus.BAD_REQUEST);
        }
        boolean removed = nodeService.removePeer(jsonRequest.getAddress());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(removed), HttpStatus.OK);
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
        return new ResponseEntity<>(APIResponse.newSimpleResponse(stopped), HttpStatus.OK);
    }

    @RequestMapping("/restart")
    protected @ResponseBody
    ResponseEntity<APIResponse> restartGeth() {
        Boolean stopped = gethService.stop();
        Boolean restarted = false;
        if (stopped) {
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

    @RequestMapping("/tm/peers")
    protected @ResponseBody
    ResponseEntity<APIResponse> getTransactionManagerPeers() throws APIException {
        String transactionManagerUrl = gethConfig.getGethTransactionManagerUrl();
        HttpHeaders jsonContentHeaders = new HttpHeaders();
        jsonContentHeaders.setContentType(APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonContentHeaders);
        ResponseEntity<Map> exchange = restTemplate
            .exchange(transactionManagerUrl + "/partyinfo", HttpMethod.GET, httpEntity, Map.class);
        return new ResponseEntity<>(APIResponse.newSimpleResponse(exchange.getBody()), HttpStatus.OK);
    }

    @RequestMapping("/tm/list")
    protected @ResponseBody
    ResponseEntity<APIResponse> getTransactionManagerList() throws APIException {
        Map<String, Object> transactionManagerNodes = nodeService.getTransactionManagerNodes();
        return new ResponseEntity<>(APIResponse.newSimpleResponse(transactionManagerNodes), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "transactionManagerNode", required = false, value = "Required. External transaction manager address(Quorum only)", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/tm/add")
    protected @ResponseBody
    ResponseEntity<APIResponse> addTransactionManagerNode(@RequestBody NodePostJsonRequest jsonRequest)
            throws APIException {
        nodeService.addTransactionManagerNode(jsonRequest.getTransactionManagerNode());
        return doGet();
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "transactionManagerNode", required = false, value = "Required. External transaction manager address(Quorum only)", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/tm/remove")
    protected @ResponseBody
    ResponseEntity<APIResponse> removeTransactionManagerNode(@RequestBody NodePostJsonRequest jsonRequest)
            throws APIException {
        nodeService.removeTransactionManagerNode(jsonRequest.getTransactionManagerNode());
        return doGet();
    }

    @RequestMapping("/tm/stop")
    protected @ResponseBody
    ResponseEntity<APIResponse> stopTransactionManager() throws APIException {
        Boolean stopped = gethService.stopTransactionManager();
        return new ResponseEntity<>(APIResponse.newSimpleResponse(stopped), HttpStatus.OK);
    }

    @RequestMapping("/tm/start")
    protected @ResponseBody
    ResponseEntity<APIResponse> startTransactionManager() throws APIException {
        boolean success = gethService.startTransactionManager();
        return new ResponseEntity<>(APIResponse.newSimpleResponse(success), HttpStatus.OK);
    }

    @GetMapping(path = "/nodes")
    protected @ResponseBody
    ResponseEntity<APIResponse> getNodes() throws APIException {
        List<NodeInfo> list = nodeInfoDAO.list();
        list.forEach((nodeInfo -> {
            if(nodeInfo.rpcUrl.equals(gethService.getCurrentRpcUrl())) {
                nodeInfo.isSelected = true;
            }
        }));
        return new ResponseEntity<>(APIResponse.newSimpleResponse(list), HttpStatus.OK);
    }

    @PostMapping(path = "/add")
    protected @ResponseBody
    ResponseEntity<APIResponse> addNode(@RequestBody NodeInfo nodeInfo) throws IOException {
        nodeInfoDAO.save(nodeInfo);
        return new ResponseEntity<>(APIResponse.newSimpleResponse(nodeInfo), HttpStatus.CREATED);
    }

    @PostMapping(path = "/addAll")
    protected @ResponseBody
    ResponseEntity<APIResponse> addAllNodes(@RequestBody List<NodeInfo> nodeInfos) throws IOException {
        nodeInfoDAO.save(nodeInfos);
        return new ResponseEntity<>(APIResponse.newSimpleResponse(nodeInfos), HttpStatus.CREATED);
    }

    @PostMapping(path = "/remove")
    protected @ResponseBody
    ResponseEntity<APIResponse> removeNode(@RequestBody NodeInfo nodeInfo) throws IOException {
        nodeInfoDAO.delete(nodeInfo);
        return new ResponseEntity<>(APIResponse.newSimpleResponse(nodeInfo), HttpStatus.NO_CONTENT);
    }

    @GetMapping(path = "/currentUrl")
    protected @ResponseBody
    ResponseEntity<APIResponse> getNodeUrl() throws APIException {
        return new ResponseEntity<>(APIResponse.newSimpleResponse(gethService.getCurrentRpcUrl()),
            HttpStatus.OK);
    }

    @PostMapping(path = "/url")
    protected @ResponseBody
    ResponseEntity<APIResponse> setNodeUrls(@RequestBody NodeInfo nodeInfo)
        throws APIException {
        gethService.connectToNode(nodeInfo.id);
        // clear cache for contracts so that we don't keep private contracts for the wrong node
        Cache cache = cacheManager.getCache("contracts");
        if (cache != null) {
            cache.clear();
        }
        return new ResponseEntity<>(APIResponse.newSimpleResponse(true), HttpStatus.OK);
    }
}
