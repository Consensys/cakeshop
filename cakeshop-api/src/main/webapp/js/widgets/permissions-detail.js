import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'permissions-detail',
		title: 'Permissions Detail',
		size: 'medium',

		url_details: 'api/permissions/getDetails',
		url_newRole: 'api/permissions/addNewRole',
		url_removeRole: 'api/permissions/removeRole',
		url_addAcct: 'api/permissions/addAccount',
		url_changeAcct: 'api/permissions/changeAccount',
		url_updateAcct: 'api/permissions/updateAccount',
		url_assignAdmin: 'api/permissions/assignAdmin',
		url_approveAdmin: 'api/permissions/approveAdmin',
		url_addNode: 'api/permissions/addNode',
		url_updateNode: 'api/permissions/updateNode',


		hideLink: true,

		setData: function(data) {
			this.data = data;
			this.orgDet = data;

			this.title = 'Org #' + this.orgDet;
		},

		templateNode: _.template('<div>'
            + '<table style="width: 100%; table-layout: fixed;" class="table table-striped">'
		 	+ '	<thead style="font-weight: bold;">'
			+ '		<tr>'
			+ '           <td class="org-id" style="width:120px;">OrgId</td>'
			+ '           <td class="url">Url</td>'
			+ '           <td class="org-status" style="width:30px;">Status</td>'
			+ '           <td class="update-node-col"></td>'
			+ '		</tr>'
			+ '	</thead>'
		 	+ '	<tbody> <%= nodeRows %> </tbody>'
		 	+ '</table>'
		    + '</div>'
            + '<div data-orgid="<%= orgId %>" class="form-group pull-right">'
			+ '	<button class="btn btn-primary add-node">Add New Node</button>'
			+ '</div>'
		),

		templateAcct: _.template('<div>'
            + '<table style="width: 100%; table-layout: fixed;" class="table table-striped">'
		 	+ '	<thead style="font-weight: bold;">'
			+ '		<tr>'
			+ '			<td class="acct-id" style="width:120px;">Account Id</td>'
			+ '			<td class="org-id">OrgId</td>'
			+ '			<td class="org-admin">OrgAdmin</td>'
			+ '			<td class="role-id">RoleId</td>'
			+ '         <td class="change-role-col"></td>'
			+ '			<td class="status" style="width:30px;">Status</td>'
			+ '         <td class="update-acct-col"></td>'
			+ '		</tr>'
			+ '	</thead>'
		 	+ '	<tbody> <%= acctRows %> </tbody>'
		 	+ '</table>'
		    + '</div>'
            + '<div data-orgid="<%= orgId %>" class="form-group pull-right">'
			+ '	<button class="btn btn-primary add-acct">Add New Account</button>'
			+ '</div>'
		),

		templateRole: _.template('<div>'
            + '<table style="width: 100%; table-layout: fixed;" class="table table-striped">'
		 	+ '	<thead style="font-weight: bold;">'
			+ '		<tr>'
			+ '			<td class="role-id">RoleId</td>'
			+ '			<td class="org-id">OrgId</td>'
			+ '			<td class="voter">Voter</td>'
			+ '			<td class="access">Access</td>'
			+ '			<td class="active">Active</td>'
			+ '			<td class="org-admin">Admin</td>'
			+ '         <td class="assign-role-col"></td>'
			+ '         <td class="approve-role-col"></td>'
			+ '         <td class="remove-role-col"></td>'
			+ '		</tr>'
			+ '	</thead>'
		 	+ '	<tbody> <%= roleRows %> </tbody>'
		 	+ '</table>'
		    + '</div>'
            + '<div data-orgid="<%= orgId %>"class="form-group pull-right">'
			+ '	<button class="btn btn-primary add-role">Add New Role</button>'
			+ '</div>'
		),

		templateRowNode: _.template('<tr>'
            + '<td class="value org-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= n.orgId %></a></td>'
            + '<td class="value org-url" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= n.url %></td>'
		    + '<td class="value org-status" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= n.status %></td>'
		    + '<td data-orgid="<%= n.orgId %>" data-url="<%= n.url %>" class="update-node-col">'
            +   '<button class="btn btn-default update-node-btn">Update Status</button>'
            + '</td>'
		    + '</tr>'
		),

		templateRowAcct: _.template('<tr>'
		    + '<td class="value acct-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= a.acctId %></td>'
            + '<td class="value org-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= a.orgId %></td>'
            + '<td class="value org-admin" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= a.orgAdmin %></a></td>'
		    + '<td class="value role-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= a.roleId %></td>'
		    + '<td data-orgid="<%= a.orgId %>" class="change-role-col">'
            +   '<button class="btn btn-default change-role-btn">Change Role</button>'
            + '</td>'
		    + '<td class="value status" contentEditable="false" style=" text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= a.status %></td>'
		    + '<td data-orgid="<%= a.orgId %>" data-acctid="<%= a.acctId %>"class="update-acct-col">'
            +   '<button class="btn btn-default update-acct-btn">Update Status</button>'
            + '</td>'
		    + '</tr>'
		),

		templateRowRole: _.template('<tr>'
		    + '<td class="value role-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= r.roleId %></td>'
		    + '<td class="value org-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= r.orgId %></td>'
		    + '<td class="value voter" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= r.isVoter %></td>'
            + '<td class="value access" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= r.access %></td>'
		    + '<td class="value active" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= r.active %></td>'
		    + '<td class="value org-admin" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= r.isAdmin %></a></td>'
		    + '<td data-orgid="<%= r.orgId %>" class="assign-role-col">'
            +   '<button class="btn btn-default assign-role-btn">Assign Admin</button>'
            + '</td>'
		    + '<td data-orgid="<%= r.orgId %>" class="approve-role-col">'
            +   '<button class="btn btn-default approve-role-btn">Approve Admin</button>'
            + '</td>'
		    + '<td data-orgid="<%= r.orgId %>" data-roleid="<%= r.roleId %>" class="remove-role-col">'
            +   '<button class="btn btn-default remove-role-btn">Remove Role</button>'
            + '</td>'
		    + '</tr>'
		),

		modalStatusTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group add-org-form">' +
			'		<label for="status">Status</label>' +
			'		<input type="text" class="form-control" id="status">' +
            '		<label for="from-account">From Account</label>' +
            '		<input type="text" class="form-control" id="from-account">' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="changeStatus-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

		modalFromTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group add-org-form">' +
            '		<label for="from-account">From Account</label>' +
            '		<input type="text" class="form-control" id="from-account">' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="from-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

		modalRoleTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group add-org-form">' +
            '		<label for="org-id">Org Id</label>' +
            '		<input type="text" class="form-control" id="org-id" placeholder="<%=orgId%>">' +
            '		<label for="role-id">Role Id</label>' +
            '		<input type="text" class="form-control" id="role-id">' +
            '		<label for="access">Account Access</label>' +
            '		<input type="text" class="form-control" id="access">' +
            '		<label for="voter">Voter</label>' +
            '		<input type="text" class="form-control" id="voter">' +
            '		<label for="admin">Is Admin</label>' +
            '		<input type="text" class="form-control" id="admin">' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="newRole-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

		modalNodeTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group add-org-form">' +
            '		<label for="org-id">Org Id</label>' +
            '		<input type="text" class="form-control" id="org-id" placeholder="<%=orgId%>">' +
            '		<label for="enode-id">Enode Id</label>' +
            '		<input type="text" class="form-control" id="enode-id">' +
            '		<label for="from-account">From Account</label>' +
            '		<input type="text" class="form-control" id="from-account">' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="newNode-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

		modalAcctTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group add-org-form">' +
            '		<label for="org-id">Org Id</label>' +
            '		<input type="text" class="form-control" id="org-id" placeholder="<%=orgId%>">' +
            '		<label for="acct-id">Acct Id</label>' +
            '		<input type="text" class="form-control" id="acct-id">' +
            '		<label for="role-id">Role Id</label>' +
            '		<input type="text" class="form-control" id="role-id">' +
            '		<label for="from-account">From Account</label>' +
            '		<input type="text" class="form-control" id="from-account">' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="newAcct-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

        modalConfirmation: _.template('<div class="modal-body"><%=message%></div>'),

		subscribe: function() {
			Dashboard.Utils.on(function(e, action) {
				if (action[0] == 'orgDetailUpdate') {
					this.fetch();
				}
			}.bind(this));
		},

		fetch: function() {
			var _this = this;
			$.when(
				utils.load({ url: this.url_details, data: { id: _this.orgDet } })
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
				    _this.templateNode({ nodeRows: nodeRows.join(''), orgId: _this.orgDet }) +
					'<h3 style="margin-top: 30px;margin-left: 8px;">Account List</h3>' +
					_this.templateAcct({ acctRows: acctRows.join(''), orgId: _this.orgDet }) +
					'<h3 style="margin-top: 30px;margin-left: 8px;">Role List</h3>' +
					_this.templateRole({ roleRows: roleRows.join(''), orgId: _this.orgDet }));

			    $('#widget-shell-' + _this.shell.id + ' .panel-title span').html(_this.title);
            });
		},

        postRender: function() {
			var _this = this;

            $('#widget-' + _this.shell.id).on('click', '.update-node-btn', function(e) {

				var orgId = $(e.target.parentElement).data("orgid");
				var url = $(e.target.parentElement).data("url");

				// set the modal text
				$('#myModal .modal-content').html(_this.modalStatusTemplate({
				    addOrg: "changeStatus"
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#changeStatus-btn-final').click( function() {
					var status = $('#status').val();
					var fromAcct = $('#from-account').val();

					$.when(
						utils.load({
							url: _this.url_updateNode,
							data: {
								"id": orgId,
								"enodeId": url,
								"action": status,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function() {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Sorry, Please try again.'
						}) );
					});
				});

			});

         $('#widget-' + _this.shell.id).on('click', '.update-acct-btn', function(e) {

				var orgId = $(e.target.parentElement).data("orgid");
				var acctId = $(e.target.parentElement).data("acctid");


				// set the modal text
				$('#myModal .modal-content').html(_this.modalStatusTemplate({
				    addOrg: "changeStatus"
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#changeStatus-btn-final').click( function() {
					var status = $('#status').val();
					var fromAcct = $('#from-account').val();

					$.when(
						utils.load({
							url: _this.url_updateAcct,
							data: {
								"id": orgId,
								"acctId": acctId,
								"action": status,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function() {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Sorry, Please try again.'
						}) );
					});
				});

			});

         $('#widget-' + _this.shell.id).on('click', '.change-role-btn', function(e) {

				var orgId = $(e.target.parentElement).data("orgid");


				// set the modal text
				$('#myModal .modal-content').html(_this.modalStatusTemplate({
				    addOrg: "changeStatus"
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#changeStatus-btn-final').click( function() {
					var role = $('#status').val();
					var fromAcct = $('#from-account').val();

					$.when(
						utils.load({
							url: _this.url_changeAcct,
							data: {
								"id": orgId,
								"roleId": role,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function() {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Sorry, Please try again.'
						}) );
					});
				});

			});

         $('#widget-' + _this.shell.id).on('click', '.remove-role-btn', function(e) {

                var orgId = $(e.target.parentElement).data("orgid");
         		var roleId = $(e.target.parentElement).data("roleid");

				// set the modal text
				$('#myModal .modal-content').html(_this.modalFromTemplate({
				    addOrg: "changeStatus"
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#from-btn-final').click( function() {
                var fromAcct = $('#from-account').val();

					$.when(
						utils.load({
							url: _this.url_removeRole,
							data: {
								"id": orgId,
								"roleId": roleId,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function() {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Sorry, Please try again.'
						}) );
					});
				});

			});

         $('#widget-' + _this.shell.id).on('click', '.add-role', function(e) {
                var orgId = $(e.target.parentElement).data("orgid");
                console.log(orgId)

				// set the modal text
				$('#myModal .modal-content').html(_this.modalRoleTemplate({
				    addOrg: "addRole",
				    orgId: orgId
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#newRole-btn-final').click( function() {
                var orgId = $('#org-id').val();
                var roleId = $('#role-id').val();
                var access = $('#access').val();
                var voter = $('#voter').val();
                var admin = $('#admin').val();
                var fromAcct = $('#from-account').val();

					$.when(
						utils.load({
							url: _this.url_addRole,
							data: {
								"id": orgId,
								"roleId": roleId,
								"access" : access,
								"isVoter": voter,
								"isAdmin": admin,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function() {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Sorry, Please try again.'
						}) );
					});
				});

			});

         $('#widget-' + _this.shell.id).on('click', '.add-node', function(e) {
                var orgId = $(e.target.parentElement).data("orgid");
                console.log(orgId)

				// set the modal text
				$('#myModal .modal-content').html(_this.modalNodeTemplate({
				    addOrg: "addNode",
				    orgId: orgId
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#newNode-btn-final').click( function() {
                var orgId = $('#org-id').val();
                var enodeId = $('#enode-id').val();
                var fromAcct = $('#from-account').val();

					$.when(
						utils.load({
							url: _this.url_addNode,
							data: {
								"id": orgId,
								"enodeId": enodeId,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function() {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Sorry, Please try again.'
						}) );
					});
				});

			});

         $('#widget-' + _this.shell.id).on('click', '.add-acct', function(e) {
                var orgId = $(e.target.parentElement).data("orgid");
                console.log(orgId)

				// set the modal text
				$('#myModal .modal-content').html(_this.modalAcctTemplate({
				    addOrg: "addAcct",
				    orgId: orgId
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#newAcct-btn-final').click( function() {
                var orgId = $('#org-id').val();
                var acctId = $('#acct-id').val();
                var roleId = $('#role-id').val();
                var fromAcct = $('#from-account').val();

					$.when(
						utils.load({
							url: _this.url_addAcct,
							data: {
								"id": orgId,
								"acctId": acctId,
								"roleId": roleId,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function() {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Sorry, Please try again.'
						}) );
					});
				});

			});

        $('#widget-' + _this.shell.id).on('click', '.assign-role-btn', function(e) {
                var orgId = $(e.target.parentElement).data("orgid");
                console.log(orgId)

				// set the modal text
				$('#myModal .modal-content').html(_this.modalAcctTemplate({
				    addOrg: "assignAdmin",
				    orgId: orgId
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#newAcct-btn-final').click( function() {
                var orgId = $('#org-id').val();
                var acctId = $('#acct-id').val();
                var roleId = $('#role-id').val();
                var fromAcct = $('#from-account').val();

					$.when(
						utils.load({
							url: _this.url_assignAdmin,
							data: {
								"id": orgId,
								"acctId": acctId,
								"roleId": roleId,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function() {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Sorry, Please try again.'
						}) );
					});
				});

			});

        $('#widget-' + _this.shell.id).on('click', '.approve-role-btn', function(e) {
                var orgId = $(e.target.parentElement).data("orgid");
                console.log(orgId)

				// set the modal text
				$('#myModal .modal-content').html(_this.modalAcctTemplate({
				    addOrg: "approveAdmin",
				    orgId: orgId
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#newAcct-btn-final').click( function() {
                var orgId = $('#org-id').val();
                var acctId = $('#acct-id').val();
                var roleId = $('#role-id').val();
                var fromAcct = $('#from-account').val();

					$.when(
						utils.load({
							url: _this.url_approveAdmin,
							data: {
								"id": orgId,
								"acctId": acctId,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function() {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Sorry, Please try again.'
						}) );
					});
				});

			});
	    }
	};

	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
