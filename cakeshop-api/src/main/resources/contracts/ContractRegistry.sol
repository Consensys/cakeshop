pragma solidity ^0.4.9;
contract ContractRegistry
{
  address public owner;

  struct Contract {
    address addr;
    address owner;
    string name;
    string abi;
    string code;
    string code_type;
    int created_date;
  }

  // helpers for iterating
  uint public num_addrs;
  address[] public addrs;

  mapping (address => Contract) public contracts; // contracts by address

  function ContractRegistry() {
    owner = msg.sender;
    num_addrs = 0;
  }

  function register(address addr, string name, string abi, string code, string code_type, int created_date) {
    addrs.length = ++num_addrs;
    addrs[num_addrs-1] = addr;
    contracts[addr] = Contract(addr, msg.sender, name, abi, code, code_type, created_date);
  }

  function getById(address id) returns (address _id, string _name, string _abi, string _code, string _code_type, int _created_date) {
    var c = contracts[id];
    _id = c.addr;
    _name = c.name;
    _abi = c.abi;
    _code = c.code;
    _code_type = c.code_type;
    _created_date = c.created_date;
  }

  /*
  function getByName(string name) returns (address _id, string _name, string _abi, string _code, string _code_type) {
    // TODO
  }
  */

  function listAddrs() returns (address[] _addresses) {
    return addrs;
  }
/*
  function listByOwner() {
    // TODO
  }
*/

}
