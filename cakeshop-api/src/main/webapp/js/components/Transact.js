import React, {useState} from "react";
import Select from "react-select";
import {PrivateFor} from "./PrivateFor";

export const Method = ({method, onSubmit, inlineButton=true}) => {

    const [inputValues, setInputValues] = useState({});

    const submit = (e) => {
        e.stopPropagation();
        onSubmit(inputValues)
    };

    const onInputChange = (inputName, value) => {
        setInputValues({
            ...inputValues,
            [inputName]: value
        });
    };

    return <tr className="method"
               data-method={method.name}>
            <td colSpan="2">
                { method.name &&
                    <span>
                        <label>{method.name}</label>
                        {inlineButton &&
                        <button onClick={submit} className="btn btn-default send">
                            {getButtonLabel(method)}
                        </button>}
                    </span>
                }
            <div className="transact-inputs">
                {method.inputs.map((input) =>
                    <TransactionInput
                            key={method.name + input.name}
                            onChange={onInputChange}
                            input={input}/>
                )}
            </div>
            {!inlineButton &&
            <button onClick={submit} className="btn btn-default deploy">
                {getButtonLabel(method)}
            </button>}
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
                            onSubmit={(inputValues) => this.onMethodCalled(method, inputValues)}
                            />
                ))
            }
            </tbody>
        )
    }

    onMethodCalled = (method, inputValues) => {
        const {activeContract, selectedAccount, privateFor} = this.state;

        this.doMethodCall(activeContract, selectedAccount.value, method, inputValues,
            "", privateFor);

    };

    onAccountSelected = (account) => {
        this.setState({selectedAccount: account});
    };

    onPrivateForChange = (privateFor) => {
        this.setState({privateFor})
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
        var methodArgs = {
          from: from,
          args: _params,
          privateFrom: privateFrom,
          privateFor: privateFor,
        };

        console.log("test", method.name, methodArgs);
        contract.proxy[method.name](methodArgs).then((res) => {
            this.props.onTransactionSubmitted(res, method, methodSig,
                methodArgs)
        });
    };
}

export const TransactionInput = ({input, onChange}) => {

    const isDynamic = isDynamicArray(input);
    const [values, setValues] = useState([""]);
    // const [numInputs, setNumInputs] = useState(1);

    const setValuesAndNotify = (newValues) => {
        onChange(input.name, isDynamic ? newValues : newValues[0]);
        setValues(newValues);
    };

    const onInputChange = (index, value) => {
        const newValues = [...values];
        newValues[index] = value;
        setValuesAndNotify(newValues);
    };

    const onPlus = (index) => {
        const newValues = [...values];
        newValues.splice(index + 1, 0, "");
        setValuesAndNotify(newValues);
    };

    const onMinus = (index) => {
        const newValues = [...values];
        newValues.splice(index, 1);
        setValuesAndNotify(newValues);
    };

    return values.map((value, index) => (
            <div key={`${input.name}${index}`}className="input-group method-inputs" data-param={input.name}>
                <input type="text" className="form-control" data-param={input.name}
                       data-type={input.type}
                       value={value}
                       placeholder={getInputPlaceholder(input)}
                       onChange={(e) => onInputChange(index, e.target.value)}>
                </input>
                {isDynamic &&
                // handle dynamic array input types - like bytes32[]
                ([
                    <a key={"minus"} onClick={() => onMinus(index)}
                       className="remove input-group-addon text-danger">
                        <i className="fa fa-minus"/>
                    </a>,
                    <a key={"plus"} onClick={() => onPlus(index)}
                       className="add input-group-addon text-success">
                        <i className="fa fa-plus"/>
                    </a>
                ])
                }
            </div>
        ))
};

const getButtonLabel = (method) => {
    if(!method.name) {
        return "Deploy";
    } else if(Contract.isReadOnly(method)) {
        return "Read";
    }
    return "Transact";
};

// public field mapping/array getter inputs don't have names, make it 'input'
const getInputPlaceholder =
    (input) => `${input.name || "input"} (${input.type})`;

const isDynamicArray = (input) => input.type.match(/\[(\d+)?\]/);
