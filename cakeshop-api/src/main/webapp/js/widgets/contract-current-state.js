import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'contract-current-state',
		title: 'Contract State',
		size: 'small',

		topic: 'topic/transaction',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
			'<thead style="font-weight: bold;"><tr><td>Method</td><td>Result</td></tr></thead>' +
			'<%= rows %></table>'),
		templateRow: _.template('<tr><td><%= key %></td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= value %></td></tr>'),

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

					var rows = [];

					_.each(stateArray, function(state) {
						rows.push( _this.templateRow({ key: state.method.name, value: state.result }) );
					});

					$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );

					utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');

					_this.postFetch();
				});
			});
		}
	};

	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
