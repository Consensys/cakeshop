module.exports = function() {
	var extended = {
		name: 'contract-list',
		title: 'Deployed Contract List',
		size: 'medium',

		hideLink: true,
		topic: '/topic/block',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
		 '<thead style="font-weight: bold;"><tr><td style="width: 90px;">ID</td><td>Contract</td><td>Deploy Date</td><td style="width: 210px;">Actions</td></tr></thead>' +
		 '<tbody><%= rows %></tbody></table>'),

		templateRow: _.template('<tr><td><%= utils.truncAddress(contract.id) %></td><td><%= contract.name %></td><td><%= contract.date %></td><td data-id="<%= contract.id %>" data-name="<%= contract.name %>"><button class="btn btn-primary btn-xs deets" data-widget="contract-detail">Details</button> <button class="btn btn-primary btn-xs tape" data-widget="contract-paper-tape">Paper Tape</button> <button class="btn btn-primary btn-xs state" data-widget="contract-current-state">Current State</button></td></tr>'),

		subscribe: function(data) {
			// subscribe to get updated states
			utils.subscribe(this.topic, this.onUpdatedState.bind(this));

		},

		onUpdatedState: function(data) {
			if (data.data.attributes.transactions.length > 0) {
				this.fetch();
			}
		},

		fetch: function() {
			var _this = this,
			 rowsOut = [];

			Contract.list(function(contracts) {
				_.each(contracts, function(c) {
					var co = {
						name: c.get('name'),
						date: moment.unix(c.get('createdDate')).format('YYYY-MM-DD hh:mm A'),
						id: c.id
					};

					rowsOut.push( _this.templateRow({ contract: co }) );
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rowsOut.join('') }) );

				_this.postFetch();
	        });
		},

		postRender: function() {
			$('#widget-' + this.shell.id).on('click', 'button', this._handleButton);
		},

		_handleButton: function(e) {
			e.preventDefault();

			Dashboard.show({ widgetId: $(this).data('widget'), section: 'contracts', data: $(this).parent().data(), refetch: true });
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
