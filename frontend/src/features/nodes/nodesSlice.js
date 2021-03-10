import { createSlice } from '@reduxjs/toolkit'
import { getNodes, setNodeUrl } from '../../api/api'

export const nodesSlice = createSlice({
  name: 'nodes',
  initialState: {
    nodes: [],
    selectedNode: undefined,
  },
  reducers: {
    update: (state, action) => {
      // Redux Toolkit allows us to write "mutating" logic in reducers. It
      // doesn't actually mutate the state because it uses the Immer library,
      // which detects changes to a "draft state" and produces a brand new
      // immutable state based off those changes
      let selectedNode = state.selectedNode
      state.nodes = action.payload.map((node) => {
        const option = {
          label: node.name,
          value: node
        }

        if (node.isSelected) {
          selectedNode = option
        }

        return option
      })

      state.selectedNode = selectedNode
    },
    select: (state, payload) => {
      state.selectedNode = state.nodes.find((node) => node.value.id === payload.id)
    }
  },
})

export const fetchNodes = () => async dispatch => {
  getNodes()
    .then(function (response) {
      dispatch(update(response))
    })
    .catch(e => console.log('error', e))
}

export const selectNode = (node) => async dispatch => {
  setNodeUrl(node)
    .then(function (response) {
      dispatch(select(node))
    })
}

export const { update, select } = nodesSlice.actions

// The function below is called a selector and allows us to select a value from
// the state. Selectors can also be defined inline where they're used instead of
// in the slice file. For example: `useSelector((state) => state.counter.value)`
export const selectNodes = state => state.nodes

export default nodesSlice.reducer
