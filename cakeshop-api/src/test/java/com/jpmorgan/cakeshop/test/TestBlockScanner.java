package com.jpmorgan.cakeshop.test;

import com.jpmorgan.cakeshop.db.BlockScanner;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class TestBlockScanner extends BlockScanner {

    @Override
    public void run() {
    }

    public void manualRun() {
        this.backfillBlocks();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public synchronized void start() {
    }

}
