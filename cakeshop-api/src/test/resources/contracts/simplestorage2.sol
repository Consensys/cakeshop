pragma solidity ^0.8.0;
contract SimpleStorage {

    uint public storedData;
    address public owner;

    constructor(uint initVal) {
      owner = msg.sender;
      storedData = initVal;
    }

    function set(uint x) public {
        storedData = x;
    }

    function get() view public returns (uint retVal) {
        return storedData;
    }

}
