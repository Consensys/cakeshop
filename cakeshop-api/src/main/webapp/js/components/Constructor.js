import React from "react";
import {PrivateFor} from "./PrivateFor";
import {Method} from "./Transact";

export default class Constructor extends React.Component {

    constructor(props, context) {
        super(props, context);
        this.state = {};
    }

    render() {
        const {method, onDeploy} = this.props;
        return (
            <tbody>
            <Method key={"constructor"}
                    method={method}
                    onInputChange={this.onInputChange}
            />

            <tr className="private_for">
                <td colSpan="2" className="from_address"><label
                    htmlFor="private_for" title="One key per line">Private
                    For</label>
                    <PrivateFor initialPrivateFor={""}
                                onChange={this.onPrivateForChange}/>
                    <button className="btn btn-default deploy"
                            onClick={() => onDeploy(this.state[method.name],
                                this.state.privateFor)}
                    >
                        Deploy
                    </button>
                </td>
            </tr>

            </tbody>
        )
    }

    onInputChange = (methodName, inputName, value) => {
        console.log("constructor input change:", methodName, inputName, value)
        this.setState((prevState, props) => {
            return {
                ...prevState,
                [methodName]: {
                    ...prevState[methodName],
                    [inputName]: value
                }
            }
        })
    }

    onPrivateForChange = (privateFor) => {
        let publicKeys = privateFor.map((option) => option.value);
        console.log("privateFor", publicKeys)
        this.setState({privateFor: publicKeys})
    }
}
