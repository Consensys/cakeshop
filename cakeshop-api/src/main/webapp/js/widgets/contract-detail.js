import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'contract-detail',
		title: 'Contract Detail',
		size: 'medium',

		topic: 'topic/block',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),
		templateRow: _.template('<tr><td style="width: 100px;"><%= key %></td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= value %></td></tr>'),

		templateCodeRow: _.template('<tr><td style="width: 100px;"><%= key %></td><td class="value" contentEditable="false"><pre><code><%= value %></code></pre></td></tr>'),

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
				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html(contract.get('name') + ' Contract');

				var rows = [],
				 keys = _.keys(contract.attributes);

				// key cleanup + reorder
				keys = _.without(keys, 'address', 'code', 'abi');

				keys.push('abi');
				keys.push('code');


				// add id row instead of removed address
				rows.push( _this.templateRow({ key: utils.camelToRegularForm('id'), value: contract.id }) );

				_.each(keys, function(val, key) {
					if ( (!contract.attributes[val]) || (contract.attributes[val].length == 0) ) {
						return;
					}

					var template = _this.templateRow;

					if ( (val === 'code') || (val === 'abi') ) {
						template = _this.templateCodeRow;
					}

					if (val === 'createdDate') {
						contract.attributes[val] = moment.unix(contract.attributes[val]).format('YYYY-MM-DD hh:mm A');
					}

					rows.push( template({ key: utils.camelToRegularForm(val), value: contract.attributes[val] }) );
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );

				utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');

				_this.postFetch();
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
