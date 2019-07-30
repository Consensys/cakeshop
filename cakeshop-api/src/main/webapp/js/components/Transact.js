import React from "react";
import Select from "react-select";
import {PrivateFor} from "./PrivateFor";

export const Method = ({method, onClick, onInputChange}) => {
    return <tr className="method"
               data-method={method.name}>
            <td colSpan="2">
                { method.name &&
                    <span>
                        <label>{method.name}</label>
                        <button onClick={onClick}
                            className="btn btn-default send">
                            {method.constant === true ? "Read" : "Transact"}
                        </button>
                    </span>
                }
            <div className="transact-inputs">
                {method.inputs.map((input) =>
                    <TransactionInput
                            key={method.name + input.name}
                            onChange={onInputChange}
                            methodName={method.name}
                            input={input}/>
                )

                }
            </div>
        </td>
    </tr>;
};


export class TransactTable extends React.Component {

    constructor(props, context) {
        super(props, context);
        const accounts = props.accounts.map((account) => ({
            label: account.address,
            value: account.address
        }));
        this.state = {
            accounts,
            selectedAccount: accounts[0],
            activeContract: props.activeContract,
        }
    }

    render() {
        const {accounts, selectedAccount, activeContract} = this.state;
        console.log("active", activeContract.abi, activeContract.attributes.privateFor)
        return (
            <tbody>
            <tr className="from_address">
                <td colSpan="2" className="from_address"><label>FROM
                    ADDRESS</label>
                    <Select options={accounts} className="accounts"
                            onChange={this.onAccountSelected}
                            value={selectedAccount}/>
                </td>
            </tr>
            <tr className="private_for">
                <td colSpan="2" className="from_address"><label
                    htmlFor="private_for" title="One key per line">Private
                    For</label>
                    <PrivateFor initialPrivateFor={activeContract.attributes.privateFor || ""} onChange={this.onPrivateForChange}/>
                </td>
            </tr>

            {
                activeContract.abi.filter(
                    (method) => method.type === "function")
                .sort((a, b) => a.name.localeCompare(b.name))
                .map((method) => (
                    <Method key={method.name}
                            method={method}
                            onInputChange={this.onInputChange}
                            onClick={() => this.onMethodCalled(method)}
                            />
                ))
            }
            </tbody>
        )
    }

    onInputChange = (methodName, inputName, value) => {
        console.log("input change:", methodName, inputName, value)
        this.setState((prevState, props) => {
            return {
                ...prevState,
                [methodName]: {
                    ...prevState[methodName],
                    [inputName]: value
                }
            }
        })
    };

    onMethodCalled = (method) => {
        console.log("method", method)
        const {activeContract, selectedAccount, privateFor} = this.state;

        // highlightMethod(method);

        const params = this.state[method.name] || {};
        console.log("params:", params, "account:", selectedAccount.value, "privateFor:", privateFor)
        this.doMethodCall(activeContract, selectedAccount.value, method, params,
            "", privateFor);

    };

    onAccountSelected = (account) => {
        console.log("selected", account)
        this.setState({selectedAccount: account});
    };

    onPrivateForChange = (privateFor) => {
        let publicKeys = privateFor.map((option) => option.value);
        console.log("privateFor", publicKeys)
        this.setState({privateFor: publicKeys})
    };

    doMethodCall = (contract, from, method, params, privateFrom,
        privateFor) => {
        var _params = _.map(params, function (v, k) {
            return v;
        });
        var _sig_params = _.map(params, function (v, k) {
            return JSON.stringify(v);
        }).join(", ");
        var methodSig = method.name + "(" + _sig_params + ")";
        var methodArgs = {from: from, args: _params};

        if (!method.constant) {
            // txn
            methodArgs.privateFrom = privateFrom;
            methodArgs.privateFor = privateFor;
        }

        contract.proxy[method.name](methodArgs).then((res) => {
            this.props.onTransactionSubmitted(res, method, methodSig,
                methodArgs)
        });
    };
}

export const TransactionInput = ({methodName, input, onChange}) => {
    // public field mapping/array getter inputs don't have names, make it 'input' or the jquery selectors break
    input.name = input.name || "input";
    return (
        <div className="input-group method-inputs" data-param={input.name}>
            <input type="text" className="form-control" data-param={input.name}
                   data-type={input.type}
                   placeholder={input.name + '(' + input.type + ')'}
                   onChange={(e) => onChange(methodName, input.name,
                       e.target.value)}>
            </input>
            {(input.type.match(/\[(\d+)?\]/)) &&
            // handle dynamic array input types - like bytes32[]
            ([
                <span key={"minus"} className="input-group-addon"><a
                    className="remove text-danger disabled"><i
                    className="fa fa-minus"/></a></span>,
                <span  key={"plus"} className="input-group-addon"><a
                    className="add text-success"><i className="fa fa-plus"/></a></span>
            ])
            }
        </div>
    )
};
