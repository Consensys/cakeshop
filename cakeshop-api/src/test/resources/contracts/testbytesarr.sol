pragma solidity ^0.5.4;
contract TestBytesArr {

  bytes32[] data;

  function foo() public returns (bytes32[] memory _ret) {
    data.length = 1;
    data[0] = "foobar";
    return data;
  }
}
