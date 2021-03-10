import React from 'react'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import { Link } from 'react-router-dom'
import { getBlocks } from '../../api/api'
import { PaginatedTableView } from '../../components/PaginatedTableView'
import TableHead from '@material-ui/core/TableHead'
import { dateFromTimestamp, relativeDateFromTimestamp } from '../../utils/QuorumUtils'

export function Blocks () {
  return <PaginatedTableView
    title={'Blocks'}
    getItems={getBlocks}
    ItemView={BlockRow}
    HeaderView={Header}
  />
}

const Header = () => {
  return <TableHead>
    <TableRow>
      <TableCell>Number</TableCell>
      <TableCell align="right">Tx Count</TableCell>
      <TableCell align="right">Transaction(s)</TableCell>
      <TableCell align="right">Time</TableCell>
    </TableRow>
  </TableHead>
}

const BlockRow = ({ timestamp, transactions, id, number }) => {
  return <TableRow key={id}>
    <TableCell component="th" scope="row">
      <Link to={`/blocks/${number}`}>{number}</Link>
    </TableCell>
    <TableCell align="right">{transactions.length}</TableCell>
    <TableCell align="right">{transactions.map((txn) => {
      return <Link key={txn} to={`/transactions/${txn}`}>{txn.substring(0, 26)}...</Link>
    })}
    </TableCell>
    <TableCell align="right" alt={dateFromTimestamp(timestamp)}>{relativeDateFromTimestamp(timestamp)}</TableCell>
  </TableRow>
}
