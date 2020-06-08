import React from 'react';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Alert from '@material-ui/lab/Alert';

export const RegisterContractDialog = (props) => {

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            props.handleRegisterNewContract()
        }
    };

    return (
        <Dialog open={props.isOpen} onClose={props.handleCloseSetting} aria-labelledby="form-dialog-title" maximumwidth="400" fullWidth>
            <DialogTitle id="form-dialog-title">Register new contract</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    Register a new contract for reporting.
                </DialogContentText>
                <br/>
                <TextField
                    label="Contract Address"
                    value={props.newContract.address}
                    onChange={props.handleNewContractAddressChange}
                    onKeyPress={handleKeyPress}
                    margin="dense"
                    fullWidth
                    autoFocus
                />
                <TextField
                    label="Contract ABI"
                    value={props.newContract.abi}
                    onChange={props.handleNewContractABIChange}
                    onKeyPress={handleKeyPress}
                    margin="dense"
                    fullWidth
                    multiline
                />
                <TextField
                    label="Contract Storage Template"
                    value={props.newContract.template}
                    onChange={props.handleNewContractTemplateChange}
                    onKeyPress={handleKeyPress}
                    margin="dense"
                    fullWidth
                    multiline
                />
            </DialogContent>
            {
                props.errorMessage &&
                <div>
                    <br/>
                    <Alert severity="error">{props.errorMessage}</Alert>
                </div>
            }
            <DialogActions>
                <Button onClick={props.handleCloseSetting} color="primary">
                    Cancel
                </Button>
                <Button onClick={props.handleRegisterNewContract} color="primary">
                    Register
                </Button>
            </DialogActions>
        </Dialog>
    )
};
