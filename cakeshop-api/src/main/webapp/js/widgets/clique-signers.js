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
				'   <td class="action-col float-right"></td>' +
				' </tr>' +
				' </thead>' +
				'<tbody><%= rows %></tbody>' +
				'</table>' +
				'</div>'),

		templateRowSigners: _.template('<tr>' +
				'	<td class="value signer" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= o %></td>'+
				'   <td data-signer="<%= o %>"class="action-col float-right">' +
				'	<% if (proposal) { %>' +
				'		<% if (keep) { %>' +
				'			<button style="background-color:#23AE89; color:white;"class="btn btn-default keep-btn"><%= add %></button>' +
				'			<button class="btn btn-default remove-btn">Remove</button>' +
				'			<button class="btn btn-default discard-btn">Discard Vote</button>' +
				'		<% } else { %>' +
				'			<button class="btn btn-default keep-btn"><%= add %></button>' +
				'			<button style="background-color:#E94B3B; color:white;" class="btn btn-default remove-btn">Remove</button>' +
				'			<button class="btn btn-default discard-btn">Discard Vote</button>' +
				'		<% } %>' +
				'	<% } else { %>' +
				'		<button class="btn btn-default remove-btn">Propose Removal</button>' +	
				'	<% } %>' +
				'   </td>' +
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
					var signers = info.data.attributes.result
					var proposalMap = new Map()
					
					var proposalRows = []
					var signerRows = []
										
					if (!_.isEmpty(res.data.attributes.result)) {
						_.each(res.data.attributes.result, function(auth, prop) {
							proposals.push(prop)
							proposalMap.set(prop, auth)
							const signer = signers.includes(prop)
							if (!signer) {
								proposalRows.push(_this.templateRowSigners({o : prop, keep: auth, add: "Add", proposal: true}))
							}
						});
					} else {
						proposalRows.push("<tr><td/><td>No Proposed Signers</td><td/><td/></tr>")
					}

					if (signers.length > 0) {
						_.each(signers, function(signer) {
							const proposed = proposals.includes(signer)
							const keep = proposed ? proposalMap.get(signer) : false
							signerRows.push( _this.templateRowSigners({ o: signer, keep: keep, add: "Keep", proposal: proposed}) );
						});
					} else {
						signerRows.push("<tr><td/><td>No Signers</td><td/><td/></tr>")
					}

					Dashboard.Utils.emit( widget.name + '|fetch|' + JSON.stringify(info.data) );

					$('#widget-' + _this.shell.id).html(_this.templateSigners({ rows: signerRows.join(''), type: "Signer" }) +
							_this.templateSigners({ rows: proposalRows.join(''), type: "Proposed Signer" }) +
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