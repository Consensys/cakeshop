import utils from "../utils";
import React, {useEffect, useRef, useState} from "react";

export const PaperTape = ({transactions, contractName}) => {

    const [txns, setTxns] = useState(transactions);

    const messagesEndRef = useRef(null)

    useEffect(() => {
        messagesEndRef.current.scrollIntoView({behavior: "smooth"})
    }, [txns]);

    useEffect(() => {
        // TODO this is gross but there's no way to unsubscribe from Dashboard
        // events without unsubscribing every other widget. So do it once in the
        // widget, but update this function so it will always use the current txns
        window.updatePapertapeState = (txn) => {
            setTxns([...txns, txn])
        };
    });

    return (
        <table style={{
            width: "100%",
            tableLayout: "fixed",
            backgroundColor: "#fcf8e3"
        }} className="table">
            <tbody>
            {txns.map((txn) => txn.attributes)
            .map((txn) => {
                return <PaperTapeRow key={txn.id} txn={txn}
                                     contractName={contractName}/>
            })}
            <tr ref={messagesEndRef}/>
            </tbody>
        </table>
    )

};

const PaperTapeRow = ({txn, contractName}) => {

    function formatContractAddress(txn) {
        return <span> Contract <a href="#" data-widget="contract-detail"
                                  data-id={txn.contractAddress}>{contractName}</a> created by TXN <a
            href="#" data-widget="txn-detail"
            data-id={txn.id}>{utils.truncAddress(txn.id)}</a></span>
    }

    function formatDecodedInput(txn) {
        const spanStyle = {
            fontWeight: "bold",
            color: "#375067",
        };
        return (
            <span> TXN <a href="#" data-widget="txn-detail"
                          data-id={txn.id}>{utils.truncAddress(txn.id)}</a>:
                <span style={spanStyle}>{txn.decodedInput.method}</span>
                (<span style={spanStyle}>{txn.decodedInput.args.join(
                    ",")}</span>)
            </span>
        )

    }

    function formatOther(txn) {
        return <span> TXN <a href="#" data-widget="txn-detail"
                             data-id={txn.id}>{utils.truncAddress(
            txn.id)}</a></span>

    }

    function getBody(txn) {
        if (!_.isEmpty(txn.contractAddress)) {
            // Contract creation
            return formatContractAddress(txn);

        } else if (txn.decodedInput) {
            return formatDecodedInput(txn);

        } else if (txn.message) {
            return <span style={{wordBreak: "break-all"}}> {txn.message}</span>

        } else {
            return formatOther(txn);
        }
    }

    function getHeader(txn) {
        return txn.blockNumber
            ? <span>[<a href="#" data-widget="block-detail"
                        data-id={txn.blockNumber}>#{txn.blockNumber}</a>]</span>
            : "";
    }

    return <tr style={{borderBottom: 2, dotted: "#faebcc"}}>
        <td>
            {getHeader(txn)}
            {getBody(txn)}
        </td>
    </tr>;
};
