import { useSelector } from 'react-redux'
import { selectNodeInfo } from './nodeInfoSlice'
import { Group, Lens, SyncAlt, Widgets } from '@material-ui/icons'
import React from 'react'
import Typography from '@material-ui/core/Typography'
import { makeStyles } from '@material-ui/core/styles'

const useStyles = makeStyles({
  iconPair: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    margin: 8,
  }
})

export function NodeInfo () {
  const { peerCount, latestBlock, pendingTxn } = useSelector(selectNodeInfo)

  return <div style={{ display: 'flex', alignItems: 'center' }}>
    <IconPair Icon={Group} value={peerCount} alt="Nodes"/>
    <IconPair Icon={Widgets} value={latestBlock} alt="Blocks"/>
    <IconPair Icon={SyncAlt} value={pendingTxn} alt="Pending Transactions"/>
    <Lens style={{ fontSize: 16, color: 'green', margin: 6 }}/>
  </div>
}

function IconPair ({Icon, value, alt}) {
  const classes = useStyles()
  return <div className={classes.iconPair}>
    <Icon size="small" color="inherit" titleAccess={alt} style={{ marginRight: 4}}/>
    <Typography>{value}</Typography>
  </div>
}

