import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'permissions-detail',
		title: 'Permissions Detail',
		size: 'medium',



		url: 'api/permissions/getDetails',

		hideLink: true,

		setData: function(data) {
			this.data = data;
			this.orgDet = data;

			this.title = 'Org #' + this.orgDet;
		},

		templateNode: _.template('<div>' +
            '<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
		 	'	<thead style="font-weight: bold;">' +
			'		<tr>' +
			'           <td style="width:50px;">OrgId</td>' +
			'           <td style="width:20px;">Status</td>' +
			'           <td style="width:150px;">Url</td>' +
			'		</tr>' +
			'	</thead>' +
		 	'	<tbody> <%= nodeRows %> </tbody>' +
		 	'</table>' +
		    '</div>'
		),

		templateAcct: _.template('<div>' +
            '<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
		 	'	<thead style="font-weight: bold;">' +
			'		<tr>' +
			'			<td style="width:30px;">OrgAdmin</td>' +
			'			<td style="width:150px;">AcctId</td>' +
			'			<td style="width:50px;">OrgId</td>' +
			'			<td style="width:50px;">RoleId</td>' +
			'			<td style="width:20px;">Status</td>' +
			'		</tr>' +
			'	</thead>' +
		 	'	<tbody> <%= acctRows %> </tbody>' +
		 	'</table>' +
		    '</div>'
		),

		templateRole: _.template('<div>' +
            '<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
		 	'	<thead style="font-weight: bold;">' +
			'		<tr>' +
			'			<td style="width:30px;">Admin</td>' +
			'			<td style="width:30px;">Voter</td>' +
			'			<td style="width:20px;">Access</td>' +
			'			<td style="width:30px;">Active</td>' +
			'			<td style="width:50px;">OrgId</td>' +
			'			<td style="width:50px;">RoleId</td>' +
			'		</tr>' +
			'	</thead>' +
		 	'	<tbody> <%= roleRows %> </tbody>' +
		 	'</table>' +
		    '</div>'
		),

		templateRowNode: _.template('<tr>'
		    + '<td><%= n.orgId %></td>'
		    + '<td><%= n.status %></td>'
		    + '<td><%= n.url %></td>'
		    + '</tr>'
		),

		templateRowAcct: _.template('<tr>'
		    + '<td><%= a.orgAdmin %></td>'
		    + '<td><%= a.acctId %></td>'
		    + '<td><%= a.orgId %></td>'
		    + '<td><%= a.roleId %></td>'
		    + '<td><%= a.status %></td>'
		    + '</tr>'
		),

		templateRowRole: _.template('<tr>'
		    + '<td><%= r.isAdmin %></td>'
		    + '<td><%= r.isVoter %></td>'
		    + '<td><%= r.access %></td>'
		    + '<td><%= r.active %></td>'
		    + '<td><%= r.orgId %></td>'
		    + '<td><%= r.roleId %></td>'
		    + '</tr>'
		),

		fetch: function() {
			var _this = this;
			$.when(
				utils.load({ url: this.url, data: { id: _this.orgDet } })
			).fail(function(res) {
                $('#widget-' + _this.shell.id).html( '<h3 style="text-align: center;margin-top: 70px;">Unable to load transaction</h3>' );

             	$('#widget-shell-' + _this.shell.id + ' .panel-title span').html('Transaction Detail');
            }).done(function(res) {
				var nodeRows = [];
				var acctRows = [];
				var roleRows = [];
				console.log(res);
				_.each(res.data.attributes.nodeList, function(node) {
			        nodeRows.push(_this.templateRowNode({n: node}));
			    })
			    _.each(res.data.attributes.acctList, function(acct) {
					acctRows.push(_this.templateRowAcct({a: acct}));
			    })
			    _.each(res.data.attributes.roleList, function(role) {
					roleRows.push(_this.templateRowRole({r: role}));
			    })

				$('#widget-' + _this.shell.id).html('<h3 style="margin-top: 30px;margin-left: 8px;">Node List</h3>' +
				    _this.templateNode({ nodeRows: nodeRows.join('') }) +
					'<h3 style="margin-top: 30px;margin-left: 8px;">Account List</h3>' +
					_this.templateAcct({ acctRows: acctRows.join('') }) +
					'<h3 style="margin-top: 30px;margin-left: 8px;">Role List</h3>' +
					_this.templateRole({ roleRows: roleRows.join('') }));

			    $('#widget-shell-' + _this.shell.id + ' .panel-title span').html(_this.title);
            });
		}
	};

	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
