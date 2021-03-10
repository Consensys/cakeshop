import Paper from '@material-ui/core/Paper'
import { Link } from 'react-router-dom'
import Typography from '@material-ui/core/Typography'
import { NodeInfo } from './features/nodeInfo/NodeInfo'
import NodeChooser from './features/nodes/NodeChooser'
import * as PropTypes from 'prop-types'
import React from 'react'
import { makeStyles } from '@material-ui/core/styles'

const useStyles = makeStyles({
  header: {
    display: 'flex',
    height: 75,
    alignItems: 'center',
    padding: 16,
  },
  home: {
    textDecoration: 'none',
    color: 'inherit',
    marginRight: 16,
  },
  link: {
    textDecoration: 'none',
    color: 'inherit',
    margin: 12,
  },
})

export function Header () {
  const classes = useStyles()
  return <Paper className={classes.header}>
    <Link to="/" className={classes.home}>
      <Typography variant="h4" component='h1'>
        Cakeshop
      </Typography>
    </Link>
    <Link className={classes.link} to="/blocks">
      <Typography>
        Blocks
      </Typography>
    </Link>
    <Link className={classes.link} to="/transactions">
      <Typography>
        Transactions
      </Typography>
    </Link>
    <Link className={classes.link} to="/tokens">
      <Typography>
        Tokens
      </Typography>
    </Link>
    <Link className={classes.link} to="/contracts">
      <Typography>
        Contracts
      </Typography>
    </Link>
    <Link className={classes.link} to="/">
      <Typography>
        More▾
      </Typography>
    </Link>
    <Typography style={{ flex: 1 }}/>
    <NodeInfo/>
    <div style={{ width: 200 }}>
      <NodeChooser/>
    </div>
  </Paper>
}

Header.propTypes = { classes: PropTypes.any }
