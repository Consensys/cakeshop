import React, {Component} from "react";
import Select from "react-select";
import {getNodesFromLocalStorage} from "./node_utils";

const MANAGE_NODES_OPTION = {label: "Manage Nodes...", value: "manage"};

export default class NodeChooser extends Component {

    constructor(props, context) {
        super(props, context);
        this.state = {nodes: [], selectedNode: MANAGE_NODES_OPTION}
    }

    render() {
        const {nodes, selectedNode} = this.state;

        const nodesWithManageOption = [
            ...nodes,
            MANAGE_NODES_OPTION
        ];
        return <Select options={nodesWithManageOption}
                       onChange={this.onChange}
                       value={selectedNode}
                       placeholder={"Select..."}
        />;
    }

    componentDidMount() {
        const _this = this;
        Client.get('api/node/currentUrl')
        .done(function (response) {
            _this.setState((prevState) => {
                let currentUrl = response.data.attributes.result;
                let selectedNode = prevState.selectedNode;
                let nodes = getNodesFromLocalStorage().map((node) => {
                    const option = {
                        label: node.name,
                        value: node.geth.url
                    };

                    if (option.value === currentUrl) {
                        selectedNode = option;
                    }

                    return option;
                });

                if (selectedNode !== MANAGE_NODES_OPTION) {
                    _this.setFocusChangeListener();
                }

                return {...prevState, nodes, selectedNode}
            })
        })
    }

    setFocusChangeListener() {
        let _this = this;
        $(window).on("focus", function () {
            Client.get('api/node/currentUrl')
            .done(function (response) {
                let currentUrl = response.data.attributes.result;
                if (currentUrl !== _this.state.selectedNode.value) {
                    console.log("Current url doesn't match dropdown, reloading",
                        currentUrl, _this.state.selectedNode.value);
                    window.location.reload();
                }
            })
        })
    }

    getNodeForUrl = (url) => getNodesFromLocalStorage().filter(
        (node) => node.geth.url === url)[0];

    onChange = (option, action, other) => {
        if (option.value === "manage") {
            window.location = "/manage.html";
            return;
        }

        let node = this.getNodeForUrl(option.value);
        Client.post('api/node/url',
            {url: node.geth.url, transactionManagerUrl: node.tessera.url})
        .done(function (response) {
            console.log("success", response);
            window.location.reload();
        })
        .fail(function (response) {
            console.log("fail", response);
        });
    }
}
