import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'contract-paper-tape',
		title: 'Contract Paper Tape',
		size: 'small',

		url: 'api/contract/transactions/list',
		topic: 'topic/block',

		template: _.template('<table style="width: 100%; table-layout: fixed; background-color: #fcf8e3;" class="table"><%= rows %></table>'),
		templateRow: _.template('<tr style="border-bottom: 2px dotted #faebcc;"><td><%= text %></td></tr>'),
		templateHeader: _.template('<span>[<a href="#" data-widget="block-detail" data-id="<%= txn.blockNumber %>">#<%= txn.blockNumber %></a>]</span>'),

		header: function(txn) {
			return this.templateHeader({txn: txn});
		},

		subscribe: function(data) {
			// subscribe to get updated states
			utils.subscribe(this.topic, this.onUpdatedState.bind(this));
		},

		onUpdatedState: function(data) {
			if (data.data.attributes.transactions.length > 0) {
				this.fetch();
			}
		},

		setData: function(data) {
			this.data = data;

			this.contractName = data.name;
			this.contractId = data.id;
		},

		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: _this.url, data: { address: _this.contractId } })
			).fail(function(err) {
				// TODO: Error will robinson!
				_this.postFetch();
			}).done(function(txns) {
				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html(_this.contractName + ' Paper Tape');

				var rows = [],
				 data = _.sortBy(txns.data,
					function(txn) {
						return parseInt(txn.attributes.blockNumber + '' + txn.attributes.transactionIndex);
					});

				_.each(data, function(val, key) {
					var txn = val.attributes,
					 text = '';

					if (!_.isEmpty(txn.contractAddress)) {
						// Contract creation
						text = _this.header(txn) + ' Contract \'<a href="#" data-widget="contract-detail" data-id="' + txn.contractAddress +'">' + _this.contractName + '</a>\' created by TXN ' +
							'<a href="#" data-widget="txn-detail" data-id="' + txn.id + '">' + utils.truncAddress(txn.id) + '</a>';

					} else if (txn.decodedInput) {
						text = _this.header(txn) + ' TXN <a href="#" data-widget="txn-detail" data-id="' + txn.id + '">' +
									utils.truncAddress(txn.id) + '</a>: <span style="font-weight: bold; color: #375067;">' +
									txn.decodedInput.method +'</span>(<span style="font-weight: bold; color: #375067;">' +
									txn.decodedInput.args.join('</span>, <span style="font-weight: bold; color: #375067;">') +
									'</span>)';

					} else {
						text = _this.header(txn) + ' TXN <a href="#" data-widget="txn-detail" data-id="' + txn.id + '">' +
									utils.truncAddress(txn.id) + '</a>';
					}

					rows.push( _this.templateRow({ text: text }) );
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );

				_this.postFetch();
			});
		},

		postRender: function() {
			$('#widget-' + this.shell.id).on('click', 'a', this._handle);
		},

		_handle: function(e) {
			e.preventDefault();

			var data = $(this).data('id');

			if ($(this).data('widget') === 'contract-detail') {
				data = $(this).data();
			}

			Dashboard.show({ widgetId: $(this).data('widget'), section: 'contracts', data: data, refetch: true });
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
