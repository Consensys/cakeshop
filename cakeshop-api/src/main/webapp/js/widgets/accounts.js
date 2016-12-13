import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'accounts',
		title: 'Accounts',
		size: 'medium',

		url: 'api/wallet/list',
		url_create: 'api/wallet/create',

		hideLink: true,
		customButtons: '<li><i class="add-account fa fa-plus-circle"></i></li>',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
		 '<thead style="font-weight: bold;"><tr><td>Account</td><td style="width: 200px;">Balance</td></tr></thead>' +
		 '<tbody><%= rows %></tbody></table>'),

		templateRow: _.template('<tr><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= o.get("address") %></td><td style="width: 200px;"><%= o.balance %></td></tr>'),

		fetch: function() {
			var _this = this;
			Account.list().then(function(accounts) {
				var rows = [];
				accounts.forEach(function(acct) {
					var b = parseInt(acct.get('balance'), 10) / 1000000000000000000;

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

		postRender: function() {
			var _this = this;
			$('#widget-shell-' + _this.shell.id + ' i.add-account').click(function(e) {
				$.when(
					utils.load({ url: _this.url_create })
				).done(function() {
					$(e.target).parent().parent().find('.fa-rotate-right').click();
				});

			});
		}
	};

	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
