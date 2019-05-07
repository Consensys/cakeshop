package com.jpmorgan.cakeshop.model;

import java.util.List;
import java.util.Map;

public class SolcResponse {

    public Map<String, Map<String, ContractBundle>> contracts;
    public List<Map<String, Object>> errors;
    public Map<String, Object> sources;

    public static class ContractBundle {

        public ContractABI abi;
        public Evm evm;
    }

    public static class Evm {

        public Bytecode bytecode;
        public GasEstimates gasEstimates;
        public Map<String, String> methodIdentifiers;

    }

    public static class Bytecode {

        public Map<String, String> linkReferences;
        public String object;
        public String opcodes;
        public String sourceMap;
    }

    public static class GasEstimates {

        public Map<String, String> creation;
        public Map<String, String> external;
    }
}
