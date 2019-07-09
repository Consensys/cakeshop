package com.jpmorgan.cakeshop.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Map;

public class Genesis {

    public Map<String, Wallet> alloc;
    public String coinbase;
    public Config config;
    public String difficulty;
    public String extraData;
    public String gasLimit;
    @JsonAlias({"mixHash", "mixhash"})
    public String mixhash;
    public String nonce;
    public String number;
    public String gasUsed;
    public String parentHash;
    public String timestamp;


    public static class Wallet {

        public String balance;
    }

    public static class Config {

        public Integer homesteadBlock;
        public Integer byzantiumBlock;
        public Integer constantinopleBlock;
        public Integer chainId;
        public Integer eip150Block;
        public Integer eip155Block;
        public String eip150Hash;
        public Integer eip158Block;
        public Boolean isQuorum;
        public IstanbulConfig istanbul;
    }

    public static class IstanbulConfig {

        public Integer epoch;
        public Integer policy;
    }
}
