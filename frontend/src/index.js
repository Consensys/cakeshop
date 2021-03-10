import React from 'react'
import ReactDOM from 'react-dom'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import 'fontsource-roboto'
import './index.css'
import App from './App'
import store from './app/store'
import { Provider } from 'react-redux'
import * as serviceWorker from './serviceWorker'
import CssBaseline from '@material-ui/core/CssBaseline'
import { update } from './features/nodeInfo/nodeInfoSlice'
import { addRecentBlock } from './features/blocks/blocksSlice'

function render (TheApp) {
  ReactDOM.render(
    <React.Fragment>
      <CssBaseline/>
      <Provider store={store}>
        <TheApp/>
      </Provider>
    </React.Fragment>,
    document.getElementById('root')
  )
}

if (process.env.NODE_ENV === 'development' && module.hot) {
  module.hot.accept('./App', () => {
    const NextApp = require('./App').default
    render(NextApp)
  })
}
render(App)

const url = new URL(window.location.href)
url.pathname = '/ws'
// TODO this is bad, ws proxying in dev doesn't seem to be working right
if(url.hostname === 'localhost') {
  url.port = 8080
}
const client = new Client({
  brokerURL: url.toString(),
  webSocketFactory: function () {
    // Note that the URL is different from the WebSocket URL
    return new SockJS(url.toString());
  },
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000
})

client.onConnect = function (frame) {
  // Do something, all subscribes must be done is this callback
  // This is needed because this will be executed after a (re)connect
  console.log('connected', frame, client)
  // const CONTRACT_TOPIC = "/topic/contract";
  const NODE_TOPIC = "/topic/node/status";
  const BLOCK_TOPIC = "/topic/block";
  // const PENDING_TRANSACTIONS_TOPIC = "/topic/pending/transactions";
  // const TRANSACTION_TOPIC = "/topic/transaction/";
  client.subscribe(NODE_TOPIC, function(res) {
    const body = JSON.parse(res.body)
    if(body.data.attributes) {
      store.dispatch(update(body.data.attributes))
    } else {
      console.log('no attributes', body.data)
    }
  });
  client.subscribe(BLOCK_TOPIC, (res) => {
    console.log('block', res.body)
    store.dispatch(addRecentBlock(JSON.parse(res.body).data.attributes))
  })
}

client.onDisconnect = function (frame) {
  console.log('Disconnected', frame, client)
}

client.onStompError = function (frame) {
  // Will be invoked in case of error encountered at Broker
  // Bad login/passcode typically will cause an error
  // Complaint brokers will set `message` header with a brief message. Body may contain details.
  // Compliant brokers will terminate the connection after any error
  console.log('Broker reported error: ' + frame.headers['message'])
  console.log('Additional details: ' + frame.body)
}

client.activate()

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister()
