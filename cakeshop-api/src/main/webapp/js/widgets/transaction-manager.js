import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'transaction-manager',
		title: 'Transaction Manager Settings',
		size: 'medium',

		hideLink: true,

		url: {
			list: 'api/node/tm/peers',
			add: 'api/node/tm/add',
			remove: 'api/node/tm/remove'
		},

		template: _.template(
			'<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
			'	<tr><td style="width: 150px;">Own node</td><td class="value" id="own-node" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"></td></tr>' +
			'	<tr><td style="width: 150px;">Other nodes</td><td class="value" id="other-node" contentEditable="false" style="text-overflow: ellipsis; overflow: hidden;"></td></tr>' +
			'</table>' +
            // TODO connect to admin api on tessera and add/remove peers
			// '<div class="form-group">' +
			// '	<label for="addy">Peer URL</label>' +
			// '	<input type="text" class="form-control" id="addy">' +
			// '</div>'+
			// '<div class="form-group pull-right">' +
			// '	<button type="button" class="btn btn-primary" id="add">Add</button>' +
            // '	<button type="button" class="btn btn-secondary" id="remove">Remove</button>' +
			// '</div>'+
			'<div id="notification">' +
			'</div>'),

		rendered: false,

		fetch: function() {
			var _this = this;

			if (!_this.rendered) {
				_this._$().html( _this.template({}) );
			}

			_this.rendered = true;

			$.when(
				utils.load({ url: this.url.list })
			).done(function(info) {
				info = info.data.attributes.result;

				console.log("peers", info)
				_this._$('#own-node').html(info.url);
                _this._$('#other-node').html(
                    info.peers.map(peer => peer.url)
                    .filter(peerUrl => peerUrl !== info.url)
                    .sort((a, b) => a.localeCompare(b))
                    .join(", "));

				_this.postFetch();
			}.bind(this));
		},

		postRender: function() {
			utils.makeAreaEditable('#widget-' + this.shell.id + ' .value');

			this._$('button').click(this._handler);
		},

		_handler: function(ev) {
			var _this = widget,
             action = $(this).attr('id'),
			 input = _this._$('#addy'),
			 notif = _this._$('#notification');

			if (!input.val()) {
				return;
			}

			$.when(
				utils.load({ url: _this.url[action], data: { 'transactionManagerNode': input.val() } })
			).done(function(r) {
				notif.show();

				if ( (r) && (r.error) ) {
					notif
					 .addClass('text-danger')
					 .removeClass('text-success')
					 .html(r.error.message);

				} else {
					input.val('');

					notif
					 .removeClass('text-danger')
					 .addClass('text-success')
					 .html('Request to add/remove transaction manager node is sent');

					setTimeout(function() {
						notif.fadeOut();
						_this.fetch();
					}, 2000);
				}
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
