import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'node-info',
		title: 'Node Info',
		size: 'medium',

		hideLink: true,

		// url: 'api/node/get',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),
		templateRow: _.template('<tr><td style="width: 150px;"><%= key %></td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= value %></td></tr>'),


		subscribe: function() {
			// adding listener to reload the widget if identity is updated
			Dashboard.Utils.on(function(ev, action) {
				if (action === 'node-settings|updated|identity') {
					widget.onData(Tower.status);
				} else if (action === 'node-status|announce') {
					widget.onData(Tower.status);
				}
			});
		},

		// TODO: renders after every fetch. May need to re-render only when needed
		onData: function(status) {
			var customOrder = _.reduce([
				'nodeUrl',
				'rpcUrl',
				'nodeName',
				'nodeIP',
				'nodePort',
				'nodeRpcPort',
				'latestBlock',
				'peerCount',
				'pendingTxn',
				'status',
				'mining',
				'quorum',
				'quorumInfo'
			], function(memo, v, i) {
				memo[v] = i;

				return memo;
			}, {});


			var rows = [],
			 keys = _.sortBy(_.keys(status), function(key) {
				// custom reorder of the returned keys
				if (key in customOrder) {
					return customOrder[key];
				}

				return (99999);
			});

			keys = utils.idAlwaysFirst(keys);

			// objects not shown in this widget
			keys = _.without(keys, 'config', 'peers', 'quorumInfo');

			_.each(keys, function(val, key) {
				rows.push( this.templateRow({ key: utils.camelToRegularForm(val), value: status[val] }) );
			}.bind(this));


			// Appending quorum info if present
			if (status.hasOwnProperty('quorumInfo') && !_.isEmpty(status.quorumInfo)) {
				rows.push( '<tr><td colspan="2" style="font-weight:bold;padding-top:20px;">Quorum Info</td></tr>' );

				keys =_.keys(status.quorumInfo).sort(function (a, b) {
					if (a < b) return -1;
					if (b < a) return 1;

					return 0;
				});

				keys = _.without(keys, 'blockMakerStrategy');

				_.each(keys, function(val, key) {
					rows.push( this.templateRow({ key: utils.camelToRegularForm(val), value: status.quorumInfo[val] }) );
				}.bind(this));

				// Strategy
				if ( status.quorumInfo.hasOwnProperty('blockMakerStrategy')
					&& !_.isEmpty(status.quorumInfo.blockMakerStrategy) ) {

					rows.push( '<tr><td colspan="2" style="font-weight:bold;padding-top:20px;">Block Maker Strategy</td></tr>' );

					keys =_.keys(status.quorumInfo.blockMakerStrategy).sort(function (a, b) {
						if (a < b) return -1;
						if (b < a) return 1;

						return 0;
					});

					_.each(keys, function(val, key) {
						rows.push( this.templateRow({ key: utils.camelToRegularForm(val), value: status.quorumInfo.blockMakerStrategy[val] }) );
					}.bind(this));
				}
			}

			$('#widget-' + this.shell.id).html( this.template({ rows: rows.join('') }) );

			utils.makeAreaEditable('#widget-' + this.shell.id + ' .value');
		},

		fetch: function() {
			widget.onData(Tower.status);
		},
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
