import utils from '../utils'
import $ from 'jquery'

module.exports = function() {
	var extended = {
		name: 'contract-list',
		title: 'Deployed Contract List',
		size: 'medium',

		hideLink: true,
		topic: '/topic/block',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
            '<thead style="font-weight: bold;"><tr><td style="width: 90px;">ID</td><td>Contract</td><td>Deploy Date</td><td style="width: 310px;">Actions</td></tr></thead>'
            +
		 '<tbody><%= rows %></tbody></table>'),

        templateRow: _.template('<tr>'
            + '<td><%= utils.truncAddress(contract.id) %></td>'
            + '<td><%= contract.name %></td>'
            + '<td><%= contract.date %></td>'
            + '<td data-id="<%= contract.id %>" data-name="<%= contract.name %>">'
            + '<% if (contract.privateFor !== "private") { %>'
            + '<button class="btn btn-primary btn-xs actions transact" data-widget="contract-transact">Transact</button>'
            + '<button class="btn btn-primary btn-xs actions deets" data-widget="contract-detail">Details</button>'
            + '<button class="btn btn-primary btn-xs actions state" data-widget="contract-current-state">Current State</button>'
              + '<% if (contract.details) { %>'
                + '<a href="<%= contract.details %>" class="btn btn-primary btn-xs actions" target="_blank">View in Reporting</a>'
              + '<% } else if (window.reportingEndpoint) { %>'
                + '<button class="btn btn-primary btn-xs actions reporting" data-widget="contract-reporting">Add to Reporting</button>'
              + '<% } %>'
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
                        address: c.get('address'),
                        details: c.get('details'),
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
            $(e.target).prop('disabled', true)
            const widgetId = $(e.target).data('widget')
            if(widgetId === 'contract-reporting') {
                Contract.get($(e.target).parent().data('id')).done(function(res) {
                    const contract = res.attributes
                    if(contract.storageLayout) {
                        Contract.register(contract.address).then((res) => {
                            console.log("Successfully add new contract to reporting: ", res);
                            _this.fetch()
                        }).catch((res) => {
                            console.log("Failed to register new contract abi: ", res);
                            alert("Failed to register contract in the Reporting Tool")
                            $(e.target).prop('disabled', false)
                        })
                    } else {
                        alert('Reporting Tool only supports contracts compiled with solc version 0.6.5 or higher. Before this version, the compiler does not provide the required Storage Layout for the contract.')
                        $(e.target).prop('disabled', false)
                    }
                })

            } else {
                Dashboard.show({
                    widgetId: widgetId,
                    section: 'contracts',
                    data: $(e.target).parent().data(),
                    refetch: true
                });
            }
		},

	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
