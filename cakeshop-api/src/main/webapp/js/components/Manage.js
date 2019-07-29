import React, {Component} from 'react';
import {DragAndDropButton} from "./DragAndDropButton";
import {AddNodeDialog} from "./AddNodeDialog";
import {NodeGrid} from "./NodeGrid";
import ArrowBack from "@material-ui/icons/ArrowBack";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import IconButton from "@material-ui/core/IconButton";
import Typography from "@material-ui/core/Typography";
import Container from "@material-ui/core/Container";
import {getNodesFromLocalStorage, saveNodesToLocalStorage} from "./node_utils";

export default class Manage extends Component {
    constructor(props) {
        super(props);

        this.state = {
            addValue: "",
            nodes: [],
            dialogOpen: false
        };
    }

    componentDidMount() {
        this.setState({nodes: getNodesFromLocalStorage()});
    }

    render() {
        const {nodes, dialogOpen} = this.state;
        return (
            <div>
                <AppBar position="fixed" color="inherit">
                    <Toolbar>
                        <IconButton edge="start" aria-label="back"
                                    onClick={() => history.back()}>
                            <ArrowBack/>
                        </IconButton>
                        <Typography variant="h6"
                                    style={{marginLeft: 12, flex: 1}}>
                            Nodes
                        </Typography>
                        <DragAndDropButton
                            onClick={this.onAddNodeClick}
                            onConfigLoaded={this.onConfigLoaded}/>
                    </Toolbar>
                </AppBar>
                <Container style={{marginTop: 84}}>
                    <NodeGrid list={nodes} onView={this.onView}
                              onDismiss={this.onDismiss}/>
                </Container>
                <AddNodeDialog open={dialogOpen}
                               onSubmit={this.onSubmit}
                               onCancel={this.onCancel}/>
            </div>
        )
    }

    onAddNodeClick = (e) => {
        e.stopPropagation();
        this.setState({dialogOpen: true})
    };

    onConfigLoaded = (config) => {
        console.log("Config successfully loaded:", config);
        if (config.nodes != null && config.nodes.length > 0) {
            saveNodesToLocalStorage(config.nodes);
            this.setState({nodes: config.nodes})
        }
    };

    onCancel = () => {
        this.setState({dialogOpen: false})
    };

    onSubmit = ({name, geth, tessera}) => {
        this.setState({dialogOpen: false});
        const newNode = {
            name,
            geth: {
                url: geth
            },
            tessera: {
                url: tessera
            }
        };
        this.setState((prevState) => {
            const {nodes} = prevState;
            let newNodes = [...nodes, newNode];
            saveNodesToLocalStorage(newNodes);
            return {
                ...prevState,
                nodes: newNodes
            }
        })
    };

    onView = (node) => {
        fetch(this.getUrl("api/node/url"), {
            method: "POST",
            mode: 'cors',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(
                {
                    url: node.geth.url,
                    transactionManagerUrl: node.tessera.url
                })
        }).then(response => {
            if (response.status === 200) {
                console.log("Success:", response.status, response.statusText);
                window.location = "/";
            } else {
                console.log("Error:", response.status, response.statusText);
            }
        })
    };

    getUrl = (path) => {
        let {host, protocol} = window.location;
        if (host === "localhost:7999") {
            // when running webpack dev server, point urls to 8080
            host = "localhost:8080";
        }
        return `${protocol}//${host}/${path}`;
    };

    onDismiss = (nodeToRemove) => {
        this.setState((prevState) => {
            const {nodes} = prevState;
            let newNodes = nodes.filter((node) => node !== nodeToRemove);
            saveNodesToLocalStorage(newNodes);
            return {
                ...prevState,
                nodes: newNodes
            }
        })
    }
}
