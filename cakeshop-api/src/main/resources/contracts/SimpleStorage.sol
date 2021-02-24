pragma solidity ^0.8.0;
contract SimpleStorage {

    uint public storedData;

    event Change(string message, uint newVal);

    constructor(uint initVal) {
        emit Change("initialized", initVal);
        storedData = initVal;
    }

    function set(uint x) public {
        emit Change("set", x);
        storedData = x;
    }

    function get() view public returns (uint retVal) {
        return storedData;
    }

}
