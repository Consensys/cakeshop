import React from 'react'
import './App.css'
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom'
import Container from '@material-ui/core/Container'
import { Header } from './Header'
import { Blocks } from './features/blocks/Blocks'
import { BlockDetail } from './features/blocks/BlockDetail'
import { TransactionDetail } from './features/transactions/TransactionDetail'
import { Transactions } from './features/transactions/Transactions'
import { Contracts } from './features/contracts/Contracts'
import { AddressDetail } from './features/addresses/AddressDetail'

function App () {
  return (
    <Router>
      <div>
        <Header />
        <Container maxWidth="lg">
          {/* A <Switch> looks through its children <Route>s and
              renders the first one that matches the current URL. */}
          <Switch>
            <Route exact path="/blocks">
              <Blocks/>
            </Route>
            <Route path="/blocks/:id" render={({ match }) => <BlockDetail number={match.params.id}/> } />
            <Route exact path="/transactions">
              <Transactions/>
            </Route>
            <Route path="/transactions/:id" render={({ match }) => <TransactionDetail id={match.params.id}/> } />
            <Route exact path="/contracts">
              <Contracts/>
            </Route>
            <Route exact path="/tokens">
              <Todo/>
            </Route>
            <Route path="/addresses/:id" render={({ match }) => <AddressDetail id={match.params.id}/> } />
            <Route path="/">
              <Home/>
            </Route>
          </Switch>
        </Container>
      </div>
    </Router>
  )
}

function Home () {
  return <Blocks />
}

function Todo () {
  return <h2>TODO</h2>
}

export default App
