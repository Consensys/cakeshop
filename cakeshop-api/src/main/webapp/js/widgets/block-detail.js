import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'block-detail',
		size: 'medium',

		url: 'api/block/get',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),
		templateRow: _.template('<tr><td style="width: 100px;"><%= key %></td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= value %></td></tr>'),
		templateTxnRow: _.template('<tr><td style="width: 100px;"><%= key %></td><td style="text-overflow: ellipsis; overflow: hidden;"><%= value %></td></tr>'),

		setData: function(data) {
			this.data = data;
			this.blockNumber = data;

			this.title = 'Block #' + this.blockNumber;
		},

		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: this.url, data: { number: parseInt(_this.blockNumber, 10) } })
			).fail(function(res) {
				$('#widget-' + _this.shell.id).html( '<h3 style="text-align: center;margin-top: 70px;">Unable to load block</h3>' );

				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html('Block Detail');

				_this.postFetch();
			}).done(function(res) {
				var rows = [],
				 keys = _.sortBy(_.keys(res.data.attributes), function(key) {
					// custom reorder of the returned keys
				  var customOrder = {
					  'number': 1,
					  'timestamp': 2,
					  'transactions': 3
				  };

				  if (key in customOrder) {
					  return '' + customOrder[key];
				  }

				  return ('zzz' + key);
			   });

				keys = utils.idAlwaysFirst(keys);

				if (keys.indexOf('timestamp') >= 0) {
					res.data.attributes.timestamp = moment.unix(res.data.attributes.timestamp).format('hh:mm:ss A MM/DD/YYYY') + ' (' + moment.unix(res.data.attributes.timestamp).fromNow() + ')' ;
				}

				_.each(keys, function(val, key) {
					if ( (!res.data.attributes[val]) || (res.data.attributes[val].length == 0) ) {
						return;
					}

					if (val == 'transactions') {
						var txnHtml = [];

						_.each(res.data.attributes[val], function(txn) {
							txnHtml.push('<a href="#">' + txn  + '</a>')
						});

						rows.push( _this.templateTxnRow({ key: utils.camelToRegularForm(val), value: txnHtml.join('<br/>') }) );
					} else {
						rows.push( _this.templateRow({ key: utils.camelToRegularForm(val), value: res.data.attributes[val] }) );
					}
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );
				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html(_this.title);

				utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');

				$('#widget-' + _this.shell.id + ' a').click(function(e) {
					e.preventDefault();

					Dashboard.show({ widgetId: 'txn-detail', section: 'explorer', data: $(this).text(), refetch: true });
				});

				_this.postFetch();
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
