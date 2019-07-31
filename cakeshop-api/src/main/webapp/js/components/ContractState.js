import React from "react";

export const ContractState = ({contractState}) => {

    return (
        <table style={{width: "100%", tableLayout: "fixed"}}
               className="table table-striped">
            <thead style={{fontWeight: "bold"}}>
            <tr>
                <td>Method</td>
                <td>Result</td>
            </tr>
            </thead>
            <tbody>
            {contractState.map((item) => (
                <tr key={item.method.name}>
                    <td>{item.method.name}</td>
                    <td className="value" style={{
                        textOverflow: "ellipsis",
                        whiteSpace: "nowrap",
                        overflow: "hidden"
                    }}>{item.result}</td>
                </tr>
            ))}
            </tbody>
        </table>
    )

};
