import utils from '../utils'

module.exports = function() {
	var extended = {
		name: 'contract-list',
		title: 'Deployed Contract List',
		size: 'medium',

		hideLink: true,
		topic: '/topic/block',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
            '<thead style="font-weight: bold;"><tr><td style="width: 90px;">ID</td><td>Contract</td><td>Deploy Date</td><td style="width: 272px;">Actions</td></tr></thead>'
            +
		 '<tbody><%= rows %></tbody></table>'),

        templateRow: _.template('<tr>'
            + '<td><%= utils.truncAddress(contract.id) %></td>'
            + '<td><%= contract.name %></td>'
            + '<td><%= contract.date %></td>'
            + '<td data-id="<%= contract.id %>" data-name="<%= contract.name %>">'
            + '<% if (contract.privateFor !== "private") { %>'
            + '<button class="btn btn-primary btn-xs transact" data-widget="contract-transact">Transact</button> <button class="btn btn-primary btn-xs deets" data-widget="contract-detail">Details</button> <button class="btn btn-primary btn-xs tape" data-widget="contract-paper-tape">Paper Tape</button> <button class="btn btn-primary btn-xs state" data-widget="contract-current-state">Current State</button>'
            + '<% } else { %>'
            + 'Private'
            + '<% } %>'
            + '</td></tr>'),

		subscribe: function(data) {
			// subscribe to get updated states
			utils.subscribe(this.topic, this.onUpdatedState.bind(this));

		},

		onUpdatedState: function(data) {
			if (data.data.attributes.transactions.length > 0) {
				this.fetch();
			}
		},

        setTitle: function (title) {
            $('#widget-shell-' + this.shell.id + ' .panel-title span').html(
                title);
        },

        fetch: function () {
            var _this = this,
                rowsOut = [];

            Contract.list(function (contracts) {
                _.each(contracts, function (c) {
                    var co = {
                        name: c.get('name'),
                        date: moment.unix(c.get('createdDate')).format(
                            'YYYY-MM-DD hh:mm A'),
                        id: c.id,
                        privateFor: c.attributes.privateFor,
                    };

                    rowsOut.push(_this.templateRow({contract: co}));
                });
                if (rowsOut.length === 0) {
                    rowsOut.push(
                        "<tr><td/><td>No contracts registered yet.</td><td/><td/></tr>")
                }

                $('#widget-' + _this.shell.id).html(
                    _this.template({rows: rowsOut.join('')}));

                _this.postFetch();
            });
		},

		postRender: function() {
            $('#widget-' + this.shell.id).on('click', 'button',
                this._handleButton.bind(this));
		},

		_handleButton: function(e) {
            e.preventDefault();
            var _this = this;

            Dashboard.show({
                widgetId: $(e.target).data('widget'),
                section: 'contracts',
                data: $(e.target).parent().data(),
                refetch: true
            });
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
