import React, {Component} from "react";
import Creatable from "react-select/creatable";

export class PrivateFor extends Component {

    constructor(props, context) {
        super(props, context);
        console.log("privateFor", props.initialPrivateFor);
        this.state = {privatePeers: [], selection: []}
    }

    render() {
        return <Creatable options={this.state.privatePeers} className="private_for"
                       onChange={this.onChangeInternal}
                       value={this.state.selection}
                       classNamePrefix="private_for" isMulti autosize={false}/>;
    }

    onChangeInternal = (options) => {
        this.setState({selection: options})
        this.props.onChange(options);
    };

    componentDidMount() {
        const {initialPrivateFor} = this.props;
        Client.post('api/node/tm/peers')
            .then((response) => {
                console.log(response);
                const nodes = JSON.parse(localStorage.getItem("nodes") || "[]");
                const parties = response.data.attributes.result.keys
                let privatePeers = [];
                let initialPrivatePeersValue = [];
                nodes.forEach((node) => {
                    if (!node.tessera.url.endsWith("/")) {
                        node.tessera.url += "/"
                    }
                    parties.forEach((party) => {
                        // party only includes the real ip url and key, not the proxy
                        // url or the cakeshop "name" for the node. Need to find the
                        // matching party for the nodes that cakeshop knows about
                        if (node.tessera.url === party.url) {
                            const peer = {
                                value: party.key,
                                label: `${node.name} (${party.key}}`
                            };
                            privatePeers.push(peer)
                            if(initialPrivateFor.indexOf(party.key) >= 0) {
                                console.log("found privateFor key")
                                initialPrivatePeersValue.push(peer);
                            }
                        }
                    });
                });
                // setting initial selection through state won't tigger onChange
                this.props.onChange(initialPrivatePeersValue);
                this.setState({privatePeers, selection: initialPrivatePeersValue})
            })
    }
}
