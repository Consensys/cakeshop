import Paper from '@material-ui/core/Paper'
import Typography from '@material-ui/core/Typography'
import React, { useEffect, useState } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { getAddressDetail, getContractDetail, getTransactionsForAddress } from '../../api/api'
import TableContainer from '@material-ui/core/TableContainer'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableRow from '@material-ui/core/TableRow'
import TableCell from '@material-ui/core/TableCell'
import { Transactions } from '../transactions/Transactions'
import { dateFromTimestamp } from '../../utils/QuorumUtils'
import { formatEther } from '@ethersproject/units'

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

export function AddressDetail ({ id }) {
  const classes = useStyles()
  const [addressDetail, setAddressDetail] = useState(undefined)
  const [contractDetail, setContractDetail] = useState(undefined)
  const [contractCode, setContractCode] = useState(undefined)

  useEffect(() => {
    getAddressDetail(id)
      .then((account) => {
        setAddressDetail(account)
      })
      .catch((error) => {
        console.log('Error', error)
        setAddressDetail(undefined)
      })
    getContractDetail(id)
      .then((contractInfo) => {
        setContractDetail(contractInfo)
        setContractCode(contractInfo.contractJson ? JSON.parse(contractInfo.contractJson) : undefined)
      })
      .catch((error) => {
        console.log('Error', error)
        setContractDetail(undefined)
        setContractCode(undefined)
      })
    getTransactionsForAddress(id)
      .then((transactions) => {
        console.log(transactions)
      })
      .catch((error) => {
        console.log('Error', error)
      })
  }, [id])

  const type = contractDetail ? 'Contract' : 'Address'
  return <div>
    <Paper className={classes.container}>
      <Typography variant="h6" className={classes.title}>{type} {id}</Typography>
      <TableContainer component={Paper}>
        <Table className={classes.table} aria-label="simple table">
          <TableBody>
            {contractDetail && contractCode && [
              <TableRow key={'name'}>
                <TableCell size="small" component="th" scope="row">Contract Name</TableCell>
                <TableCell align="right"
                           data-value={contractCode.name}>{contractCode.name || 'Unknown Contract'}</TableCell>
              </TableRow>,
              <TableRow key={'createdDate'}>
                <TableCell size="small" component="th" scope="row">Created</TableCell>
                <TableCell align="right" data-value={contractCode.createdDate}>
                  {dateFromTimestamp(contractCode.createdDate)}
                </TableCell>
              </TableRow>,
            ]}
            {addressDetail &&
            <TableRow key={'balance'}>
              <TableCell size="small" component="th" scope="row">Balance</TableCell>
              <TableCell align="right"
                         data-value={addressDetail.balance}>{formatEther(addressDetail.balance)} ETH</TableCell>
            </TableRow>
            }
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
    {contractCode &&
    <Paper className={classes.container}>
      <Typography variant="h6" className={classes.title}>Contract Details</Typography>
      {Object.entries(contractCode)
        .filter(([key, value]) => key.indexOf('_') !== 0)
        .map(([key, value]) => (
          <TableRow key={key}>
            <TableCell size="small" component="th" scope="row">{key}</TableCell>
            <TableCell align="left" padding="default" data-value={value}>{value}</TableCell>
          </TableRow>
        ))}
    </Paper>
    }
    <Paper className={classes.container}>
      <Transactions
        title={'Transactions'}
        getItems={(page, pageSize) => getTransactionsForAddress(id, page, pageSize)}
        startingRowsPerPage={5}
      />

    </Paper>
  </div>
}
