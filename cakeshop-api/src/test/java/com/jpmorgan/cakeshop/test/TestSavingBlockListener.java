package com.jpmorgan.cakeshop.test;

import com.jpmorgan.cakeshop.db.SavingBlockListener;
import com.jpmorgan.cakeshop.model.Block;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class TestSavingBlockListener extends SavingBlockListener {

    public TestSavingBlockListener() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void blockCreated(Block block) {
        saveBlock(block);
    }

}
