import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'permissions-list',
		title: 'Permissions List',
		size: 'medium',

		url: 'api/permissions/getList',
		topic: '/topic/block',

		hideLink: true,

		template: _.template('<div>'
		    + '<table style="width: 100%; table-layout: fixed;" class="table table-striped">'
		    + ' <thead style="font-weight: bold;">'
		    + '     <tr>'
		    + '         <td style="width:50px;">OrgId</td>'
		    + '         <td style="width:20px;">Status</td>'
		    + '         <td style="width:50px;">parentOrgId</td>'
		    + '         <td style="width:50px;">ultimateParent</td>'
		    + '     </tr>'
		    + ' </thead>'
		    + '<tbody><%= rows %></tbody>'
		    + '</table>'
		    + '</div>'
		),

		 templateUninitialized: _.template(
            '<div>'
            + '<h3 style="text-align: center;margin-top: 70px;">Permissioning not enabled</h3>'
            + '</div>'
         ),

		templateRow: _.template('<tr>'
		    + '<td>#<a href="#"><%= o.fullOrgId %></a></td>'
		    + '<td><%= o.status %></td>'
		    + '<td <%= o.parentOrgId %></td>'
		    + '<td><%= o.ultimateParent %></td>'
		    + '</tr>'
		),

        fetch: function () {
			var _this = this;

			$.when(
				utils.load({ url: this.url })
			).done(function(info) {
			    console.log(info)
				var rows = [];
				this.numPeers = info.data.length;

				if (info.data.length > 0) {
					_.each(info.data, function(peer) {
						rows.push( _this.templateRow({ o: peer.attributes }) );
					});


                    $('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );
				}

				$('#widget-' + _this.shell.id + ' a').click(function(e) {
					e.preventDefault();
					console.log($(this).text())

					Dashboard.show({ widgetId: 'permissions-detail', section: 'permissions', data: $(this).text(), refetch: true });
				});

				_this.postFetch();
			}.bind(this));
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
