pragma solidity ^0.6.4;
contract SimpleStorage {

    uint public storedData;
    address public owner;

    constructor(uint initVal) public {
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
