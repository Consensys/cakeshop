import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'peers-add',
		title: 'Add Peer',
		size: 'small',

		hideLink: true,
		hideRefresh: true,

		addUrl: 'api/node/peers/add',
        removeUrl: 'api/node/peers/remove',

		template: _.template(
			'  <div class="form-group">' +
			'    <label for="addy">Peer Node Address</label>' +
			'    <input type="text" class="form-control" id="addy">' +
            '    <br/>' +
            '	<% if (Tower.consensus === "raft") { %>' +
            '    <input type="checkbox" id="raftLearner">' +
            '    <label for="raftLearner">Learner Node (View-only)</label>' +
            '	<% } %>' +
			'  </div>'+
			'  <div class="form-group pull-right">' +
			'    <button type="button" class="btn btn-primary" id="peerAdd">Add</button>' +
            '    <button type="button" class="btn btn-secondary" id="peerRemove">Remove</button>' +
			'  </div>'+
			'  <div id="notification">' +
			'  </div>'),

		postRender: function() {
			$('#widget-' + this.shell.id).html( this.template({}) );

			$('#widget-' + this.shell.id + ' button').click(this._handler);
		},

		_handler: function(ev) {

			var _this = widget,
             url = ev.target.id === "peerRemove" ? widget.removeUrl : widget.addUrl,
			 input = $('#widget-' + _this.shell.id + ' #addy'),
             raftLearner = $('#widget-' + _this.shell.id + ' #raftLearner').prop('checked'),
			 notif = $('#widget-' + _this.shell.id + ' #notification');

			if (!input.val()) {
				return;
			}

			$.when(
				utils.load({ url: url, data: { address: input.val(), raftLearner: raftLearner } })
			).done(function(r) {
				notif.show();

					input.val('');

					notif
					 .removeClass('text-danger')
					 .addClass('text-success')
					 .html('Request to add peer is sent');

					setTimeout(function() {
						notif.fadeOut();
					}, 2000);
			}).fail(function(r) {
                notif
                  .addClass('text-danger')
                  .removeClass('text-success')
                  .html(r.responseJSON.errors.map((error) => error.detail));

			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
