import Paper from '@material-ui/core/Paper'
import Typography from '@material-ui/core/Typography'
import React, { useEffect, useState } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { getBlockDetail } from '../../api/api'
import TableContainer from '@material-ui/core/TableContainer'
import Table from '@material-ui/core/Table'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import TableBody from '@material-ui/core/TableBody'

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

export function BlockDetail ({ number }) {
  const classes = useStyles()
  const [block, setBlock] = useState()

  useEffect(() => {
    getBlockDetail(number)
      .then((block) => setBlock(block))
  }, [number])

  return <Paper className={classes.container}>
    {block && <TableContainer component={Paper}>
    <Typography variant="h6" className={classes.title}>Block #{block.number}</Typography>
      <Table className={classes.table} aria-label="simple table">
        <TableBody>
          {Object.entries(block)
            .filter(([key, value]) => key.indexOf("_") !== 0)
            .map(([key, value]) => (
            <TableRow key={key}>
              <TableCell size="small" component="th" scope="row">{key}</TableCell>
              <TableCell align="left" padding="default" data-value={value}>{value}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
    }
  </Paper>
}
