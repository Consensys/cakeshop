import React from 'react'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import { useSelector } from 'react-redux'
import { selectNodeInfo } from '../nodeInfo/nodeInfoSlice'
import { Link } from 'react-router-dom'
import { getContracts } from '../../api/api'
import { PaginatedTableView } from '../../components/PaginatedTableView'
import TableHead from '@material-ui/core/TableHead'
import { dateFromTimestamp } from '../../utils/QuorumUtils'

export function Contracts () {
  const { totalContracts = 0 } = useSelector(selectNodeInfo)
  return <PaginatedTableView
    title={'Contracts'}
    count={totalContracts}
    getItems={getContracts}
    ItemView={ContractRow}
    HeaderView={Header}
  />
}

const Header = () => {
  return <TableHead>
    <TableRow>
      <TableCell>Address</TableCell>
      <TableCell align="right">Name</TableCell>
      <TableCell align="right">Created Date</TableCell>
    </TableRow>
  </TableHead>
}

const ContractRow = ({ address, createdDate, contractJson }) => {
  const contract = JSON.parse(contractJson)
  console.log('contract', contract, createdDate)
  return <TableRow key={address}>
    <TableCell component="th" scope="row">
      <Link to={`/addresses/${address}`}>{address}</Link>
    </TableCell>
    <TableCell align="right">{contract.name || "Unknown Contract"}</TableCell>
    <TableCell align="right">{dateFromTimestamp(contract.createdDate)}</TableCell>
  </TableRow>
}
