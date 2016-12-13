module.exports = function() {
	var extended = {
		name: 'block-view',
		title: 'Find Block / Transaction',
		size: 'small',

		hideLink: true,
		hideRefresh: true,

		template: _.template(
			'  <div class="form-group">' +
			'    <label for="block-id">Identifier [number, hash, tag]</label>' +
			'    <input type="text" class="form-control" id="block-id">' +
			'  </div>'+
			'  <div class="radio">' +
			'    <label>' +
			'      <input type="radio" id="searchType" name="searchType" value="block" checked="checked"/>' +
			'      Block' +
			'    </label>' +
			'  </div>' +
			'  <div class="radio">' +
			'    <label>' +
			'      <input type="radio" id="searchType" name="searchType" value="txn"/>' +
			'      Transaction' +
			'    </label>' +
			'  </div>' +
			'  <div class="form-group pull-right">' +
			'    <button type="button" class="btn btn-primary">Find</button>' +
			'  </div>'+
			'  <div id="notification">' +
			'  </div>'),

		render: function() {
			Dashboard.render.widget(this.name, this.shell.tpl);

			$('#widget-' + this.shell.id)
				.css({
					'height': '240px',
					'margin-bottom': '10px',
					'overflow': 'auto'
				})
				.html( this.template({}) );

			$('#widget-' + this.shell.id + ' button').click(this._handler);
		},

		_handler: function(ev) {
			var _this = widget,
			 id = $('#widget-' + _this.shell.id + ' #block-id'),
			 type = $('#widget-' + _this.shell.id + ' #searchType:checked');

			if (id.val() &&
					( (type.val() == 'block') || (type.val() == 'txn') ) ) {
				Dashboard.show({ widgetId: type.val() + '-detail', section: 'explorer', data: id.val(), refetch: true });
			}
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
