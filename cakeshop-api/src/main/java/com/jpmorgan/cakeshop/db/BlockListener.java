package com.jpmorgan.cakeshop.db;

import com.jpmorgan.cakeshop.model.BlockWrapper;


public interface BlockListener {

    public void blockCreated(BlockWrapper block);

    public void shutdown();

}
