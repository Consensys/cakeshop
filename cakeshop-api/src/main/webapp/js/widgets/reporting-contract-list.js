import utils from '../utils'
import $ from "jquery";

module.exports = function() {
    var extended = {
        name: 'reporting-contract-list',
        title: 'Reporting Registered Contract List',
        size: 'medium',

        hideLink: true,

        template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
            '<thead style="font-weight: bold;"><tr><td style="width: 300px;">Contract</td><td style="width: 250px;">Actions</td></tr></thead>'
            + '<tbody><%= rows %></tbody></table>'),

        templateUninitialized: _.template(
            '<div><button class="btn btn-lg btn-primary" id="start-reporting-button">Connect to Reporting Engine</button></div>'
        ),

        templateRow: _.template('<tr>'
            + '<td><%= contract %></td>'
            + '<td data-id="<%= contract %>">'
            + '<button class="btn btn-primary btn-xs" data-query="query-contract-creation" data-widget="reporting-query-result">Contract Creation</button>'
            + '<button class="btn btn-primary btn-xs" data-query="query-tx-to" data-widget="reporting-query-result">TX To</button>'
            + '<button class="btn btn-primary btn-xs" data-query="query-tx-internal-to" data-widget="reporting-query-result">TX Internal To</button>'
            + '<button class="btn btn-primary btn-xs" data-widget="reporting-report">Report</button>'
            + '</td></tr>'),

        setTitle: function (title) {
            $('#widget-shell-' + this.shell.id + ' .panel-title span').html(
                title);
        },

        updateView: function (res) {
            if (!window.reportingEndpoint) {
                this.setTitle("Connect to Reporting Engine");
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

            _this.postFetch();
        },

        fetch: function () {
            var _this = this;
            $.when(
                utils.load({ url: window.reportingEndpoint, data: {"jsonrpc":"2.0","method":"reporting_getAddresses","params":[],"id":99} })
            ).fail(function (res) {
                console.log("Failed to load reporting registered addresses", res);
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

            if (e.target.id === "start-reporting-button") {
                $(e.target).attr('disabled', 'disabled');
                $('#start-reporting').click();
                this.fetch();
            } else {
                Dashboard.show({
                    widgetId: $(e.target).data('widget'),
                    section: 'reporting',
                    data: _.extend({id: $(e.target).parent().data('id')}, $(e.target).data()),
                    refetch: true
                });
            }
        }
    };


    var widget = _.extend({}, widgetRoot, extended);

    // register presence with screen manager
    Dashboard.addWidget(widget);
};
