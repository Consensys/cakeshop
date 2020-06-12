import React from 'react';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Alert from '@material-ui/lab/Alert';

export const AddTemplateDialog = (props) => {

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            props.handleRegisterNewContract()
        }
    };

    return (
        <Dialog open={props.isOpen} onClose={props.handleCloseSetting} aria-labelledby="form-dialog-title" maximumwidth="400" fullWidth>
            <DialogTitle id="form-dialog-title">Add new Template</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    Add contract template with ABI & Storage information
                </DialogContentText>
                <br/>
                <TextField
                    label="Contract Template Name"
                    value={props.newTemplate.name}
                    onChange={props.handleNewTemplateNameChange}
                    onKeyPress={handleKeyPress}
                    margin="dense"
                    fullWidth
                    autoFocus
                />
                <TextField
                    label="Contract Template ABI"
                    value={props.newTemplate.abi}
                    onChange={props.handleNewTemplateABIChange}
                    onKeyPress={handleKeyPress}
                    margin="dense"
                    fullWidth
                    multiline
                />
                <TextField
                    label="Contract Template Storage Template"
                    value={props.newTemplate.storage}
                    onChange={props.handleNewTemplateStorageChange}
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
                <Button onClick={props.handleAddNewTemplate} color="primary">
                    Add
                </Button>
            </DialogActions>
        </Dialog>
    )
};
