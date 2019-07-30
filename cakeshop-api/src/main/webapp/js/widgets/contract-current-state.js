import React from 'react';
import ReactDOM from 'react-dom';
import utils from '../utils';
import {ContractState} from "../components/ContractState";

module.exports = function() {
	var extended = {
		name: 'contract-current-state',
		title: 'Contract State',
		size: 'third',

		topic: 'topic/transaction',

		setData: function(data) {
			this.data = data;
			this.contractId = data.id;
		},

		subscribe: function(data) {
			// subscribe to get updated states
			utils.subscribe(this.topic, this.onUpdatedState.bind(this));
		},

		onUpdatedState: function(data) {
			if (data.data.attributes.transactions.length > 0) {
				this.fetch();
			}
		},

		fetch: function() {
			var _this = this;

			Contract.get(this.contractId).done(function(contract) {
				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html(contract.get('name') + ' State');

				contract.readState().done(function(stateArray) {
					if (!stateArray || stateArray.length == 0) {
						// TODO: show error / message?
						return;
					}
                    $('#widget-' + _this.shell.id).html('<div id="contract-state-container" style="width: 100%;"/>');

                    ReactDOM.render(<ContractState contractState={stateArray} />,
                        document.getElementById('contract-state-container'));
				});
			});
		}
	};

	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
