import React from 'react';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Alert from '@material-ui/lab/Alert';
import InputLabel from "@material-ui/core/InputLabel";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";
import FormControl from "@material-ui/core/FormControl";

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
                <FormControl margin="dense" fullWidth>
                    <InputLabel>Contract Template Name</InputLabel>
                    <Select
                        value={props.newContract.name}
                        onChange={props.handleNewContractNameChange}
                    >
                        {props.templates.map( c => (
                            <MenuItem key={c} value={c}>{c}</MenuItem>
                        ))}
                    </Select>
                </FormControl>
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
