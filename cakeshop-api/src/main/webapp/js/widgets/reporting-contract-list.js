import utils from '../utils'
import $ from "jquery";
import ReactDOM from "react-dom";
import { RegisterContractContainer } from "../components/RegisterContractContainer";
import React from "react";

module.exports = function() {
    var extended = {
        name: 'reporting-contract-list',
        title: 'Reporting Registered Contract List',
        size: 'medium',

        hideLink: true,

        template: _.template('<div>'
            + '<table style="width: 100%; table-layout: fixed;" class="table table-striped">'
            + '<thead style="font-weight: bold;"><tr><td style="width: 300px;">Contract</td><td style="width: 250px;">Actions</td></tr></thead>'
            + '<tbody><%= rows %></tbody></table>'
            + '<div id="register-dialog" style="width: 100%;"/>'
            + '</div>'
        ),

        templateUninitialized: _.template(
            '<div>'
            + '<h3 style="text-align: center;margin-top: 70px;">No Connection to Reporting Engine</h3>'
            + '</div>'
        ),

        templateRow: _.template('<tr>'
            + '<td><%= contract %></td>'
            + '<td data-id="<%= contract %>">'
            + '<button class="btn btn-primary btn-xs" data-query="query-contract-creation" data-widget="reporting-query-result">Creation Tx</button> '
            + '<button class="btn btn-primary btn-xs" data-query="query-tx-to" data-widget="reporting-query-result">To Tx</button> '
            + '<button class="btn btn-primary btn-xs" data-query="query-tx-internal-to" data-widget="reporting-query-result">Internal To Tx</button> '
            + '<button class="btn btn-primary btn-xs" data-widget="reporting-report">Report</button>'
            + '</td></tr>'
        ),

        setTitle: function (title) {
            $('#widget-shell-' + this.shell.id + ' .panel-title span').html(
                title);
        },

        updateView: function (res) {
            if (!window.reportingEndpoint) {
                $('#widget-' + this.shell.id).html(this.templateUninitialized());
                return
            }
            if (res.error) {
                this.setTitle("Load Error: " + res.error);
                $('#widget-' + this.shell.id).html(this.templateUninitialized());
                return
            }
            // if reporting engine is connected
            console.log("Connecting to reporting endpoint.");
            var _this = this,
                rowsOut = [];

            let registeredContracts = res.result;
            _.each(registeredContracts, function(c) {
                rowsOut.push(_this.templateRow({contract: c}));
            });
            if (rowsOut.length === 0) {
                rowsOut.push(
                    "<tr><td/><td>No contracts registered yet.</td><td/><td/></tr>")
            }
            $('#widget-' + _this.shell.id).html(
                _this.template({rows: rowsOut.join('')}));

            ReactDOM.render(<RegisterContractContainer addContract={this.addContract} addTemplate={this.addTemplate} />, document.getElementById('register-dialog'));
            _this.postFetch();
        },

        addContract: function(newContract) {
            var _this = this;
            $.when(
                utils.load({ url: window.reportingEndpoint, data: {"jsonrpc":"2.0","method":"reporting_addAddress","params":[newContract.address],"id":100} })
            ).fail(function (res) {
                console.log("Failed to register new contract: ", res);
            }).done(function (res) {
                console.log("Successfully register new contract: ", newContract.address);
                $.when(
                    utils.load({ url: window.reportingEndpoint, data: {"jsonrpc":"2.0","method":"reporting_assignTemplate","params":[newContract.address, newContract.name],"id":101} })
                ).fail(function (res) {
                    console.log("Failed to register new contract abi: ", res);
                }).done(function (res) {
                    console.log("Successfully assign template name: ", newContract.name);
                });
                this.fetch()
            });
        },

        addTemplate: function(newTemplate) {
            var _this = this;
            $.when(
                utils.load({ url: window.reportingEndpoint, data: {"jsonrpc":"2.0","method":"reporting_addTemplate","params":[newTemplate.name, newTemplate.abi, newTemplate.storage],"id":100} })
            ).fail(function (res) {
                console.log("Failed to add new contract template: ", res);
            }).done(function (res) {
                console.log("Successfully add new contract template: ", newTemplate);
                this.fetch()
            });
        },

        fetch: function () {
            var _this = this;
            $.when(
                utils.load({ url: window.reportingEndpoint, data: {"jsonrpc":"2.0","method":"reporting_getAddresses","params":[],"id":99} })
            ).fail(function (res) {
                console.log("Failed to load reporting registered addresses: ", res);
            }).done(function (res) {
                _this.updateView(res);
            });
        },

        postRender: function() {
            $('#widget-' + this.shell.id).on('click', 'button',
                this._handleButton.bind(this));
        },

        _handleButton: function(e) {
            e.preventDefault();
            Dashboard.show({
                widgetId: $(e.target).data('widget'),
                section: 'reporting',
                data: _.extend({id: $(e.target).parent().data('id')}, $(e.target).data()),
                refetch: true
            });
        }
    };


    var widget = _.extend({}, widgetRoot, extended);

    // register presence with screen manager
    Dashboard.addWidget(widget);
};
