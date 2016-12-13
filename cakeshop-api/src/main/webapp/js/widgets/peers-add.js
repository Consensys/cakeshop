import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'peers-add',
		title: 'Add Peer',
		size: 'small',

		hideLink: true,
		hideRefresh: true,

		url: 'api/node/peers/add',

		template: _.template(
			'  <div class="form-group">' +
			'    <label for="addy">Peer Node Address</label>' +
			'    <input type="text" class="form-control" id="addy">' +
			'  </div>'+
			'  <div class="form-group pull-right">' +
			'    <button type="button" class="btn btn-primary" id="restart">Add</button>' +
			'  </div>'+
			'  <div id="notification">' +
			'  </div>'),

		postRender: function() {
			$('#widget-' + this.shell.id).html( this.template({}) );

			$('#widget-' + this.shell.id + ' button').click(this._handler);
		},

		_handler: function(ev) {
			var _this = widget,
			 input = $('#widget-' + _this.shell.id + ' #addy'),
			 notif = $('#widget-' + _this.shell.id + ' #notification');

			if (!input.val()) {
				return;
			}

			$.when(
				utils.load({ url: widget.url, data: { "address": input.val() } })
			).done(function(r) {
				console.log('peers',r)
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
					 .html('Request to add peer is sent');

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
