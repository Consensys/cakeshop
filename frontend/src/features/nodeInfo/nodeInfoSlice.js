import { createSlice } from '@reduxjs/toolkit'
import { getNodeInfo } from '../../api/api'

export const nodeInfoSlice = createSlice({
  name: 'nodeInfo',
  initialState: {
  },
  reducers: {
    update: (state, action) => {
      return action.payload
    },
  },
})

export const fetchNodeInfo = () => async dispatch => {
  getNodeInfo()
    .then(function (response) {
      dispatch(update(response))
    })
    .catch(e => console.log('error', e))
}

export const { update } = nodeInfoSlice.actions

// The function below is called a selector and allows us to select a value from
// the state. Selectors can also be defined inline where they're used instead of
// in the slice file. For example: `useSelector((state) => state.counter.value)`
export const selectNodeInfo = state => state.nodeInfo

export default nodeInfoSlice.reducer
