import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'txn-detail',
		size: 'medium',

		url: 'api/transaction/get',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),
		templateRow: _.template('<tr><td style="width: 160px;"><%= key %></td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= value %></td></tr>'),
		templateBlockRow: _.template('<tr><td style="width: 160px;"><%= key %></td><td style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><a href="#"><%= value %></a></td></tr>'),


		setData: function(data) {
			this.data = data;
			this.txnAddy = data;

			this.title = 'Transaction #' + this.txnAddy;
		},

		fetch: function() {
			var _this = this;

			$.when(
                function () {
                    // check if reporting engine is used
                    if (window.reportingEndpoint) {
                        console.log("reporting engine fetch: " + window.reportingEndpoint);
                        return utils.load({ url: window.reportingEndpoint, data: {"jsonrpc":"2.0","method":"reporting.GetTransaction","params":[_this.txnAddy],"id":99} })
                    } else {
                        console.log("default fetch");
                        return utils.load({ url: _this.url, data: { id: _this.txnAddy } })
                    }
                }()
			).fail(function(res) {
				$('#widget-' + _this.shell.id).html( '<h3 style="text-align: center;margin-top: 70px;">Unable to load transaction</h3>' );

				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html('Transaction Detail');

				_this.postFetch();
			}).done(function(res) {
                if (window.reportingEndpoint) {
                    if (res.error) {
                        console.log(res.error);
                        $('#widget-' + _this.shell.id).html( '<h3 style="text-align: center;margin-top: 70px;">Unable to load transaction</h3>' );
                        $('#widget-shell-' + _this.shell.id + ' .panel-title span').html('Transaction Detail');
                        return
                    }
                    // reformat response data
                    res = {
                        data: {
                            id: res.result.rawTransaction.hash,
                            type: "transaction",
                            attributes: _.extend(res.result.rawTransaction, {
                                txSig: res.result.txSig,
                                func4Bytes: res.result.func4Bytes,
                                parsedData: JSON.stringify(res.result.parsedData),
                                parsedEvents: JSON.stringify(res.result.parsedEvents.map( e => ({eventSig: e.eventSig, parsedData: e.parsedData}))),
                                blockId: res.result.rawTransaction.blockHash,
                                contractAddress: res.result.rawTransaction.createdContract,
                                transactionIndex: res.result.rawTransaction.index,
                                // parse raw events JSON
                                events: JSON.stringify(res.result.rawTransaction.events)
                            })
                        }
                    }
                }
                // console.log("res: " + JSON.stringify(res))
				var mainTable = ['id', 'status', 'blockId', 'blockNumber', 'contractAddress', 'transactionIndex', 'gasUsed', 'cumulativeGasUsed'],
                reportingTable = ['txSig', 'func4Bytes', 'parsedData', 'parsedEvents'],
				secTable = ['from', 'to', 'value', 'input', 'decodedInput', 'logs', 'gas', 'gasPrice', 'nonce'],
				keyOrder = _.reduce(mainTable.concat(secTable), function(m, v, i) { m[v] = i; return m; }, {}),
				mainRows = [],
                reportingRow = [],
				secRows = [],
				keys = _.keys(res.data.attributes).sort(function(a, b) {
					// custom reorder of the returned keys
					if (keyOrder[a] === keyOrder[b]) {
						return 0;
					} else if (keyOrder[a] < keyOrder[b]) {
						return -1;
					} else if (keyOrder[a] > keyOrder[b]) {
						return 1;
					}
					return 0;
				});

				keys = utils.idAlwaysFirst(keys);
				_.each(keys, function(val, key) {
					if (res.data.attributes[val] == null) {
						return;
					}

					var template;

					if (val == 'blockNumber') {
						template = _this.templateBlockRow({ key: utils.camelToRegularForm(val), value: res.data.attributes[val] });
					} else if ( (val === 'decodedInput') || (val === 'logs') ) {
						template = _this.templateRow({ key: utils.camelToRegularForm(val), value: JSON.stringify(res.data.attributes[val]) });
					} else {
						template = _this.templateRow({ key: utils.camelToRegularForm(val), value: res.data.attributes[val] });
					}


					if (_.contains(mainTable, val)) {
						mainRows.push( template );
					} else if (_.contains(reportingTable, val)) {
                        reportingRow.push( template );
                    } else {
						secRows.push( template );
					}
				});

				if (window.reportingEndpoint) {
                    $('#widget-' + _this.shell.id).html( _this.template({ rows: mainRows.join('') }) +
                        '<h3 style="margin-top: 30px;margin-left: 8px;">Reporting Engine Parsed Data</h3>' +
                        _this.template({ rows: reportingRow.join('') }) +
                        '<h3 style="margin-top: 30px;margin-left: 8px;">Other Transaction Inputs &amp; Parameters</h3>' +
                        _this.template({ rows: secRows.join('') }) );
                } else {
                    $('#widget-' + _this.shell.id).html( _this.template({ rows: mainRows.join('') }) +
                        '<h3 style="margin-top: 30px;margin-left: 8px;">Transaction Inputs &amp; Parameters</h3>' +
                        _this.template({ rows: secRows.join('') }) );
                }

				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html(_this.title);

				utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');

				_this.postFetch();
			});
		},

		postRender: function() {
			$('#widget-' + this.shell.id).on('click', 'a', function(e) {
				e.preventDefault();

				Dashboard.show({ widgetId: 'block-detail', section: 'explorer', data: $(this).text(), refetch: true });
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
