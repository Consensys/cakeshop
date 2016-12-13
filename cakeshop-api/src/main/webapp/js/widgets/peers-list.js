import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'peers-list',
		title: 'Peer List',
		size: 'medium',
		numPeers: 0,

		hideLink: true,

		url: 'api/node/peers',
		topic: '/topic/node/status',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),
		templateRow: _.template('<tr><td style="padding-left: 0px; padding-right: 0px; padding-top: 0px; padding-bottom: 10px;">' +
			'<table style="width: 100%; table-layout: fixed; background-color: inherit; margin-bottom: initial;" class="table">' +
			'	<tr><td style="font-weight: bold; width: 35px;">Peer</td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;" colspan="2"><%= o.nodeUrl %></td></tr>' +
			'	<tr><td style="font-weight: bold;">Info</td><td><%= o.nodeName %></td><td><%= o.nodeIP %></td></tr>' +
			//'	<tr><td style="font-weight: bold;">IPs</td><td><%= o.nodeIP %></td><td><%= o.status %></td></tr>' +
			'</table></td></tr>'),


		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: this.url })
			).done(function(info) {
				var rows = [];
				this.numPeers = info.data.length;

				if (info.data.length > 0) {
					_.each(info.data, function(peer) {
						rows.push( _this.templateRow({ o: peer.attributes }) );
					});

					Dashboard.Utils.emit( widget.name + '|fetch|' + JSON.stringify(info.data) );

					$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );

					utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');
				} else {
					// no peers
					$('#widget-' + _this.shell.id).html('');
				}

				_this.postFetch();
			}.bind(this));
		},

		subscribe: function() {
			utils.subscribe(this.topic, this.updatePeers.bind(this));
		},

		updatePeers: function(response) {
			if (response.data.attributes.peerCount != this.numPeers) {
				this.fetch();
			}
		}

	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
