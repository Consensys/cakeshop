pragma solidity ^0.8.0;
contract SimpleStorage {
	event Debug(string msg, uint val);
    uint storedData;
    constructor(uint _storedData) {
    	emit Debug("init storage", _storedData);
        storedData = _storedData;
    }
    function set(uint x) public {
    	emit Debug("change val", x);
        storedData = x;
    }
    function get() view public returns (uint retVal) {
        return storedData;
    }
    function echo_2(address foo, string memory bar) public returns(address _foo, string memory _bar) {
        _foo = foo;
        _bar = bar;
    }
    function echo_contract(address id, string memory name, string memory abi, string memory code, string memory code_type)
            public returns (address _id, string memory _name, string memory _abi, string memory _code, string memory _code_type) {

        _id = id;
        _name = name;
        _abi = abi;
        _code = code;
        _code_type = code_type;

    }
}
