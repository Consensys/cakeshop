pragma solidity ^0.8.0;
contract TestBytesArr {

  bytes32[] data;

  function foo() public returns (bytes32[] memory _ret) {
    data.push("foobar");
    return data;
  }
}
