package com.jpmorgan.cakeshop.db;

import com.jpmorgan.cakeshop.model.Block;


public interface BlockListener {

    public void blockCreated(Block block);

    public void shutdown();

}
