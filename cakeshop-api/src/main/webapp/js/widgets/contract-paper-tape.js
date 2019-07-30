import React from 'react';
import ReactDOM from 'react-dom';
import utils from '../utils';
import {PaperTape} from "../components/PaperTape";

// TODO this is gross but there's no way to unsubscribe from Dashboard
// events without unsubscribing every other widget. So do it once in the
// widget, but update this function so it will always use the current txns
window.updatePapertapeState = () => {
};

module.exports = function() {
	var extended = {
		name: 'contract-paper-tape',
		title: 'Contract Paper Tape',
		size: 'third',

		url: 'api/contract/transactions/list',

		header: function(txn) {
			return this.templateHeader({txn: txn});
		},

        subscribe: function (data) {
            utils.subscribe('/topic/block', this.onNewTransactions.bind(this));

            Dashboard.Utils.on(function (ev, action) {
                if (action.indexOf('contract|transact|') === 0) {
                    const message = action.replace('contract|transact|', '');
                    window.updatePapertapeState({attributes: JSON.parse(message)})
                }
            });
        },

        onNewTransactions: function (data) {
            const transactions = data.data.attributes.transactions || [];
            transactions.forEach((transactionId) => {
                $.when(
                    utils.load(
                        {url: 'api/transaction/get', data: {id: transactionId}})
                ).done((res) => {
                    const data = res.data.attributes;
                    Dashboard.Utils.emit(
                        'contract|transact|' + JSON.stringify(data));
                })
            })
        },

        setData: function (data) {
			this.data = data;

            if (this.contractId !== data.id) {
                this.contractName = data.name;
                this.contractId = data.id;
                this.shouldFetch = true;

            } else {
                this.shouldFetch = false;
            }
		},

		fetch: function() {
			var _this = this;
            if (!this.shouldFetch) {
                return;
            }

			$.when(
				utils.load({ url: _this.url, data: { address: _this.contractId } })
			).fail(function(err) {
			    console.log("Error loading paper tape:", err)
				// TODO: Error will robinson!
			}).done(function(txns) {
				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html(_this.contractName + ' Paper Tape');

				const data = _.sortBy(txns.data,
					function(txn) {
						return parseInt(txn.attributes.blockNumber + '' + txn.attributes.transactionIndex);
					});

                $('#widget-' + _this.shell.id).html('<div id="paper-tape-container" style="width: 100%;"/>');

                ReactDOM.render(<PaperTape transactions={data} contractName={_this.contractName} />,
                    document.getElementById('paper-tape-container'));
			});
		},

		postRender: function() {
			$('#widget-' + this.shell.id).on('click', 'a', this._handle);
		},

		_handle: function(e) {
			e.preventDefault();

			var data = $(this).data('id');

			if ($(this).data('widget') === 'contract-detail') {
				data = $(this).data();
			}

			Dashboard.show({ widgetId: $(this).data('widget'), section: 'contracts', data: data, refetch: true });
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
