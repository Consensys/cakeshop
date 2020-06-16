import React from 'react';
import Button from "@material-ui/core/Button";
import { RegisterContractDialog } from './RegisterContractDialog';
import { AddTemplateDialog } from "./AddTemplateDialog";

export class RegisterContractContainer extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            contractFormIsOpen: false,
            templateFormIsOpen: false,
            newContract: {
                address: "",
                name: "",
            },
            newTemplate: {
                name: "",
                abi: "",
                storage: "",
            },
            templates: [],
            errorMessage: "",
        }
    }

    handleTemplateOpenSetting = () => {
        this.setState({ templateFormIsOpen: true, contractFormIsOpen: false, errorMessage: "" })
    };

    handleContractOpenSetting = () => {
        this.props.getTemplates( (templates) => {
            this.setState({ templates })
        });
        this.setState({ templateFormIsOpen: false, contractFormIsOpen: true, errorMessage: "" })
    };

    handleCloseSetting = () => {
        this.setState({ templateFormIsOpen: false, contractFormIsOpen: false })
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

    handleNewContractNameChange = (e) => {
        this.setState({
            newContract: {
                ...this.state.newContract,
                name: e.target.value,
            },
            errorMessage: "",
        })
    };

    handleNewTemplateNameChange = (e) => {
        this.setState({
            newTemplate: {
                ...this.state.newTemplate,
                name: e.target.value,
            },
            errorMessage: "",
        })
    };

    handleNewTemplateABIChange = (e) => {
        this.setState({
            newTemplate: {
                ...this.state.newTemplate,
                abi: e.target.value,
            },
            errorMessage: "",
        })
    };

    handleNewTemplateStorageChange = (e) => {
        this.setState({
            newTemplate: {
                ...this.state.newTemplate,
                storage: e.target.value,
            },
            errorMessage: "",
        })
    };

    handleAddNewTemplate = () => {
        if (this.state.newTemplate.name === ""){
            this.setState({
                errorMessage: "template name must not be empty",
            });
            return
        }
        if (this.state.newTemplate.abi === ""){
            this.setState({
                errorMessage: "abi must not be empty",
            });
            return
        }
        if (this.state.newTemplate.storage === "") {
            this.setState({
                errorMessage: "storage layout must not be empty",
            });
            return
        }
        this.props.addTemplate(this.state.newTemplate);
        this.handleCloseSetting();
    };

    handleRegisterNewContract = () => {
        if (this.state.newContract.address === ""){
            this.setState({
                errorMessage: "address must not be empty",
            });
            return
        }
        // newContract.name is optional
        this.props.addContract(this.state.newContract);
        this.handleCloseSetting();
    };

    render(){
        return (
            <div>
                <Button variant="contained" size="small" style={{backgroundColor: "#337AB7", color: "white", marginRight: "5px"}} onClick={this.handleTemplateOpenSetting}>
                    Add Template
                </Button>
                <Button variant="contained" size="small" style={{backgroundColor: "#337AB7", color: "white"}} onClick={this.handleContractOpenSetting}>
                    Register Contract
                </Button>
                <br/>
                <RegisterContractDialog
                    isOpen={this.state.contractFormIsOpen}
                    handleCloseSetting={this.handleCloseSetting}
                    handleNewContractAddressChange={this.handleNewContractAddressChange}
                    handleNewContractNameChange={this.handleNewContractNameChange}
                    handleRegisterNewContract={this.handleRegisterNewContract}
                    newContract={this.state.newContract}
                    templates={this.state.templates}
                    errorMessage={this.state.errorMessage}
                />
                <AddTemplateDialog
                    isOpen={this.state.templateFormIsOpen}
                    handleCloseSetting={this.handleCloseSetting}
                    handleNewTemplateNameChange={this.handleNewTemplateNameChange}
                    handleNewTemplateABIChange={this.handleNewTemplateABIChange}
                    handleNewTemplateStorageChange={this.handleNewTemplateStorageChange}
                    handleAddNewTemplate={this.handleAddNewTemplate}
                    newTemplate={this.state.newTemplate}
                    errorMessage={this.state.errorMessage}
                />
            </div>
        )
    }
}
