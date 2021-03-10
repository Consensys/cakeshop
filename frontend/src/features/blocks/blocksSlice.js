import { createSlice } from '@reduxjs/toolkit'
import { getBlocks } from '../../api/api'

export const blocksSlice = createSlice({
  name: 'blocks',
  initialState: {
    recent: [],
    list: [],
    nextPage: 0,
  },
  reducers: {
    addRecentBlock: (state, action) => {
      state.recent.unshift(action.payload)
    },
    setBlockList: (state, action) => {
      state.list = action.payload
    },
    appendBlocks: (state, action) => {
      state.list.push(...action.payload)
    },
    showRecent:  (state, action) => {
      state.list = [...state.recent, ...state.list]
      state.recent = []
    },
    setNextPage:  (state, action) => {
      state.nextPage = action.payload
    },
  },
})

export const { addRecentBlock, appendBlocks, setBlockList, showRecent, setNextPage } = blocksSlice.actions

export const fetchBlocks = (startingBlock, resetList=false, pageSize=20) => dispatch => {
  const pageEnd = Math.max(startingBlock - pageSize, 0)
  getBlocks(pageEnd, startingBlock)
    .then((responses) => {
      if(resetList) {
        dispatch(setBlockList(responses))
      } else {
        dispatch(appendBlocks(responses))
      }
      dispatch(setNextPage(pageEnd))
    })
}

// The function below is called a selector and allows us to select a value from
// the state. Selectors can also be defined inline where they're used instead of
// in the slice file. For example: `useSelector((state) => state.counter.value)`
export const selectBlocks = state => state.blocks

export default blocksSlice.reducer
