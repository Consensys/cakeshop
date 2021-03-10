import Paper from '@material-ui/core/Paper'
import Typography from '@material-ui/core/Typography'
import React, { useEffect } from 'react'
import TableContainer from '@material-ui/core/TableContainer'
import Table from '@material-ui/core/Table'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import TableBody from '@material-ui/core/TableBody'
import { makeStyles } from '@material-ui/core/styles'
import { useDispatch, useSelector } from 'react-redux'
import { fetchBlocks, selectBlocks, showRecent } from './blocksSlice'
import { selectNodeInfo } from '../nodeInfo/nodeInfoSlice'
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

export function Blocks () {
  const classes = useStyles()
  const dispatch = useDispatch()
  const { recent, list, nextPage } = useSelector(selectBlocks)
  const { latestBlock = 0 } = useSelector(selectNodeInfo)
  useEffect(() => {
    if (latestBlock !== 0 && list.length === 0) {
      console.log('fetching for the first time', latestBlock, list.length)
      dispatch(fetchBlocks(latestBlock, true))
    }
  }, [latestBlock])

  const clickRecent = (e) => {
    e.preventDefault()
    dispatch(showRecent())
  }

  const clickMore = (e) => {
    e.preventDefault()
    dispatch(fetchBlocks(nextPage))
  }

  const recentText = recent.length > 0 ?
    <Typography variant="overline"><a href="#" onClick={clickRecent}>Show {recent.length} new
      block(s)</a></Typography> :
    ''
  return <Paper className={classes.container}>
    <Typography variant="h6" className={classes.title}>Blocks {recentText}</Typography>
    <TableContainer component={Paper}>
      <Table className={classes.table} aria-label="simple table">
        <TableHead>
          <TableRow>
            <TableCell>#</TableCell>
            <TableCell align="right">Tx Count</TableCell>
            <TableCell align="right">Transactions</TableCell>
            <TableCell align="right">Time</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {list.map((row) => (
            <TableRow key={row.id}>
              <TableCell component="th" scope="row">
                <Link to={`/blocks/${row.number}`}>{row.number}</Link>
              </TableCell>
              <TableCell align="right">{row.transactions.length}</TableCell>
              <TableCell align="right">{row.transactions.map((txn) => {
                return <Link key={txn} to={`/transactions/${txn}`}>{txn.substring(0, 12) + '...'}</Link>
              })}
              </TableCell>
              <TableCell align="right">{row.timestamp}</TableCell>
            </TableRow>
          ))}
          {nextPage !== 0 && <TableRow key="showmore">
            <TableCell component="th" scope="row">
              <a href="#" onClick={clickMore}>Show more</a>
            </TableCell>
          </TableRow>}
        </TableBody>
      </Table>
    </TableContainer>
  </Paper>
}
