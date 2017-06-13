pragma solidity ^0.4.9;
contract SimpleStorage {
	event Debug(string msg, uint val);
    uint storedData;
    function SimpleStorage() {
    	Debug("init storage", 100);
        storedData = 100;
    }
    function set(uint x) {
    	Debug("change val", x);
        storedData = x;
    }
    function get() constant returns (uint retVal) {
        return storedData;
    }
    function echo_2(address foo, string bar) returns(address _foo, string _bar) {
        _foo = foo;
        _bar = bar;
    }
    function echo_contract(address id, string name, string abi, string code, string code_type)
            returns (address _id, string _name, string _abi, string _code, string _code_type) {

        _id = id;
        _name = name;
        _abi = abi;
        _code = code;
        _code_type = code_type;

    }
}
