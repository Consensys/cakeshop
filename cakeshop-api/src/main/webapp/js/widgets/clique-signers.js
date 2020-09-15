import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'clique-signers',
		title: 'Clique Signers',
		size: 'medium',

		hideLink: true,

		url_signers: 'api/node/peers/clique/signers',
		url_proposals: 'api/node/peers/clique/proposals',
		url_propose: 'api/node/peers/cliquePropose',
		url_discard: 'api/node/peers/cliqueDiscard',


		templateSigners: _.template('<div>' +
				'<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
				' <thead style="front-weight: bold;">' +
				' <tr>' +
				'	<td style="front-weight: bold;" class="signer"><%= type %></td>' +
				'   <td class="vote-col"</td>' +
				'   <td class="remove-col"</td>' +
				' </tr>' +
				' </thead>' +
				'<tbody><%= rows %></tbody>' +
				'</table>' +
				'</div>'),

		templateRowSigners: _.template('<tr>' +
				'	<td class="value signer" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= o %></td>'+
				'<% if (buttons) { %>' +		
				'   	<td data-signer="<%= o %>"class="vote-col">' +
				'	<% if (proposed) { %>' +
				'			<button class="btn btn-default keep-btn">Add</button>' +
				'			<button style="background-color:red;" class="btn btn-default remove-btn">Remove</button>' +
				'	<% } %>' +
				'   	</td>' +
				'   <td data-signer="<%= o %>"class="remove-col">' +
				'	<% if (proposed) { %>' +
				'		<button class="btn btn-default discard-btn">Discard Vote</button>' +
				'	<% } else { %>' +
				'		<button class="btn btn-default remove-btn">Remove</button>' +
				'	<% } %>' +
				'   </td>' +
				'<% } %>' +
				'</tr>'),
				
		templateRowProposals: _.template('<tr>' +
				'	<td class="value signer" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= o %></td>'+
				'<% if (buttons) { %>' +
				'   <td data-signer="<%= o %>"class="vote-col">' +
				'		<button style="background-color:green;" class="btn btn-default keep-btn">Add</button>' +
				'		<button class="btn btn-default remove-btn">Remove</button>' +
				'   </td>' +
				'   <td data-signer="<%= o %>"class="remove-col">' +
				'		<button class="btn btn-default discard-btn">Discard Vote</button>' +
				'   </td>' +
				'<% } %>' +
				'</tr>'),
				
		templateButton: _.template('<div class="form-group pull-right">' +
				'	<button class="btn btn-primary add-btn">Propose New Signer</button>' +
				'</div>'),
				

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
				utils.load({ url: _this.url_signers })
			).fail(function(err) {
				$('#myModal .modal-content').html(_this.modalConfirmation({
					message: err.responseJSON.errors.map((error) => error.detail)
				}) );
			}).done(function(info) {
				$.when(
					utils.load({url: _this.url_proposals })
				).fail(function(err) {
					$('#myModal .modal-content').html(_this.modalConfirmation({
						message: err.responseJSON.errors.map((error) => error.detail)
					}) );
				}).done(function(res) {
					var proposals = []
					var proposedSigners = []
					if (!_.isEmpty(res.data.attributes.result)) {
						_.each(res.data.attributes.result, function(auth, prop) {
							proposals.push(prop)
							proposedSigners.push(_this.templateRowProposals({o : prop, buttons: true}))
						});
					} else {
						proposedSigners.push(_this.templateRowProposals({o : "No Proposed Signers", buttons: false}))
					}

					var signers = []
					if (info.data.attributes.result.length > 0) {
						_.each(info.data.attributes.result, function(signer) {
							const proposed = proposals.includes(signer)
							signers.push( _this.templateRowSigners({ o: signer, proposed: proposed, buttons: true }) );
						});
					} else {
						signers.push(_this.templateRowSigners({ o : "No Signers", buttons: false}))
					}

					Dashboard.Utils.emit( widget.name + '|fetch|' + JSON.stringify(info.data) );

					$('#widget-' + _this.shell.id).html(_this.templateSigners({ rows: signers.join(''), type: "Signer" }) +
							_this.templateSigners({ rows: proposedSigners.join(''), type: "Proposed Signer" }) +
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
			
			$('#widget-' + _this.shell.id).on('click', '.keep-btn', function(e) {
				var signer = $(e.target.parentElement).data("signer")
				$.when(
					utils.load({
						url: _this.url_propose,
						data: {
							"address": signer,
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
			
			$('#widget-' + _this.shell.id).on('click', '.discard-btn', function(e) {
	        	 var signer = $(e.target.parentElement).data("signer")
	        	 $.when(
	 					utils.load({
	 						url: _this.url_discard,
	 						data: {
	 							"address": signer,
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