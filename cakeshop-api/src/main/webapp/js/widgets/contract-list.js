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

        templateUninitialized: _.template(
            '<div><button class="btn btn-lg btn-primary" id="deploy-registry">Deploy Contract Registry</button></div>'
            +
            '  <br/>' +
            '  <p>OR</p>' +
            '  <div class="form-group">' +
            '    <label for="addy">Use Existing Contract Registry Address</label>'
            +
            '    <input type="text" class="form-control" id="registry-address" placeholder="0xabe8672...">'
            +
            '  </div>' +
            '  <div class="form-group pull-right">' +
            '    <button type="button" class="btn btn-primary" id="use-existing">Use</button>'
            +
            '  </div>'
        ),

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
        updateView: function (res) {
            var _this = this,
                rowsOut = [];

            let contractRegistryAddress = res.data.attributes.result;
            if (contractRegistryAddress === "0x") {
                console.log("No contract registry, showing deploy widget.")
                this.setTitle("Deploy Contract Registry");
                $('#widget-' + this.shell.id).html(
                    this.templateUninitialized());

            } else {
                console.log("Contract registry exists at",
                    contractRegistryAddress, "Loading contracts");
                this.setTitle("Deployed Contract List");
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
            }
        },

        fetch: function () {
            var _this = this;
            $.when(
                utils.load({url: 'api/contract/registry', method: "GET"})
            ).fail(function (res) {
                console.log("Failed to load contract registry address", res);
            }).done(function (res) {
                _this.updateView(res);
            });
		},

		postRender: function() {
            $('#widget-' + this.shell.id).on('click', 'button',
                this._handleButton.bind(this));
		},

		_handleButton: function(e) {
            e.preventDefault();
            var _this = this;

            if (e.target.id === "deploy-registry") {
                $(e.target).attr('disabled', 'disabled');
                $.when(
                    utils.load({url: 'api/contract/registry/deploy', data: {}})
                ).fail(function (res) {
                    console.log("Failed to deploy contract registry", res);
                    alert(res)
                    _this.updateView(res);
                }).done(function (res) {
                    _this.updateView(res);
                })
            } else if (e.target.id === "use-existing") {
                $(e.target).attr('disabled', 'disabled');
                let address = $('#registry-address').val();
                $.when(
                    utils.load({
                        url: 'api/contract/registry/use',
                        data: {address: address}
                    })
                ).fail(function (res) {
                    console.log("Failed to set contract registry address", res);
                    alert(res.responseJSON.data.attributes.result)
                    _this.updateView(res);
                }).done(function (res) {
                    _this.updateView(res);
                })

            } else {
                Dashboard.show({
                    widgetId: $(e.target).data('widget'),
                    section: 'contracts',
                    data: $(e.target).parent().data(),
                    refetch: true
                });
            }
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
