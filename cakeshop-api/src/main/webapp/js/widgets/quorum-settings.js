import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'quorum-settings',
		title: 'Quorum Settings',
		size: 'small',

		hideLink: true,

		url: 'api/node/get',
		update_url: 'api/node/update',

		template: _.template(
			'<div class="form-group">' +
			'	<label for="blockMakerAccount">Block Maker Account</label>' +
			'	<select id="blockMakerAccount" class="form-control" style="transition: none;">' +
			'	</select>' +
			'</div>' +
			'<div class="form-group">' +
			'	<label for="voterAccount">Voter Account</label>' +
			'	<select id="voterAccount" class="form-control" style="transition: none;">' +
			'	</select>' +
			'</div>' +
			'<div class="form-group">' +
			'	<label for="minBlockTime">Minimum Block Time</label>' +
			'	<input type="number" class="form-control" id="minBlockTime">' +
			'</div>' +
			'<div class="form-group">' +
			'	<label for="maxBlockTime">Maximum Block Time</label>' +
			'	<input type="number" class="form-control" id="maxBlockTime">' +
			'</div>'),


		subscribe: function() {
			// adding listener to reload the widget if identity is updated
			Dashboard.Utils.on(function(ev, action) {
				if (action === 'node-status|announce') {
					widget.onData(Tower.status);
				}
			});
		},

		rendered: false,
		onData:function(status) {
			if (this.rendered) {
				return;
			}

			// Show & populate quorum settings if needed
			if (status.hasOwnProperty('quorumInfo')) {
				Account.list().then(function(accounts) {
					var rows = ['<option>None</option>'];

					accounts.forEach(function(acct) {
						rows.push( '<option>' + acct.get('address') + '</option>' );
					});

					this._$('#blockMakerAccount')
						.html( rows.join('') )
						.val( status.quorumInfo.blockMakerAccount );

					this._$('#voterAccount')
						.html( rows.join('') )
						.val( status.quorumInfo.voteAccount );

				}.bind(this));

				this._$('#minBlockTime').val( status.quorumInfo.blockMakerStrategy.minBlockTime );
				this._$('#maxBlockTime').val( status.quorumInfo.blockMakerStrategy.maxBlockTime );
			}
		},


		fetch: function() {
			this.rendered = false;
			widget.onData(Tower.status);
		},


		render: function() {
			Dashboard.render.widget(this.name, this.shell.tpl);

			this._$()
				.css({ 'height': '240px', 'margin-bottom': '10px', 'overflow': 'auto' })
				.html( this.template({}) );

			this._$('.form-control').change(this._handler);
			$(document).trigger('WidgetInternalEvent', ['widget|rendered|' + this.name]);
		},


		_handler: function(ev) {
			var _this = $(this),
			 action = _this.attr('id'),
			 val = _this.val(),
			 data = {};

			data[action] = val;

			$.when(
				utils.load({ url: widget.update_url, data: data })
			).done(function(info) {
				// trigger event update
				Dashboard.Utils.emit(widget.name + '|updated|' + action);
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
