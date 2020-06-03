import utils from '../utils';

module.exports = function() {
    var extended = {
        name: 'reporting-query-result',
        size: 'medium',

        title: 'Query Result',

        template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),
        templateTxnRow: _.template('<tr><td style="width: 100px; text-overflow: ellipsis; overflow: hidden;"><%= value %></td></tr>'),

        setData: function(data) {
            this.contractAddress = data.id;
            this.query = data.query;
        },

        fetch: function() {
            var _this = this;

            $.when(
                function () {
                    if (_this.query === "query-tx-to") {
                        // display latest 100 max for now
                        return utils.load({ url: window.reportingEndpoint, data: {"jsonrpc":"2.0","method":"reporting_getAllTransactionsToAddress","params":[_this.contractAddress, {pageSize: 100}],"id":99} })
                    } else if (_this.query === "query-tx-internal-to") {
                        // display latest 100 max for now
                        return utils.load({ url: window.reportingEndpoint, data: {"jsonrpc":"2.0","method":"reporting_getAllTransactionsInternalToAddress","params":[_this.contractAddress, {pageSize: 100}],"id":99} })
                    } else {
                        return utils.load({ url: window.reportingEndpoint, data: {"jsonrpc":"2.0","method":"reporting_getContractCreationTransaction","params":[_this.contractAddress],"id":99} })
                    }
                }()
            ).fail(function(res) {
                $('#widget-' + _this.shell.id).html( '<h3 style="text-align: center;margin-top: 70px;">Search Failed</h3>' );
            }).done(function(res) {
                if (res.error) {
                    console.log(res.error);
                    $('#widget-' + _this.shell.id).html( '<h3 style="text-align: center;margin-top: 70px;">Search Failed</h3>' );
                    return
                }
                // console.log(JSON.stringify(res));
                if (res.result === "" || res.result.length === 0) {
                    $('#widget-' + _this.shell.id).html( '<h3 style="text-align: center;margin-top: 70px;">No Result</h3>' );
                    return
                }
                if (_this.query === "query-contract-creation") {
                    $('#widget-' + _this.shell.id).html( _this.template({ rows: '<tr><td style="width: 100px;"><a href="#">' + res.result  + '</a></td></tr>' }) );
                } else {
                    var rows = []
                    _.each(res.result, function(txHash) {
                        rows.push('<a href="#">' + txHash  + '</a>')
                    });
                    $('#widget-' + _this.shell.id).html( _this.template({ rows: _this.templateTxnRow({ value: rows.join('<br/>') }) }) );
                }
                // show tx details by click on the link
                $('#widget-' + _this.shell.id + ' a').click(function(e) {
                    e.preventDefault();
                    Dashboard.show({ widgetId: 'txn-detail', section: 'reporting', data: $(this).text(), refetch: true });
                });
            });
        }
    };


    var widget = _.extend({}, widgetRoot, extended);

    // register presence with screen manager
    Dashboard.addWidget(widget);
};
