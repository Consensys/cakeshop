import React from 'react'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import { useSelector } from 'react-redux'
import { selectNodeInfo } from '../nodeInfo/nodeInfoSlice'
import { Link } from 'react-router-dom'
import { getTransactions } from '../../api/api'
import { PaginatedTableView } from '../../components/PaginatedTableView'
import TableHead from '@material-ui/core/TableHead'
import { formatEther } from 'ethers/lib/utils'

export function Transactions ({ getItems = getTransactions, startingRowsPerPage = 10 }) {
  return <PaginatedTableView
    title={'Transactions'}
    getItems={getItems}
    ItemView={TransactionRow}
    HeaderView={Header}
    startingRowsPerPage={startingRowsPerPage}
  />
}

const Header = () => {
  return <TableHead>
    <TableRow>
      <TableCell>ID</TableCell>
      <TableCell align="right">From</TableCell>
      <TableCell align="right">To</TableCell>
      <TableCell align="right">Value</TableCell>
      <TableCell align="right">Block</TableCell>
    </TableRow>
  </TableHead>
}

export const TransactionRow = ({ id, from, to, value, blockNumber, contractAddress }) => {
  const toAddress = to || contractAddress
  return <TableRow key={id}>
    <TableCell component="th" scope="row">
      <Link to={`/transactions/${id}`}>{id.substring(0, 14)}...</Link>
    </TableCell>
    <TableCell align="right">
      <Link to={`/addresses/${from}`}>{from.substring(0, 14)}...</Link>
    </TableCell>
    <TableCell align="right">
      <Link to={`/addresses/${toAddress}`}>{to ? to.substring(0, 14) + '...' : 'Contract Creation'}</Link>
    </TableCell>
    <TableCell align="right">{formatEther(value)}</TableCell>
    <TableCell align="right">
      <Link to={`/blocks/${blockNumber}`}>{blockNumber}</Link>
    </TableCell>
  </TableRow>
}
