import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'istanbul-validators',
		title: 'Istanbul Validators',
		size: 'medium',

		hideLink: true,

		url: 'api/node/peers/istanbul/validator',
		url_propose: 'api/node/peers/istanbulPropose',
		url_address: 'api/node/peers/istanbul/nodeAddress',


		template: _.template('<div>' +
				'<text>Current Node Address: <%= address %></text>' +
				'</div>' +
				'</br>' +
				'<div>' +
				'<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
				' <thead style="front-weight: bold;">' +
				' <tr>' +
				'	<td class="validator">Validator</td>' +
				'   <td class="remove-col"</td>' +
				' </tr>' +
				' </thead>' +
				'<tbody><%= rows %></tbody>' +
				'</table>' +
				'</div>' +
				'<div class="modal-footer">' +
				'	<button class="btn btn-primary add-btn">Add New Validator</button>' +
				'</div>'),
				
		templateRow: _.template('<tr>' +
				'	<td class="value validator" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= o %></td>'+
				'   <td data-validator="<%= o %>"class="remove-col">' +
				'		<button class="btn btn-default remove-btn">Remove</button>' +
				'   </td>' +
				'</tr>'),
		
		modalConfirmation: _.template('<div class="modal-body"><%=message%></div>'),
		
		modalValidator: _.template( '<div class="modal-header">' +
				'</div>' +
				'<div class="modal-body">' +
				'	<div class="form-group add-org-form">' +
				'		<label for="address">Validator Address</label>' +
				'		<input type="text" class="form-control" id="address" placeholder="Enter validator address">' +
				'	</div>' +
				'</div>' +
				'<div class="modal-footer">' +
				'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
				'	<button type="button" id="add-validator-btn" class="btn btn-primary">Add Validator</button>' +
				'</div>'),

		fetch: function() {
			var _this = this;
			var validators = []
			var address = ""
			$.when(
				utils.load({url: _this.url_address})
			).done(function(addr) {
				address = addr.data.attributes.result
			});
			
			$.when(
				utils.load({ url: _this.url })
			).done(function(info) {
				if (info.data.attributes.result.length > 0) {
					_.each(info.data.attributes.result, function(validator) {
						validators.push( _this.templateRow({ o: validator }) );
					});
				
					
					Dashboard.Utils.emit( widget.name + '|fetch|' + JSON.stringify(info.data) );

					$('#widget-' + _this.shell.id).html( _this.template({ rows: validators.join(''), address: address }) );


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
				var validator = $(e.target.parentElement).data("validator")
				$.when(
					utils.load({
						url: _this.url_propose,
						data: {
							"address": validator,
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
				console.log("hiii")
				
				// set the modal text
				$('#myModal .modal-content').html(_this.modalValidator({}) );

				//open modal
				$('#myModal').modal('show');


                $('#add-validator-btn').click( function() {
                	var validator = $('#address').val();

                	$.when(
                		utils.load({
                			url: _this.url_propose,
                			data: {
                				"address": validator,
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
