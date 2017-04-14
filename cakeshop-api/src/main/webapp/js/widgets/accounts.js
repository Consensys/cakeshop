import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'accounts',
		title: 'Accounts',
		size: 'medium',

		url_list: 'api/wallet/list',
		url_create: 'api/wallet/create',
		url_lock: 'api/wallet/lock',
		url_unlock: 'api/wallet/unlock',

		hideLink: true,
		customButtons: '<li><i class="add-account fa fa-plus-circle"></i></li>',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
		 	'	<thead style="font-weight: bold;">' +
			'		<tr>' +
			'			<td class="unlocked-col locked"></td>' +
			'			<td class="account-name">Account</td>' +
			'			<td class="account-balance"><span>Balance</span></td>' +
			'			<td class="locking-col"></td>'+ //for buttons
			'		</tr>' +
			'	</thead>' +
		 	'	<tbody> <%= rows %> </tbody>' +
		 	'</table>' +
			'<div class="form-group pull-right">' +
			'	<button class="btn btn-primary add-account">New Account</button>' +
			'</div>'),

		templateRow: _.template('<tr>' +
				'<td class="unlocked-col" >' +
					'<% if( !o.unlocked ){ %><i class="fa fa-lock locked-icon" aria-hidden="true"><% } %></i>' +
				'</td>' +
				'<td class="value account-name" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= o.address %></td>' +
				'<td class="account-balance"><span><%= o.balance %></span></td>' +
				'<td data-account="<%= o.address %>" class="locking-col">' +
					'<button class="btn btn-default locking-btn <% if( o.unlocked ){ %>">Lock<% } else { %>locked">Unlock<% } %>' +
					'</button>' +
				'</td>' +
			'</tr>'),

		modalTemplate: _.template( '<div class="modal-header">' +
			'	<%=lock%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group lock-accounts-form">' +
			'		<label for="account-label">Account</label>' +
			'		<input type="text" class="form-control" id="account-label" readonly="readonly" placeholder="<%=account%>">' +
			'		<label for="account-pwd">Password</label>' +
			'		<input type="password" class="form-control" id="account-pwd">' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
			'	<button type="button" id="locking-btn-final" class="btn btn-primary">Yes, <%=lock%>.</button>' +
			'</div>'),

		modalConfirmation: _.template('<div class="modal-body"><%=message%></div>'),

		fetch: function() {
			var _this = this;
			$.when(
				utils.load({ url: _this.url_list })
			).done(function(accounts) {
				var rows = [];
				_.each(accounts.data, function(acct) {
					acct = acct.attributes;
					var b = parseInt(acct.balance, 10);

					if (b > 1000000000) {
						b = 'Unlimited';
					} else {
						b = b.toFixed(2);
					}

					acct.balance = b + ' ETH';
					rows.push( _this.templateRow({ o: acct }) );
				});
				$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );
				utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');
			});
		},

		subscribe: function() {
			//fetch when account funds are transferred
			Dashboard.Utils.on(function(e, action) {
				if (action[0] == 'fundTransfer') {
					this.fetch();
				}
			}.bind(this));
		},

		postRender: function() {
			var _this = this;
			$('#widget-shell-' + _this.shell.id + ' .add-account').click(function(e) {
				$.when(
					utils.load({ url: _this.url_create })
				).done(function() {
					Dashboard.Utils.emit(['accountUpdate'], true);
					_this.fetch();
				});

			});

			$('#widget-' + _this.shell.id).on('click', '.locking-btn', function(e) {
				var account = $(e.target.parentElement).data("account"),
				 url = _this.url_lock,
				 lock = 'lock';

				if ($(e.target).hasClass('locked')) {
					url = _this.url_unlock;
					lock = 'unlock';
				}

				// set the modal text
				$('#myModal .modal-content').html(_this.modalTemplate({
					account: account,
					lock: lock
				}) );

				//open modal
				$('#myModal').modal('show');

				$('#locking-btn-final').click( function() {
					var pwd = $('#account-pwd').val();

					$.when(
						utils.load({
							url: url,
							data: {
								"account": account,
								"accountPassword": pwd,
								"fromAccount": "",
								"newBalance": ""
							}
						})
					).done(function () {
						if($(e.target).hasClass('locked') ) {
							$(e.target).removeClass('locked');
						} else {
							$(e.target).addClass('locked');
						}
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['accountUpdate'], true)
						_this.fetch();

					}).fail(function() {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Sorry, could not ' + lock + ' account. Please try again.'
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
