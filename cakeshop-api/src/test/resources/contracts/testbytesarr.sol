pragma solidity ^0.6.4;
contract TestBytesArr {

  bytes32[] data;

  function foo() public returns (bytes32[] memory _ret) {
    data.push("foobar");
    return data;
  }
}
