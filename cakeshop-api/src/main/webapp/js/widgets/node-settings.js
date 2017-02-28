import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'node-settings',
		title: 'Node Settings',
		size: 'small',

		hideLink: true,

		url: 'api/node/get',
		update_url: 'api/node/update',

		template: _.template(
			'<div class="form-group">' +
			'	<label for="committingTransactions">Commiting Transactions</label>' +
			'	<select id="committingTransactions" class="form-control" style="transition: none;">' +
			'		<option value="true">Yes</option>' +
			'		<option value="false">No</option>' +
			'	</select>' +
			'</div>' +
			'<div class="form-group">' +
			'	<label for="networkId">Network ID</label>' +
			'	<input type="number" class="form-control" id="networkId">' +
			'</div>' +
			'<div class="form-group">' +
			'	<label for="identity">Identity</label>' +
			'	<input type="text" class="form-control" id="identity">' +
			'</div>' +
			'<div class="form-group">' +
			'	<label for="logLevel">Log Level</label>' +
			'	<select id="logLevel" class="form-control" style="transition: none;">' +
			'		<option value="6">TRACE</option>' +
			'		<option value="5">DEBUG</option>' +
			'		<option value="4">INFO</option>' +
			'		<option value="3">WARN</option>' +
			'		<option value="2">ERROR</option>' +
			'		<option value="1">FATAL</option>' +
			'	</select>' +
			'</div>' +
			'<div class="form-group">' +
			'	<label for="extraParams">Extra Start-up Params</label>' +
			'	<input type="text" class="form-control" id="extraParams">' +
			'</div>' +
			'<div class="form-group">' +
			'	<label for="genesisBlock">Genesis Block</label>' +
			'	<textarea class="form-control" rows="5" id="genesisBlock"></textarea>' +
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
			if ( (this.rendered) || _.isEmpty(status.config) ) {
				return;
			}

			this.rendered = true;
			this._$('#networkId').val( status.config.networkId ? status.config.networkId : '' );
			this._$('#identity').val( status.config.identity ? status.config.identity : '' );
			this._$('#logLevel').val( status.config.logLevel ? status.config.logLevel : '4' );
			this._$('#committingTransactions').val( status.config.committingTransactions ? 'true' : 'false' );
			this._$('#extraParams').val( status.config.extraParams ? status.config.extraParams : '' );
			this._$('#genesisBlock').val( status.config.genesisBlock ? status.config.genesisBlock : '' );
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
