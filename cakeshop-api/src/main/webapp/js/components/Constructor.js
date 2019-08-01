import React, {useState} from "react";
import {PrivateFor} from "./PrivateFor";
import {Method} from "./Transact";

export const Constructor = ({method, onDeploy}) => {

    const [privateFor, setPrivateFor] = useState([]);

    return (
        <tbody>
            <tr className="private_for">
                <td colSpan="2" className="from_address"><label
                    htmlFor="private_for" title="One key per line">Private
                    For</label>
                    <PrivateFor initialPrivateFor={""}
                                onChange={(options) => setPrivateFor(options)}/>
                </td>
            </tr>

            <Method key={"constructor"}
                    method={method}
                    onSubmit={(inputValues) => onDeploy(inputValues,
                        privateFor)}
                    inlineButton={false}
            />

        </tbody>
    );
}
