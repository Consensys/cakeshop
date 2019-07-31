pragma solidity ^0.5.4;
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

  constructor() public {
    owner = msg.sender;
    num_addrs = 0;
  }

  function register(address addr, string memory name, string memory abi, string memory code, string memory code_type, int created_date) public {
    addrs.length = ++num_addrs;
    addrs[num_addrs-1] = addr;
    contracts[addr] = Contract(addr, msg.sender, name, abi, code, code_type, created_date);
  }

  function getById(address id) public view returns (address _id, string memory _name, string memory _abi, string memory _code, string memory _code_type, int _created_date) {
    ContractRegistry.Contract storage c = contracts[id];
    _id = c.addr;
    _name = c.name;
    _abi = c.abi;
    _code = c.code;
    _code_type = c.code_type;
    _created_date = c.created_date;
  }

  /*
  function getByName(string memory name) public returns (address _id, string memory _name, string memory _abi, string memory _code, string memory _code_type) {
    // TODO
  }
  */

  function listAddrs() public view returns (address[] memory _addresses) {
    return addrs;
  }
/*
  function listByOwner() public {
    // TODO
  }
*/

}
