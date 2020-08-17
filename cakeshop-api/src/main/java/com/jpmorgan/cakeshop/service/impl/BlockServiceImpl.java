package com.jpmorgan.cakeshop.service.impl;


import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Web3DefaultResponseType;
import com.jpmorgan.cakeshop.model.Block;
import com.jpmorgan.cakeshop.service.BlockService;
import com.jpmorgan.cakeshop.service.GethHttpService;

import org.web3j.protocol.core.BatchRequest;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlockServiceImpl implements BlockService {

    @Autowired
    private GethHttpService gethService;

    @Override
    public Block get(String id, Long number, String tag) throws APIException {
    	org.web3j.protocol.core.methods.response.EthBlock.Block b = null;
        try {
        	if (id != null && !id.isEmpty()) {
        		b = gethService.getQuorumService().ethGetBlockByHash(id, false).send().getBlock();
        	} else if (number != null && number >= 0) {
        		b = gethService.getQuorumService().ethGetBlockByNumber(new DefaultBlockParameterNumber(number), false).send().getBlock();
            } else if (tag != null && !tag.isEmpty()) {
            	b = gethService.getQuorumService().ethGetBlockByNumber(DefaultBlockParameter.valueOf(tag), false).send().getBlock();
            }
        } catch (IOException e) {
        	throw new APIException(e.getMessage());
        }
        return processBlock(b);
    }
    
    private Block processBlock(org.web3j.protocol.core.methods.response.EthBlock.Block b) {
    	Block block = new Block();
    	block.setId(b.getHash());
    	block.setParentId(b.getParentHash());
    	block.setNonce(b.getNonceRaw());
    	block.setSha3Uncles(b.getSha3Uncles());
    	block.setLogsBloom(b.getLogsBloom());
    	block.setTransactionsRoot(b.getTransactionsRoot());
    	block.setStateRoot(b.getStateRoot());
    	block.setMiner(b.getMiner());
    	block.setExtraData(b.getExtraData());
    	block.setTransactions(processTransactions(b.getTransactions()));
    	block.setUncles(b.getUncles());
    	block.setNumber(b.getNumber());
    	block.setDifficulty(b.getDifficulty());
    	block.setTotalDifficulty(b.getTotalDifficulty());
    	block.setGasLimit(b.getGasLimit());
    	block.setGasUsed(b.getGasUsed());
    	block.setTimestamp(b.getTimestamp());
    	
    	return block;
    }
    
    private List<String> processTransactions(List<TransactionResult> tr) {
    	List<String> txs = new ArrayList<>();
    	for (TransactionResult t : tr) {
    		txs.add((String) t.get()); 
    	}
    	return txs;
    }

    @Override
    public List<Block> get(long start, long end) throws APIException {
        List<Request<?, EthBlock>> reqs = new ArrayList<>();
        for (long i = start; i <= end; i++) {
            reqs.add(gethService.createHttpRequestType("eth_getBlockByNumber", EthBlock.class, new Object[]{"0x" + Long.toHexString(i), false}));
        }
        return batchGet(reqs);
    }

    @Override
    public List<Block> get(List<Long> numbers) throws APIException {
        List<Request<?, EthBlock>> reqs = new ArrayList<>();
        for (Long num : numbers) {
        	reqs.add(gethService.createHttpRequestType("eth_getBlockByNumber", EthBlock.class, new Object[]{"0x" + Long.toHexString(num), false}));          		
        }
        return batchGet(reqs);
    }

    private List<Block> batchGet(List<Request<?, EthBlock>> reqs) throws APIException {
    	BatchRequest batch = null;
    	for (Request<?, EthBlock> req : reqs) {
    		batch = gethService.getQuorumService().newBatch().add(req);
    	}
    	List<? extends Response<?>> res; 
    	try {
    		res = batch.send().getResponses();
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}

        // TODO ignore return order for now
        List<Block> blocks = new ArrayList<>();
        for (Response<?> blockData : res) {
            blocks.add(processBlock((org.web3j.protocol.core.methods.response.EthBlock.Block)blockData.getResult()));
        }
        return blocks;
    }

}
