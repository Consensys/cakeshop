import axios from 'axios'

export function getNodeInfo () {
  return request('/api/node/get', 'POST')
    .then((data) => {
      return data.data.attributes
    })
}

export function getNodes () {
  return request('/api/node/nodes')
    .then((data) => {
      return data.data.attributes.result
    })
}

export function setNodeUrl (node) {
  return request('/api/node/url', 'POST', node)
    .then((data) => {
      return data.data.attributes
    })
}

export function getBlockDetail (blockNumber) {
  if (typeof blockNumber === 'string' && blockNumber.indexOf('0x') === 0) {
    return request(`/api/blocks/${blockNumber}`)
  }
  return request(`/api/blocks/search/findByNumber?number=${blockNumber}`)
}

export function getTransactionDetail (id) {
  return request(`/api/transactions/${id}`)
}

export const getTransactions = (page, pageSize) => requestPage('transactions', page, pageSize, 'blockNumber,desc')

export const getBlocks = (page, pageSize) => requestPage('blocks', page, pageSize, 'number,desc')

export const getContracts = (page, pageSize) => requestPage('contracts', page, pageSize, 'createdDate,desc')

export function getAddressDetail (id) {
  return request(`/api/node/address/${id}`, 'POST')
    .then((data) => {
      return data.data
    })
}

export function getContractDetail (id) {
  return request(`/api/contracts/${id}`)
}

export function getTransactionsForAddress (address, page, pageSize) {
  return request(`/api/transactions/search/findAllByToOrFrom?to=${address}&from=${address}&page=${page}&size=${pageSize}&sort=blockNumber,desc`)
    .then((data) => {
      return [data._embedded.transactions, data.page]
    })
}

export function requestPage (repo, page, pageSize, sort='id,desc') {
  return request(`/api/${repo}?page=${page}&size=${pageSize}&sort=${sort}`, 'GET')
    .then((data) => {
      return [data._embedded[repo], data.page]
    })
}

export function request (url, method = 'GET', data = '') {
  return axios(url, {
    method,
    data,
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    }
  })
    .then((response) => {
      return response.data
    })
}
