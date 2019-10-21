import React, {useState} from "react";
import {
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle
} from "@material-ui/core";
import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";

export const AddNodeDialog = ({open, onSubmit, onCancel}) => {

    const [name, setName] = useState("");
    const [rpcUrl, setRpcUrl] = useState("http://localhost:22000");
    const [transactionManagerUrl, setTransactionManagerUrl] = useState("http://localhost:9001");
    return (
        <Dialog open={open} onClose={onCancel}
                aria-labelledby="form-dialog-title">
            <DialogTitle id="form-dialog-title">Add Node</DialogTitle>
            <form onSubmit={(e) => {
                e.preventDefault();
                onSubmit({name, rpcUrl, transactionManagerUrl});
            }}>
                <DialogContent>
                    <DialogContentText>
                        To attach to a node, add the geth node's RPC url and the
                        Tessera P2P url, if using.
                    </DialogContentText>
                    <TextField
                        autoFocus
                        margin="dense"
                        id="name"
                        label="Node Name"
                        type="text"
                        value={name}
                        fullWidth
                        onChange={(e) => setName(e.target.value)}
                    />
                    <TextField
                        margin="dense"
                        id="geth"
                        label="Geth RPC Url"
                        type="url"
                        value={rpcUrl}
                        fullWidth
                        onChange={(e) => setRpcUrl(e.target.value)}
                    />
                    <TextField
                        margin="dense"
                        id="tessera"
                        label="Tessera P2P Url (optional)"
                        defaultValue="http://localhost:9001"
                        type="url"
                        fullWidth
                        onChange={(e) => setTransactionManagerUrl(e.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={onCancel} color="primary">
                        Cancel
                    </Button>
                    <Button type="submit" color="primary">
                        Add
                    </Button>
                </DialogActions>
            </form>
        </Dialog>
    );
};
