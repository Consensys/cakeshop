import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'constellation',
		title: 'Constellation Settings',
		size: 'small',

		hideLink: true,

		url: {
			list: 'api/node/constellation/list',
			add: 'api/node/constellation/add',
			remove: 'api/node/constellation/remove'
		},

		template: _.template(
			'<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
			'	<tr><td style="width: 150px;">Own node</td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;">http://127.0.0.1:9000</td></tr>' +
			'	<tr><td style="width: 150px;">Other nodes</td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;">http://127.0.0.1:9001, http://127.0.0.1:9002</td></tr>' +
			'</table>' +
			'<div class="form-group">' +
			'	<label for="addy">Constellation URL</label>' +
			'	<input type="text" class="form-control" id="addy">' +
			'</div>'+
			'<div class="form-group pull-right">' +
			'	<button type="button" class="btn btn-primary">Add</button>' +
			'</div>'+
			'<div id="notification">' +
			'</div>'),

		rendered: false,

		fetch: function() {
			var _this = this;

			this._$().html( this.template({}) );
			utils.makeAreaEditable('#widget-' + this.shell.id + ' .value');

			$.when(
				utils.load({ url: this.url.list })
			).done(function(info) {
				// TODO!

				// var rows = [];
				// this.numPeers = info.data.length;
				//
				// if (info.data.length > 0) {
				// 	_.each(info.data, function(peer) {
				// 		rows.push( _this.templateRow({ o: peer.attributes }) );
				// 	});
				//
				// 	Dashboard.Utils.emit( widget.name + '|fetch|' + JSON.stringify(info.data) );
				//
				// 	$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );
				//
				// 	utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');
				// } else {
				// 	// no peers
				// 	$('#widget-' + _this.shell.id).html('');
				// }

				_this.postFetch();
			}.bind(this));
		},

		postRender: function() {
			this._$('button').click(this._handler);
		},

		_handler: function(ev) {
			var _this = widget,
			 input = _this._$('#addy'),
			 notif = _this._$('#notification');

			if (!input.val()) {
				return;
			}

			$.when(
				utils.load({ url: _this.url.add, data: { "constellationNode": input.val() } })
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
					 .html('Request to add constelltion node is sent');

					setTimeout(function() {
						notif.fadeOut();
					}, 2000);
				}
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
