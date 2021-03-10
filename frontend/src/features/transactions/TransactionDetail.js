import Paper from '@material-ui/core/Paper'
import Typography from '@material-ui/core/Typography'
import React, { useEffect, useState } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { getTransactionDetail } from '../../api/api'
import TableContainer from '@material-ui/core/TableContainer'
import Table from '@material-ui/core/Table'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import TableBody from '@material-ui/core/TableBody'
import { Link } from 'react-router-dom'

const useStyles = makeStyles({
  container: {
    marginTop: 24,
  },
  title: {
    padding: 12,
  },
  table: {
    minWidth: 650,
  },
})

export function TransactionDetail ({ id }) {
  const classes = useStyles()
  const [transaction, setTransaction] = useState()

  useEffect(() => {
    getTransactionDetail(id)
      .then((transaction) => setTransaction(transaction))
  }, [id])

  return <Paper className={classes.container}>
    <Typography variant="h6" className={classes.title}>Transaction {id}</Typography>
    {transaction && <TableContainer component={Paper}>
      <Table className={classes.table} aria-label="simple table">
        <TableBody>
          <TableRow key={'from'}>
            <TableCell size="small" component="th" scope="row">from</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.from}>
              <Link to={`/addresses/${transaction.from}`}>{transaction.from}</Link>
            </TableCell>
          </TableRow>
          <TableRow key={'to'}>
            <TableCell size="small" component="th" scope="row">to</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.to}>
              <Link to={`/addresses/${transaction.to}`}>{transaction.to}</Link>
            </TableCell>
          </TableRow>
          <TableRow key={'value'}>
            <TableCell size="small" component="th" scope="row">value</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.value}>{transaction.value}</TableCell>
          </TableRow>
          <TableRow key={'gas'}>
            <TableCell size="small" component="th" scope="row">gas</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.gas}>{transaction.gas}</TableCell>
          </TableRow>
          <TableRow key={'gasPrice'}>
            <TableCell size="small" component="th" scope="row">gasPrice</TableCell>
            <TableCell align="left" padding="default"
                       data-value={transaction.gasPrice}>{transaction.gasPrice}</TableCell>
          </TableRow>
          <TableRow key={'input'}>
            <TableCell size="small" component="th" scope="row">input</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.input}>{transaction.input}</TableCell>
          </TableRow>
          <TableRow key={'blockNumber'}>
            <TableCell size="small" component="th" scope="row">blockNumber</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.blockNumber}>
              <Link to={`/blocks/${transaction.blockNumber}`}>{transaction.blockNumber}</Link>
            </TableCell>
          </TableRow>
          <TableRow key={'blockId'}>
            <TableCell size="small" component="th" scope="row">blockId</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.blockId}>
              <Link to={`/blocks/${transaction.blockId}`}>{transaction.blockId}</Link>
            </TableCell>
          </TableRow>
          <TableRow key={'status'}>
            <TableCell size="small" component="th" scope="row">status</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.status}>{transaction.status}</TableCell>
          </TableRow>
          <TableRow key={'returnCode'}>
            <TableCell size="small" component="th" scope="row">returnCode</TableCell>
            <TableCell align="left" padding="default"
                       data-value={transaction.returnCode}>{transaction.returnCode}</TableCell>
          </TableRow>
          <TableRow key={'nonce'}>
            <TableCell size="small" component="th" scope="row">nonce</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.nonce}>{transaction.nonce}</TableCell>
          </TableRow>
          <TableRow key={'transactionIndex'}>
            <TableCell size="small" component="th" scope="row">transactionIndex</TableCell>
            <TableCell align="left" padding="default"
                       data-value={transaction.transactionIndex}>{transaction.transactionIndex}</TableCell>
          </TableRow>
          <TableRow key={'decodedInput'}>
            <TableCell size="small" component="th" scope="row">decodedInput</TableCell>
            <TableCell align="left" padding="default"
                       data-value={transaction.decodedInput}>{transaction.decodedInput}</TableCell>
          </TableRow>
          <TableRow key={'cumulativeGasUsed'}>
            <TableCell size="small" component="th" scope="row">cumulativeGasUsed</TableCell>
            <TableCell align="left" padding="default"
                       data-value={transaction.cumulativeGasUsed}>{transaction.cumulativeGasUsed}</TableCell>
          </TableRow>
          <TableRow key={'gasUsed'}>
            <TableCell size="small" component="th" scope="row">gasUsed</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.gasUsed}>{transaction.gasUsed}</TableCell>
          </TableRow>
          <TableRow key={'contractAddress'}>
            <TableCell size="small" component="th" scope="row">contractAddress</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.contractAddress}>
              <Link to={`/addresses/${transaction.contractAddress}`}>{transaction.contractAddress}</Link>
            </TableCell>
          </TableRow>
          <TableRow key={'logs'}>
            <TableCell size="small" component="th" scope="row">logs</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.logs.toString()}>
              {transaction.logs.map((event) => event.toString()).join(', ')}
            </TableCell>
          </TableRow>
          <TableRow key={'r'}>
            <TableCell size="small" component="th" scope="row">r</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.r}>{transaction.r}</TableCell>
          </TableRow>
          <TableRow key={'s'}>
            <TableCell size="small" component="th" scope="row">s</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.s}>{transaction.s}</TableCell>
          </TableRow>
          <TableRow key={'v'}>
            <TableCell size="small" component="th" scope="row">v</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.v}>{transaction.v}</TableCell>
          </TableRow>
          <TableRow key={'private'}>
            <TableCell size="small" component="th" scope="row">private</TableCell>
            <TableCell align="left" padding="default" data-value={transaction.private}>{transaction.private}</TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </TableContainer>
    }
  </Paper>
}
