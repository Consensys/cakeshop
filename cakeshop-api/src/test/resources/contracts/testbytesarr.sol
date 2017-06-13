pragma solidity ^0.4.9;
contract TestBytesArr {

  bytes32[] data;

  function foo() returns (bytes32[] _ret) {
    data.length = 1;
    data[0] = "foobar";
    return data;
  }
}
