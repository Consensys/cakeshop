import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'istanbul-validators',
		title: 'Istanbul Validators',
		size: 'medium',

		hideLink: true,
		
		url_validators: 'api/node/peers/istanbul/validator',
		url_candidates: 'api/node/peers/istanbul/candidates',
		url_propose: 'api/node/peers/istanbulPropose',
		url_address: 'api/node/peers/istanbul/nodeAddress',
		url_discard: 'api/node/peers/istanbulDiscard',


		templateValidators: _.template('<div>' +
				'<text>Current Node Address: <%= address %></text>' +
				'</div>' +
				'</br>' +
				'<div>' +
				'<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
				' <thead style="front-weight: bold;">' +
				' <tr>' +
				'	<td style="front-weight: bold;" class="validator"><%= type %></td>' +
				'   <td class="action-col float-right"></td>' +
				' </tr>' +
				' </thead>' +
				'<tbody><%= rows %></tbody>' +
				'</table>' +
				'</div>'),

		templateRowValidators: _.template('<tr>' +
				'	<td class="value validator" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= o %></td>'+
				'   <td data-validator="<%= o %>"class="action-col float-right">' +
				'	<% if (proposal) { %>' +
				'		<% if (keep) { %>' +
				'			<button class="btn btn-default discard-btn">Discard Vote</button>' +
                '			<button class="btn btn-default remove-btn">Remove</button>' +
                '			<button style="background-color:#23AE89; color:white;"class="btn btn-default keep-btn"><%= add %></button>' +
				'		<% } else { %>' +
				'			<button class="btn btn-default discard-btn">Discard Vote</button>' +
                '			<button style="background-color:#E94B3B; color:white;" class="btn btn-default remove-btn">Remove</button>' +
                '			<button class="btn btn-default keep-btn"><%= add %></button>' +
				'		<% } %>' +
				'	<% } else { %>' +
				'		<button class="btn btn-default remove-btn">Propose Removal</button>' +	
				'	<% } %>' +
				'   </td>' +
				'</tr>'),
				
		templateButton: _.template('<div class="form-group pull-right">' +
				'	<button class="btn btn-primary add-btn">Propose New Validator</button>' +
				'</div>'),
				
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
				'	<button type="button" id="add-validator-btn" class="btn btn-primary">Propose Validator</button>' +
				'</div>'),

		fetch: function() {
			var _this = this;
			var address = ""
			$.when(
				utils.load({url: _this.url_address})
			).done(function(addr) {
				address = addr.data.attributes.result
			});

			$.when(
				utils.load({ url: _this.url_validators })
			).fail(function(err) {
				$('#myModal .modal-content').html(_this.modalConfirmation({
					message: err.responseJSON.errors.map((error) => error.detail)
				}) );
			}).done(function(info) {
				$.when(
					utils.load({url: _this.url_candidates })
				).fail(function(err) {
					$('#myModal .modal-content').html(_this.modalConfirmation({
						message: err.responseJSON.errors.map((error) => error.detail)
					}) );
				}).done(function(res) {
					var candidates = []
					var validators = info.data.attributes.result
					var candidateMap = new Map()
					
					var candidateRows = []
					var validatorRows = []
										
					if (!_.isEmpty(res.data.attributes.result)) {
						_.each(res.data.attributes.result, function(auth, prop) {
							candidates.push(prop)
							candidateMap.set(prop, auth)
							const validator = validators.includes(prop)
							if (!validator) {
								candidateRows.push(_this.templateRowValidators({o : prop, keep: auth, add: "Add", proposal: true}))
							}
						});
					} else {
						candidateRows.push("<tr><td/><td>No Proposed Validators</td><td/><td/></tr>")
					}

					if (validators.length > 0) {
						_.each(validators, function(validator) {
							const proposed = candidates.includes(validator)
							const keep = proposed ? candidateMap.get(validator) : false
							validatorRows.push( _this.templateRowValidators({ o: validator, keep: keep, add: "Keep", proposal: proposed}) );
						});
					} else {
						validatorRows.push("<tr><td/><td>No Validators</td><td/><td/></tr>")
					}

					Dashboard.Utils.emit( widget.name + '|fetch|' + JSON.stringify(info.data) );

					$('#widget-' + _this.shell.id).html(_this.templateValidators({ rows: validatorRows.join(''), type: "Validator", address: address }) +
							_this.templateValidators({ rows: candidateRows.join(''), type: "Proposed Validator", address: "" }) +
							_this.templateButton({})
					);	

					utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');
				})
			});

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
			
			$('#widget-' + _this.shell.id).on('click', '.keep-btn', function(e) {
				var validator = $(e.target.parentElement).data("validator")
				$.when(
					utils.load({
						url: _this.url_propose,
						data: {
							"address": validator,
							"istanbulPropose": "true"
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
			
			$('#widget-' + _this.shell.id).on('click', '.discard-btn', function(e) {
	        	 var validator = $(e.target.parentElement).data("validator")
	        	 $.when(
	 					utils.load({
	 						url: _this.url_discard,
	 						data: {
	 							"address": validator,
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
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
