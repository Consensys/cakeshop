package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.*;
import com.jpmorgan.cakeshop.model.json.NodePostJsonRequest;
import com.jpmorgan.cakeshop.repo.NodeInfoRepository;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.NodeService;
import com.jpmorgan.cakeshop.service.ReportingHttpService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigInteger;
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
    private ReportingHttpService reportingService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private NodeInfoRepository nodeInfoRepository;

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
        @ApiImplicitParam(name = "address", required = false, value = "Required. External node address to add", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "raftLearner", required = false, defaultValue = "false", value = "Whether the node should be added as a raft learner", dataType = "java.lang.Boolean", paramType = "body")
    })
    @RequestMapping("/peers/add")
    public ResponseEntity<APIResponse> addPeer(@RequestBody NodePostJsonRequest jsonRequest) throws APIException {
        if (StringUtils.isBlank(jsonRequest.getAddress())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'address'")),
                    HttpStatus.BAD_REQUEST);
        }
        boolean added = nodeService.addPeer(jsonRequest.getAddress(), jsonRequest.isRaftLearner());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(added), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "address", required = false, value = "Required. External node address to add", dataType = "java.lang.String", paramType = "body"),
    })
    @RequestMapping("/peers/promote")
    public ResponseEntity<APIResponse> promoteToPeer(@RequestBody NodePostJsonRequest jsonRequest) throws APIException {
        if (StringUtils.isBlank(jsonRequest.getAddress())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'address'")),
                HttpStatus.BAD_REQUEST);
        }
        nodeService.promoteToPeer(jsonRequest.getAddress());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(true), HttpStatus.OK);
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


    @RequestMapping("/tm/peers")
    protected @ResponseBody
    ResponseEntity<APIResponse> getTransactionManagerPeers() throws APIException {
        String partyInfoUrl = UriComponentsBuilder
            .fromHttpUrl(gethService.getCurrentTransactionManagerUrl())
            .path("/partyinfo/keys")
            .toUriString();
        HttpHeaders jsonContentHeaders = new HttpHeaders();
        jsonContentHeaders.setContentType(APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonContentHeaders);
        ResponseEntity<Map> exchange = restTemplate
            .exchange(partyInfoUrl, HttpMethod.GET, httpEntity, Map.class);
        return new ResponseEntity<>(APIResponse.newSimpleResponse(exchange.getBody()), HttpStatus.OK);
    }

    @GetMapping(path = "/nodes")
    protected @ResponseBody
    ResponseEntity<APIResponse> getNodes() throws APIException {
        List<NodeInfo> list = IterableUtils.toList(nodeInfoRepository.findAll());
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
        nodeInfoRepository.save(nodeInfo);
        return new ResponseEntity<>(APIResponse.newSimpleResponse(nodeInfo), HttpStatus.CREATED);
    }

    @PostMapping(path = "/addAll")
    protected @ResponseBody
    ResponseEntity<APIResponse> addAllNodes(@RequestBody List<NodeInfo> nodeInfos) throws IOException {
        nodeInfoRepository.saveAll(nodeInfos);
        return new ResponseEntity<>(APIResponse.newSimpleResponse(nodeInfos), HttpStatus.CREATED);
    }

    @PostMapping(path = "/remove")
    protected @ResponseBody
    ResponseEntity<APIResponse> removeNode(@RequestBody NodeInfo nodeInfo) throws IOException {
        nodeInfoRepository.delete(nodeInfo);
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
    ResponseEntity<APIResponse> setNodeUrl(@RequestBody NodeInfo nodeInfo)
        throws APIException {
        gethService.connectToNode(nodeInfo.id);
        // clear cache for contracts so that we don't keep private contracts for the wrong node
        Cache cache = cacheManager.getCache("contracts");
        if (cache != null) {
            cache.clear();
        }
        return new ResponseEntity<>(APIResponse.newSimpleResponse(true), HttpStatus.OK);
    }

    @GetMapping(path = "/reportingUrl")
    protected @ResponseBody
    ResponseEntity<APIResponse> getReportingUrl() throws APIException {
        return new ResponseEntity<>(APIResponse.newSimpleResponse(reportingService.getReportingUrl()),
            HttpStatus.OK);
    }

    @RequestMapping("/peers/clique/proposals")
    public ResponseEntity<APIResponse> getProposals() throws APIException {
        Map<String, Boolean> proposals = nodeService.getProposals();
        return new ResponseEntity<>(APIResponse.newSimpleResponse(proposals), HttpStatus.OK);
    }

    @RequestMapping("/peers/clique/signers")
    public ResponseEntity<APIResponse> getSigners() throws APIException {
        List<String> signers = nodeService.getSigners();
        return new ResponseEntity<>(APIResponse.newSimpleResponse(signers), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "address", required = false, value = "Required. External node address to add", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "istanbulPropose", required = false, value = "what action to propose", dataType = "java.lang.Boolean", paramType = "body")
    })
    @RequestMapping("/peers/cliquePropose")
    public ResponseEntity<APIResponse> cliquePropose(@RequestBody NodePostJsonRequest jsonRequest) throws APIException {
        Boolean response = nodeService.cliquePropose(jsonRequest.getAddress(), jsonRequest.isIstanbulPropose());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(response), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "address", required = false, value = "Required. External node address to discard", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/peers/cliqueDiscard")
    public ResponseEntity<APIResponse> cliqueDiscard(@RequestBody NodePostJsonRequest jsonRequest) throws APIException {
        Boolean response = nodeService.cliqueDiscard(jsonRequest.getAddress());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(response), HttpStatus.OK);
    }

    @RequestMapping("/peers/istanbul/candidates")
    public ResponseEntity<APIResponse> getCandidates() throws APIException {
        Map<String, Boolean> candidates = nodeService.getCandidates();
        return new ResponseEntity<>(APIResponse.newSimpleResponse(candidates), HttpStatus.OK);
    }

    @RequestMapping("/peers/istanbul/validator")
    public ResponseEntity<APIResponse> getValidators() throws APIException {
        List<String> validators = nodeService.getValidators();
        return new ResponseEntity<>(APIResponse.newSimpleResponse(validators), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "address", required = false, value = "Required. External node address to add", dataType = "java.lang.String", paramType = "body"),
        @ApiImplicitParam(name = "istanbulPropose", required = false, value = "what action to propose", dataType = "java.lang.Boolean", paramType = "body")
    })
    @RequestMapping("/peers/istanbulPropose")
    public ResponseEntity<APIResponse> istanbulPropose(@RequestBody NodePostJsonRequest jsonRequest) throws APIException {
        String response = nodeService.propose(jsonRequest.getAddress(), jsonRequest.isIstanbulPropose());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(response), HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "address", required = false, value = "Required. External node address to add", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/peers/istanbulDiscard")
    public ResponseEntity<APIResponse> istanbulDiscard(@RequestBody NodePostJsonRequest jsonRequest) throws APIException {
        String response = nodeService.discard(jsonRequest.getAddress());
        return new ResponseEntity<>(APIResponse.newSimpleResponse(response), HttpStatus.OK);
    }

    @RequestMapping("/peers/istanbul/nodeAddress")
    public ResponseEntity<APIResponse> getNodeAddress() throws APIException {
        String address = nodeService.istanbulGetNodeAddress();
        return new ResponseEntity<>(APIResponse.newSimpleResponse(address), HttpStatus.OK);
    }
}
