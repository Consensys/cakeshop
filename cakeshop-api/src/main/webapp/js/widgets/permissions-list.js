import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'permissions-list',
		title: 'Permissions List',
		size: 'medium',

		url: 'api/block/get',
		topic: '/topic/block',

		hideLink: true,

		lastBlockNum: null,

		template: _.template('<div>'
		    + '<table style="width: 100%; table-layout: fixed;" class="table table-striped">'
		    + '<thead style="font-weight: bold;"><tr><td style="width:60px;">OrgId</td><td style="width:60px;">Status</td><td style="width:60px;">parentOrgId</td><td style="width:60px;">ultimateParent</td></tr></thead>'
		    + '<tbody><%= rows %></tbody></table>'
		    //+ '<div id="register-dialog" style="width: 100%;"/>'
		    + '</div>'
		),

		 templateUninitialized: _.template(
            '<div>'
            + '<h3 style="text-align: center;margin-top: 70px;">Permissioning not enabled</h3>'
            + '</div>'
         ),

		templateRow: _.template('<tr>'
		    + '<td>#<a href="#"><%= block.num %></a></td>'
		    + '<td><%= block.age === 0 ? "Genesis" : moment(block.age).fromNow() %></td>'
		    + '<td <% if (block.txnCount == 0) { %>style="opacity: 0.2;"<% } %>><%= block.txnCount %></td>'
		    + '<td>#<a href="#"><%= block.num %></a></td>'
		    + '</tr>'
		),

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
				age: utils.convertTimestampToMillis(data.timestamp),
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
									age: utils.convertTimestampToMillis(res.responseJSON.data.attributes.timestamp),
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
