import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'block-list',
		title: 'Block List',
		size: 'small',

		url: 'api/block/get',
		topic: '/topic/block',

		hideLink: true,

		lastBlockNum: null,

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
		 '<thead style="font-weight: bold;"><tr><td style="width:60px;">Block</td><td>Age</td><td style="width:45px;">TXNs</td></tr></thead>' +
		 '<tbody><%= rows %></tbody></table>'),

		templateRow: _.template('<tr><td>#<a href="#"><%= block.num %></a></td><td><%= moment.unix(block.age).fromNow() %></td><td <% if (block.txnCount == 0) { %>style="opacity: 0.2;"<% } %>><%= block.txnCount %></td></tr>'),

		setData: function(data) {
			this.data = data;

			this.lastBlockNum = data;
		},

		subscribe: function(data) {
			// subscribe to get new blocks
			utils.subscribe(this.topic, this.onNewBlock);
		},

		onNewBlock: function(data) {
			data = data.data.attributes;

			var b = {
				num: data.number,
				age: data.timestamp,
				txnCount: data.transactions.length,
			};

			$('#widget-' + widget.shell.id + ' > table > tbody').prepend( widget.templateRow({ block: b }) );
		},

		BLOCKS_TO_SHOW: 100,
		fetch: function() {
			try {
				if (this.lastBlockNum != Tower.status.latestBlock) {
					this.lastBlockNum = Tower.status.latestBlock;
				}
			} catch (e) {}

			var displayLimit, promizes = [], rows = [], _this = this;

			if ( (this.lastBlockNum < this.BLOCKS_TO_SHOW) && (this.lastBlockNum >= 0) ) {
				displayLimit = this.lastBlockNum + 1;
			} else {
				displayLimit = this.BLOCKS_TO_SHOW;
			}

			_.times(displayLimit,
				function(n) {
					promizes.push(
						utils.load({
							url: _this.url,
							data: { number: _this.lastBlockNum - n },
							complete: function(res) {
								rows.push( {
									num: res.responseJSON.data.attributes.number,
									age: res.responseJSON.data.attributes.timestamp,
									txnCount: res.responseJSON.data.attributes.transactions.length,
								} );
							}
						})
					);
			 	});

			Promise.all(promizes).then(function() {
				var rowsOut = [];
				rows = _.sortBy(rows, function(o) { return o.num; }).reverse();

				_.each(rows, function(b, index) {
					rowsOut.push( _this.templateRow({ block: b }) );
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rowsOut.join('') }) );

				_this.postFetch();
			});
		},

		postRender: function() {
			$('#widget-' + this.shell.id).on('click', 'a', this.showBlock);
		},

		showBlock: function(e) {
			e.preventDefault();

			Dashboard.show({ widgetId: 'block-detail', section: 'explorer', data: $(this).text(), refetch: true });
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
