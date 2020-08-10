import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'peers-list',
		title: 'Peer List',
		size: 'medium',
		numPeers: 0,

		hideLink: true,

		url: 'api/node/peers',
		promoteUrl: 'api/node/peers/promote',
		removeUrl: 'api/node/peers/remove',
		topic: '/topic/node/status',

		template: _.template('<div>' +
		    '<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
		    ' <thead style ="front-weight: bold;">' +
		    '   <tr>' +
		    '       <td class="enode">Enode</td>' +
		    '       <td class="name">Name</td>' +
		    '       <td class="ip">Ip</td>' +
		    '<% if (consensus == "raft") { %>' +
		    '       <td class="raftId">Raft Id</td>' +
		    '       <td class="role">Role</td>' +
		    '       <td class="promote-col"></td>' +
		    '<% } %>' +
		    '       <td class="remove-col"></td>' +
		    '   </tr>' +
		    ' </thead>' +
		    '<tbody><%= rows %></tbody>' +
		    '</table>'+
		    '</div>'),

		templateRow: _.template('<tr>' +
		    '   <td class="value enode" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= o.nodeUrl %></td>' +
		    '   <td class="value name"><%= o.nodeName %></td>' +
		    '   <td class="value ip"><%= o.nodeIP %></td>' +
		    '<% if (consensus == "raft") { %>' +
		    '       <td class="value raftId"><%= o.raftId %></td>' +
		    '       <td class="value role"><%= o.role %></td>' +
		    '       <td data-enode="<%= o.nodeUrl %>" class="promote-col">' +
            '       <button class="btn btn-default promote-btn" <%= o.role == "learner" ? "" : "disabled" %>>Promote</button>' +
            '       </td>' +
		    '<% } %>' +
		    '       <td data-enode="<%= o.nodeUrl %>" class="remove-col">' +
            '       <button class="btn btn-default remove-btn">Remove</button>' +
            '       </td>' +
		    '</tr>'),


		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: this.url })
			).done(function(info) {
				var rows = [];
				this.numPeers = info.data.length;

				if (info.data.length > 0) {
					_.each(info.data, function(peer) {
						rows.push( _this.templateRow({ o: peer.attributes, consensus: "raft" }) );
					});

					Dashboard.Utils.emit( widget.name + '|fetch|' + JSON.stringify(info.data) );

					$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join(''), consensus: "raft" }) );

					utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');
				} else {
					// no peers
					$('#widget-' + _this.shell.id).html('');
				}

				_this.postFetch();
			}.bind(this));
		},

		postRender: function() {
            var _this = this;

            $('#widget-' + _this.shell.id).on('click', '.promote-btn', function(e) {
                var enode = $(e.target.parentElement).data("enode");

                $.when(
				    utils.load({ url: _this.promoteUrl, data: { "address": enode } })
			    ).done(function(r) {

			    }).fail(function(r) {
			        console.log(r)
                });
            });

            $('#widget-' + _this.shell.id).on('click', '.remove-btn', function(e) {
                var enode = $(e.target.parentElement).data("enode");

                $.when(
				    utils.load({ url: _this.removeUrl, data: { "address": enode } })
			    ).done(function(r) {
                    console.log('success')
			    }).fail(function(r) {
			        console.log(r)
                });
            });
        },

		subscribe: function() {
			utils.subscribe(this.topic, this.updatePeers.bind(this));
		},

		updatePeers: function(response) {
			if (response.data.attributes.peerCount != this.numPeers) {
				this.fetch();
			}
		}

	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
