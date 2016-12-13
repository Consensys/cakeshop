
contract SimpleStorage {

    uint public storedData;
    address public owner;

    function SimpleStorage(uint initVal) {
      owner = msg.sender;
      storedData = initVal;
    }

    function set(uint x) {
        storedData = x;
    }

    function get() constant returns (uint retVal) {
        return storedData;
    }
    
}
