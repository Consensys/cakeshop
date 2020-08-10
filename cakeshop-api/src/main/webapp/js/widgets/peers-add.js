import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'peers-add',
		title: 'Add Peer',
		size: 'small',

		hideLink: true,
		hideRefresh: true,

	    memberUrl: 'api/node/peers/add',
		learnerUrl: 'api/node/peers/addLearner',

		template: _.template(
			'  <div class="radio">' +
            '    <label>' +
            '      <input type="radio" id="role" name="role" value="member" checked="checked"/>' +
            '      Add as Member' +
            '    </label>' +
            '  </div>' +
            '  <div class="radio">' +
            '    <label>' +
            '      <input type="radio" id="role" name="role" value="learner"/>' +
            '      Add as Learner' +
            '    </label>' +
            '  </div>' +
			'  <div class="form-group">' +
			'    <label for="addy">Peer Node Address</label>' +
			'    <input type="text" class="form-control" id="addy">' +
			'  </div>'+
			'  <div class="form-group pull-right">' +
			'    <button type="button" class="btn btn-primary" id="update">Add</button>' +
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

			var type = $('#role:checked').val();
			console.log(type)

			if (!input.val()) {
				return;
			}

			var url;
            switch(type) {
                case "member":
                    url = _this.memberUrl
                    break;
                case "learner":
                    url = _this.learnerUrl
                    break;
                default:
                    url = ""
            }

			$.when(
				utils.load({ url: url, data: { "address": input.val() } })
			).done(function(r) {
				console.log('peers',r)
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
			    console.log(r)
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
