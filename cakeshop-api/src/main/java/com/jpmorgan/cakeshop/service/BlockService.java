package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.BlockWrapper;

import java.util.List;

public interface BlockService {

    /**
     * Get the block represented by the given identifier (only one is allowed
     * at a time).
     *
     * @param id                Block ID (hash)
     * @param number            Block number
     * @param tag               One of "earliest", "latest" or "pending"
     *
     * @return {@link Block}    Block or null if block does not exist
     *
     * @throws APIException
     */
    public BlockWrapper get(String id, Long number, String tag) throws APIException;

    public List<BlockWrapper> get(long start, long end) throws APIException;

    public List<BlockWrapper> get(List<Long> numbers) throws APIException;

}
