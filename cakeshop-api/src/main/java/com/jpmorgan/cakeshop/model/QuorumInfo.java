package com.jpmorgan.cakeshop.model;

public class QuorumInfo {

    public static class BlockMakerStrategy {

        private int minBlockTime;
        private int maxBlockTime;
        private String status;
        private String type;

        public BlockMakerStrategy() {
        }

        public int getMinBlockTime() {
            return minBlockTime;
        }

        public void setMinBlockTime(int minBlockTime) {
            this.minBlockTime = minBlockTime;
        }

        public int getMaxBlockTime() {
            return maxBlockTime;
        }

        public void setMaxBlockTime(int maxBlockTime) {
            this.maxBlockTime = maxBlockTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    private Boolean isQuorum;

    private String blockMakerAccount;

    private Boolean canCreateBlocks;

    private String voteAccount;

    private String nodeKey;

    private Boolean canVote;

    private Boolean isConstellationEnabled;

    private BlockMakerStrategy blockMakerStrategy;

    public QuorumInfo() {
    }

    public String getBlockMakerAccount() {
        return blockMakerAccount;
    }

    public void setBlockMakerAccount(String blockMakerAccount) {
        this.blockMakerAccount = blockMakerAccount;
    }

    public Boolean isCanCreateBlocks() {
        return canCreateBlocks;
    }

    public void setCanCreateBlocks(Boolean canCreateBlocks) {
        this.canCreateBlocks = canCreateBlocks;
    }

    public String getVoteAccount() {
        return voteAccount;
    }

    public void setVoteAccount(String voteAccount) {
        this.voteAccount = voteAccount;
    }

    public Boolean isCanVote() {
        return canVote;
    }

    public void setCanVote(Boolean canVote) {
        this.canVote = canVote;
    }

    public BlockMakerStrategy getBlockMakerStrategy() {
        return blockMakerStrategy;
    }

    public void setBlockMakerStrategy(BlockMakerStrategy blockMakerStrategy) {
        this.blockMakerStrategy = blockMakerStrategy;
    }

    public Boolean isQuorum() {
        return isQuorum;
    }

    public void setQuorum(Boolean isQuorum) {
        this.isQuorum = isQuorum;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public Boolean getIsConstellationEnabled() {
        return isConstellationEnabled;
    }

    public void setIsConstellationEnabled(Boolean isConstellationEnabled) {
        this.isConstellationEnabled = isConstellationEnabled;
    }

}
