import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'clique-signers',
		title: 'Clique Signers',
		size: 'medium',

		hideLink: true,

		url: 'api/node/peers/clique/signers',
		url_propose: 'api/node/peers/cliquePropose',


		template: _.template('<div>' +
				'<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
				' <thead style="front-weight: bold;">' +
				' <tr>' +
				'	<td class="signer">Signer</td>' +
				'   <td class="remove-col"</td>' +
				' </tr>' +
				' </thead>' +
				'<tbody><%= rows %></tbody>' +
				'</table>' +
				'</div>' +
				'<div class="modal-footer">' +
				'	<button class="btn btn-primary add-btn">Propose New Signer</button>' +
				'</div>'),

		templateRow: _.template('<tr>' +
				'	<td class="value signer" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= o %></td>'+
				'   <td data-signer="<%= o %>"class="remove-col">' +
				'		<button class="btn btn-default remove-btn">Remove</button>' +
				'   </td>' +
				'</tr>'),

		modalConfirmation: _.template('<div class="modal-body"><%=message%></div>'),

		modalSigner: _.template( '<div class="modal-header">' +
				'</div>' +
				'<div class="modal-body">' +
				'	<div class="form-group add-org-form">' +
				'		<label for="address">Signer Address</label>' +
				'		<input type="text" class="form-control" id="address" placeholder="Enter signer address">' +
				'	</div>' +
				'</div>' +
				'<div class="modal-footer">' +
				'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
				'	<button type="button" id="add-signer-btn" class="btn btn-primary">Propose Signer</button>' +
				'</div>'),

		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: _this.url })
			).done(function(info) {
				if (info.data.attributes.result.length > 0) {
					var signers = []
					_.each(info.data.attributes.result, function(signer) {
						signers.push( _this.templateRow({ o: signer }) );
					});


					Dashboard.Utils.emit( widget.name + '|fetch|' + JSON.stringify(info.data) );

					$('#widget-' + _this.shell.id).html( _this.template({ rows: signers.join('') }) );


					utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');
				} else {
					// no peers
					$('#widget-' + _this.shell.id).html('No Candidates');
				}
			}.bind(this));

			_this.postFetch();
		},

		postRender: function() {
			var _this = this;

			$('#widget-' + _this.shell.id).on('click', '.remove-btn', function(e) {
				var signer = $(e.target.parentElement).data("signer")
				$.when(
					utils.load({
						url: _this.url_propose,
						data: {
							"address": signer,
							"istanbulPropose": "false"
						}
					})
				).done(function () {
					_this.fetch();
				}).fail(function(err) {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: err.responseJSON.errors.map((error) => error.detail)
						}) );
				});
			});

			$('#widget-' + _this.shell.id).on('click', '.add-btn', function(e) {

				// set the modal text
				$('#myModal .modal-content').html(_this.modalSigner({}) );

				//open modal
				$('#myModal').modal('show');


                $('#add-signer-btn').click( function() {
                	var signer = $('#address').val();

                	$.when(
                		utils.load({
                			url: _this.url_propose,
                			data: {
                				"address": signer,
                				"istanbulPropose": "true"
                			}
                		})
                	).done(function () {
                		$('#myModal').modal('hide');
                		_this.fetch();
                	}).fail(function(err) {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: err.responseJSON.errors.map((error) => error.detail)
						}) );
                	});
                });
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};