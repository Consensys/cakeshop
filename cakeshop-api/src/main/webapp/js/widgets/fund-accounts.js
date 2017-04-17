import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'fund-accounts',
		title: 'Fund Accounts',
		size: 'small',

		url: 'api/wallet/fund',
		accountList: [],

		hideLink: true,

		template: _.template( '	<div class="form-group fund-accounts-form">' +
		'		<label for="transfer-from">From Account</label>' +
		'		<select id="transfer-from" class="form-control" style="transition: none;"> </select>' +
		'		<label for="transfer-to">To Account</label>' +
		'		<select id="transfer-to" class="form-control" style="transition: none;"> </select>' +
		'		<label for="amount">Amount</label>' +
		'		<input type="text" class="form-control" id="amount">' +
		'	</div>'+
		'	<div class="form-group pull-right">' +
		'		<span class="danger error-msg"></span>' +
		'		<button type="button" class="btn btn-primary" id="transfer-btn">Transfer</button>' +
		'	</div>'+
		'	<div id="notification">' +
		'	</div>'),

		modalTemplate: _.template( '<div class="modal-body">' +
		'	Are you sure you want to transfer <span class="danger"><%=amount%></span> from <%=from%> to <%=to%> ?' +
		'	</div>' +
		'	<div class="modal-footer">' +
		'		<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
		'		<button type="button" id="transfer-btn-final" class="btn btn-primary">Yes, transfer.</button>' +
		'	</div>'),

		modalConfirmation: _.template( '<div class="modal-body"> <%=message %> </div>'),

		subscribe: function() {
			//repopulate account list when new account is added
			// or, if accounts are locked /unlocked
			Dashboard.Utils.on(function(e, action) {
				if (action[0] == 'accountUpdate') {
					this.fetch();
				}
			}.bind(this));
		},

		fetch: function() {
			Account.list().then(function(accounts) {
				var rows = ['<option>Choose Account</option>'];

				accounts.forEach(function(acct) {
					if (acct.get('unlocked')) {
						//only add unlocked accounts
						rows.push( '<option>' + acct.get('address') + '</option>' );
					}
				});

				this._$('#transfer-from')
					.html( rows.join('') );

				this._$('#transfer-to')
					.html( rows.join('') );

			}.bind(this));
		},

		postRender: function() {
			var _this = this;
			$('#widget-' + this.shell.id).html( this.template({}) );

			//populate account dropdowns
			this.fetch();

			$('#widget-' + this.shell.id + ' #transfer-btn').click( function() {
				var from = $('#widget-' + _this.shell.id + ' #transfer-from').val(),
					to = $('#widget-' + _this.shell.id + ' #transfer-to').val(),
					amount = $('#widget-' + _this.shell.id + ' #amount').val();

				//verify that everything is filled out
				if (from == '' || to == '' || amount == '') {
					//error
					$('#widget-' + _this.shell.id + ' .error-msg').html('All fields required.');
				} else {
					//empty error fields just in case
					$('#widget-' + _this.shell.id + ' .error-msg').html('');

					// set the modal text
					$('#myModal .modal-content').html(_this.modalTemplate({
						amount: amount,
						from: from,
						to: to
					}) );

					//open modal
					$('#myModal').modal('show');
				}

				// send transfer request to backend
				$('#transfer-btn-final').click(function() {
					$.when(
						utils.load({
							url: _this.url,
							data: {
								"account": to,
								"accountPassword": '',
								"fromAccount": from,
								"newBalance": amount
							}
						})
					).done(function(m) {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Successfully transferred funds! You may have to wait a moment for the changes to reflect on the balance.'
						}) );
						setTimeout( function(){
								Dashboard.Utils.emit(['fundTransfer'], true)
							}, 6000);
					}).fail(function(err) {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Sorry, transaction did not complete. Please try again.'
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
