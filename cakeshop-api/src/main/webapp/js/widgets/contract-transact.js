import utils from '../utils';
import {TransactTable} from "../components/Transact";
import React from "react";
import ReactDOM from "react-dom";

module.exports = function() {
	var extended = {
		name: 'contract-transact',
		title: 'Contract Transact',
		size: 'third',
        minHeight: 600,

		topic: 'topic/block',

		setData: function(data) {
			this.data = data;

			this.contractId = data.id;
		},

		fetch: function() {
			var _this = this;

            $.when(
                utils.load({ url: "api/wallet/list" })
            ).done(function(res) {
                const accounts = res.data.map((account) => ({ address: account.id }))
			    Contract.get(_this.contractId).done(function(contract) {
			    	$('#widget-shell-' + _this.shell.id + ' .panel-title span').html(contract.get('name') + ' Contract');
                    $('#widget-' + _this.shell.id).html('<table id="contract-transact-table" style="width: 100%; table-layout: fixed;" class="table table-striped"/>');

                    ReactDOM.render(<TransactTable accounts={accounts}
                                                   activeContract={contract}
                                                   onTransactionSubmitted={(res, method, methodSig, methodArgs) => _this.onResponse(res, method, methodSig, methodArgs, _this.data)}/>,
                        document.getElementById('contract-transact-table'));

			    });
            });
		},

        onResponse: (res, method, methodSig, methodArgs, data) => {
            let message,
                _this = this;
		    if(method.constant) {
                message = `[read] ${method.name} => ${JSON.stringify(res)}`;
            } else {
                message = "[txn] " + methodSig + " => created tx " + res;
            }
            Dashboard.show({
                widgetId: 'contract-paper-tape',
                section: 'contracts',
                data: data,
                refetch: true
            });
		    // delay a bit just in case we're opening papertape for the first time
		    setTimeout(() => {
                    _this.count += 1
		            Dashboard.Utils.emit('contract|transact|' + JSON.stringify({id: Math.random(), message}))
                },
                100);
        }

    };

	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
