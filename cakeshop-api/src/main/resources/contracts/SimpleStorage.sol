
contract SimpleStorage {

    uint public storedData;

    event Change(string message, uint newVal);

    function SimpleStorage(uint initVal) {
        Change("initialized", initVal);
        storedData = initVal;
    }

    function set(uint x) {
        Change("set", x);
        storedData = x;
    }

    function get() constant returns (uint retVal) {
        return storedData;
    }

}
