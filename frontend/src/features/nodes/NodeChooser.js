import React, { useEffect } from 'react'
import Select from 'react-select'
import { useDispatch, useSelector } from 'react-redux'
import { fetchNodes, selectNode, selectNodes } from './nodesSlice'

const MANAGE_NODES_OPTION = { label: 'Manage Nodes...', value: 'manage' }

const NodeChooser = (props) => {

  const { nodes, selectedNode } = useSelector(selectNodes)
  const dispatch = useDispatch()

  const onChange = (option, action, other) => {
    if (option.value === 'manage') {
      window.location = '/manage.html'
      return
    }
    let node = option.value
    dispatch(selectNode(node))
  }

  useEffect(() => {
    dispatch(fetchNodes())
  }, [])

  const nodesWithManageOption = [
    ...nodes,
    MANAGE_NODES_OPTION
  ]
  return <Select options={nodesWithManageOption}
                 onChange={onChange}
                 value={selectedNode}
                 placeholder={'Select...'}
  />
}

export default NodeChooser
