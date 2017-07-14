import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'node-control',
		title: 'Node Control',
		size: 'small',

		hideLink: true,
		hideRefresh: true,

		url: {
			nodeControl: 'api/node',
		},

		template: _.template('<ul class="widget-node-control">'+
				'<li><button type="button" class="btn btn-default ctrls" id="restart">Restart Node</button></li>'+
				'<li><button type="button" class="btn btn-default ctrls" id="stop">Stop Node</button></li>'+
				'<li><button type="button" class="btn btn-default ctrls" id="start">Start Node</button></li>'+
				'<li><button type="button" class="btn btn-default" id="confirm" data-toggle="modal" data-target="#newchainconfirm">Create New Chain</button></li>'+
				'<li><button type="button" class="btn btn-default ctrls quorum-control" id="constellation/stop">Stop Constellation</button></li>'+
				'<li><button type="button" class="btn btn-default ctrls quorum-control" id="constellation/start">Start Constellation</button></li>'+
			'</ul>' +

			'<div class="modal fade" id="newchainconfirm" tabindex="-1" role="dialog">'+
				'<div class="modal-dialog" role="document">'+
					'<div class="modal-content">'+
						'<div class="modal-header">'+
							'<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+
							'<h4 class="modal-title">Create New Chain</h4>'+
						'</div>'+
						'<div class="modal-body">'+
							'<p>WARNING: Creating a new chain will delete the existing one. <span class="text-danger">ALL existing contracts and data will be lost!<span></p>' +
							'<p>Are you sure you wish to continue?</p>'+
						'</div>'+
						'<div class="modal-footer">'+
							'<button type="button" class="btn btn-default" data-dismiss="modal">No</button>'+
							'<button type="button" class="btn btn-primary ctrls" id="reset" data-dismiss="modal">Yes, Create</button>'+
						'</div>'+
					'</div>'+
				'</div>'+
			'</div>'),

		postRender: function() {
			this._$().html(widget.template({}));
			this._$('.ctrls').click(widget._handler);
		},

		_handler: function(ev) {
			var _this = $(this),
			 action = $(this).attr('id');

			$(this).attr('disabled', 'disabled');

			$.when(
				utils.load({ url: widget.url.nodeControl + '/' + action })
			).done(function() {
				_this.removeAttr('disabled');
			}).fail(function() {
				// TODO: fill in
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence
	Dashboard.addWidget(widget);
};
