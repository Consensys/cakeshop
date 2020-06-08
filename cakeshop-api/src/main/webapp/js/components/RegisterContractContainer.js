import React from 'react';
import IconButton from '@material-ui/core/IconButton';
import AddIcon from '@material-ui/icons/Add';
import { RegisterContractDialog } from './RegisterContractDialog';

export class RegisterContractContainer extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            formIsOpen: false,
            newContract: {
                address: "",
                abi: "",
                template: "",
            },
            errorMessage: "",
        }
    }

    handleOpenSetting = () => {
        this.setState({ formIsOpen: true })
    };

    handleCloseSetting = () => {
        this.setState({ formIsOpen: false })
    };

    handleNewContractAddressChange = (e) => {
        this.setState({
            newContract: {
                ...this.state.newContract,
                address: e.target.value,
            },
            errorMessage: "",
        })
    };

    handleNewContractABIChange = (e) => {
        this.setState({
            newContract: {
                ...this.state.newContract,
                abi: e.target.value,
            },
            errorMessage: "",
        })
    };

    handleNewContractTemplateChange = (e) => {
        this.setState({
            newContract: {
                ...this.state.newContract,
                template: e.target.value,
            },
            errorMessage: "",
        })
    };

    handleRegisterNewContract = () => {
        if (this.state.newContract.address === ""){
            this.setState({
                errorMessage: "address must not be empty",
            });
            return
        }
        if (this.state.newContract.abi === ""){
            this.setState({
                errorMessage: "abi must not be empty",
            });
            return
        }
        if (this.state.newContract.template === "") {
            this.setState({
                errorMessage: "template must not be empty",
            });
            return
        }
        this.props.addContract(this.state.newContract);
        this.setState({ formIsOpen: false })
    };

    render(){
        return (
            <div>
                <IconButton color="primary" variant="h4" onClick={this.handleOpenSetting}>
                    <AddIcon />
                </IconButton>
                <br/>
                <RegisterContractDialog
                    isOpen={this.state.formIsOpen}
                    handleCloseSetting={this.handleCloseSetting}
                    handleNewContractAddressChange={this.handleNewContractAddressChange}
                    handleNewContractABIChange={this.handleNewContractABIChange}
                    handleNewContractTemplateChange={this.handleNewContractTemplateChange}
                    handleRegisterNewContract={this.handleRegisterNewContract}
                    newContract={this.state.newContract}
                    errorMessage={this.state.errorMessage}
                />
            </div>
        )
    }
}
