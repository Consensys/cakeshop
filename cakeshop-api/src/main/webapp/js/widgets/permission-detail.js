import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'permissions-detail',
		title: 'Permissions Detail',
		size: 'large',

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
		url_recoverAcct: 'api/permissions/recoverAcct',
		url_approveAcct: 'api/permissions/approveAcct',
		url_recoverNode: 'api/permissions/recoverNode',
		url_approveNode: 'api/permissions/approveNode',


		hideLink: true,

		setData: function(data) {
			this.data = data;
			this.orgDet = data;

			this.title = 'Permissions For: ' + this.orgDet;
		},

		populateFrom: function() {
            Account.list().then(function(accounts) {
		        var rows = ['<option>Choose Account</option>'];

				accounts.forEach(function(acct) {
                    if (acct.get('unlocked')) {
                        //only add unlocked accounts
                        rows.push( '<option>' + acct.get('address') + '</option>' );
                    }
				});

                $('#from-account').html( rows.join('') );

			}.bind(this));

		},

		roleAccessOptions: function() {

            Account.list().then(function(accounts) {
		        var rows = ['<option>0-ReadOnly</option>', '<option>1-Transact</option>', '<option>2-ContractDeploy</option>', '<option>3-FullAccess</option>'];

                $('#access').html( rows.join('') );
            })
        },

		statusOptions: function() {

            Account.list().then(function(accounts) {
		        var rows = ['<option>1-Suspend</option>', '<option>2-Activate</option>', '<option>3-Blacklist</option>'];

                console.log('rows')
                console.log(rows)

                $('#stat').html( rows.join('') );
            })
        },

		templateNode: _.template('<div>'
            + '<table style="width: 100%; table-layout: fixed;" class="table table-striped">'
		 	+ '	<thead style="font-weight: bold;">'
			+ '		<tr>'
			+ '           <td class="url">Url</td>'
			+ '           <td class="org-status" title=" 0-NotInList&#010 1-PendingApproval&#010 2-Approved&#010 3-Deactivated&#010 4-Blacklisted&#010 5-Recovery initiated">Status</td>'
			+ '           <td class="org-id">OrgId</td>'
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
			+ '			<td class="acct-id">Account Id</td>'
			+ '			<td class="role-id">RoleId</td>'
			+ '			<td class="status" title=" 0-NotInList&#010 1-PendingApproval&#010 2-Active&#010 4-Suspended&#010 5-Blacklisted&#010 6-Revoked&#010 7-Recovery initiated">Status</td>'
			+ '			<td class="org-id">OrgId</td>'
			+ '			<td class="org-admin">OrgAdmin</td>'
			+ '         <td class="recover-acct-col"></td>'
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
			+ '			<td class="active">Active</td>'
			+ '			<td class="access" title=" 0-ReadOnly&#010 1-Tranasct&#010 2-ContractDeploy&#010 3-FullAccess">Access</td>'
			+ '			<td class="org-id">OrgId</td>'
			+ '			<td class="voter">Voter</td>'
			+ '			<td class="admin">Admin</td>'
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

		templateSubs: _.template('<div>'
            + '<table style="width: 100%; table-layout: fixed;" class="table table-striped">'
		 	+ '	<thead style="font-weight: bold;">'
			+ '		<tr>'
			+ '			<td class="subOrg">SubOrg</td>'
			+ '		</tr>'
			+ '	</thead>'
		 	+ '	<tbody> <%= subRows %> </tbody>'
		 	+ '</table>'
		    + '</div>'
		),

		templateRowNode: _.template('<tr>'
            + '<td class="value org-url" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= n.url %></td>'
            + '<td data-orgid="<%= n.orgId %>" data-url="<%= n.url %>" class="org-status" title=" 0-NotInList&#010 1-PendingApproval&#010 2-Approved&#010 3-Deactivated&#010 4-Blacklisted&#010 5-Recovery initiated">'
            +   '<button class="btn btn-default update-node-btn"><%= n.status %></button>'
            + '</td>'
            + '<td class="value org-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= n.orgId %></a></td>'
		    + '<td data-orgid="<%= n.orgId %>" data-enodeid="<%= n.url %>"class="recover-node-col">'
            +   '<button class="btn btn-default recover-node-btn">Recover</button>'
            + '</td>'
		    + '</tr>'
		),

		templateRowAcct: _.template('<tr>'
		    + '<td class="value acct-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= a.acctId %></td>'
		    + '<td data-orgid="<%= a.orgId %>" data-acctid="<%= a.acctId %>" class="role-id">'
            +   '<button class="btn btn-default change-role-btn"><%= a.roleId %></button>'
            + '</td>'
		    + '<td data-orgid="<%= a.orgId %>" data-acctid="<%= a.acctId %>"class="status" title=" 0-NotInList&#010 1-PendingApproval&#010 2-Active&#010 4-Suspended&#010 5-Blacklisted&#010 6-Revoked&#010 7-Recovery initiated">'
            +   '<button class="btn btn-default update-acct-btn"><%= a.status %></button>'
            + '</td>'
            + '<td class="value org-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= a.orgId %></td>'
            + '<td class="value org-admin" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= a.orgAdmin %></td>'
		    + '<td data-orgid="<%= a.orgId %>" data-acctid="<%= a.acctId %>"class="recover-acct-col">'
            +   '<button class="btn btn-default recover-acct-btn">Recover</button>'
            + '</td>'
		    + '</tr>'
		),

		templateRowRole: _.template('<tr>'
		    + '<td class="value role-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= r.roleId %></td>'
		    + '<td class="value active" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= r.active %></td>'
            + '<td class="value access" contentEditable="false" title=" 0-ReadOnly&#010 1-Tranasct&#010 2-ContractDeploy&#010 3-FullAccess" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= r.access %></td>'
		    + '<td class="value org-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= r.orgId %></td>'
		    + '<td class="value voter" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= r.isVoter %></td>'
		    + '<td class="value admin" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= r.isAdmin %></a></td>'
		    + '<td data-orgid="<%= r.orgId %>" data-roleid="<%= r.roleId %>" class="remove-role-col">'
            +   '<button class="btn btn-default remove-role-btn">Remove Role</button>'
            + '</td>'
		    + '</tr>'
		),

		templateRowSubs: _.template('<tr>'
		    + '<td class="value subOrg" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= s %></td>'
		    + '</tr>'
		),

		modalStatusTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group status-form">' +
		    '	    <label for="stat">Status</label>' +
            '	    <select id="stat" class="form-control" style="transition: none;"> </select>' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="changeStatus-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

		modalFromTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group from-form">' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="from-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

		modalRoleTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group role-form">' +
            '		<label for="org-id">Org Id</label>' +
            '		<input type="text" class="form-control" id="org-id" value="<%=orgId%>">' +
            '		<label for="role-id">Role Id</label>' +
            '		<input type="text" class="form-control" id="role-id">' +
		    '	    <label for="access">Access</label>' +
            '	    <select id="access" class="form-control" style="transition: none;"> </select>' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
            '       <input type="checkbox" id="voter" name="voter" value="yes"> <label for="voter">Voter</label>' +
            '       <input type="checkbox" id="admin" name="admin" value="yes"> <label for="admin">Admin</label>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="newRole-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

		modalNodeTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group node-form">' +
            '		<label for="org-id">Org Id</label>' +
            '		<input type="text" class="form-control" id="org-id" value="<%=orgId%>">' +
            '		<label for="enode-id">Enode Id</label>' +
            '		<input type="text" class="form-control" id="enode-id">' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="newNode-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

		modalAcctTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group acct-form">' +
            '		<label for="org-id">Org Id</label>' +
            '		<input type="text" class="form-control" id="org-id" value="<%=orgId%>">' +
            '		<label for="acct-id">Acct Id</label>' +
            '		<input type="text" class="form-control" id="acct-id" value="<%=acctId%>">' +
            '		<label for="role-id">Role Id</label>' +
            '		<input type="text" class="form-control" id="role-id">' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="newAcct-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

		modalAdminTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'</div>' +
			'<div class="modal-body">' +
            '  <div class="radio">' +
			'    <label>' +
			'      <input type="radio" id="action" name="action" value="change" checked="checked"/>' +
			'      Change Role' +
			'    </label>' +
			'  </div>' +
            '  <div class="radio">' +
			'    <label>' +
			'      <input type="radio" id="action" name="action" value="assign"/>' +
			'      Assign Admin Role' +
			'    </label>' +
			'  </div>' +
			'  <div class="radio">' +
			'    <label>' +
			'      <input type="radio" id="action" name="action" value="approve"/>' +
			'      Approve Admin Role' +
			'    </label>' +
			'  </div>' +
			'	<div class="form-group acct-form">' +
            '		<label for="org-id">Org Id</label>' +
            '		<input type="text" class="form-control" id="org-id" value="<%=orgId%>">' +
            '		<label for="acct-id">Acct Id</label>' +
            '		<input type="text" class="form-control" id="acct-id" value="<%=acctId%>">' +
            '		<label for="role-id">Role Id</label>' +
            '		<input type="text" class="form-control" id="role-id">' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="admin-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

		modalRecoverAcctTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'</div>' +
			'<div class="modal-body">' +
            '  <div class="radio">' +
			'    <label>' +
			'      <input type="radio" id="action" name="action" value="recover" checked="checked"/>' +
			'      Recover Blacklisted Account' +
			'    </label>' +
			'  </div>' +
			'  <div class="radio">' +
			'    <label>' +
			'      <input type="radio" id="action" name="action" value="approve"/>' +
			'      Approve Blacklisted Account' +
			'    </label>' +
			'  </div>' +
			'	<div class="form-group recover-form">' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="recover-acct-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

		modalRecoverNodeTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'</div>' +
			'<div class="modal-body">' +
            '  <div class="radio">' +
			'    <label>' +
			'      <input type="radio" id="action" name="action" value="recover" checked="checked"/>' +
			'      Recover Blacklisted Node' +
			'    </label>' +
			'  </div>' +
			'  <div class="radio">' +
			'    <label>' +
			'      <input type="radio" id="action" name="action" value="approve"/>' +
			'      Approve Blacklisted Node' +
			'    </label>' +
			'  </div>' +
			'	<div class="form-group recover-form">' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="recover-node-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
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
                $('#widget-' + _this.shell.id).html( '<h3 style="text-align: center;margin-top: 70px;">No Org Selected</h3>' );

             	$('#widget-shell-' + _this.shell.id + ' .panel-title span').html('Permission Org Detail');
            }).done(function(res) {
				var nodeRows = [];
				var acctRows = [];
				var roleRows = [];
				var subRows = [];

				_.each(res.data.attributes.nodeList, function(node) {
			        nodeRows.push(_this.templateRowNode({n: node}));
			    })
			    _.each(res.data.attributes.acctList, function(acct) {
					acctRows.push(_this.templateRowAcct({a: acct}));
			    })
			    _.each(res.data.attributes.roleList, function(role) {
			        console.log(role);
					roleRows.push(_this.templateRowRole({r: role}));
			    })
			    _.each(res.data.attributes.subOrgList, function(subOrg) {
					subRows.push(_this.templateRowSubs({s: subOrg}));
			    })

				$('#widget-' + _this.shell.id).html('<h3 style="margin-top: 30px;margin-left: 8px;">Node List</h3>' +
				    _this.templateNode({ nodeRows: nodeRows.join(''), orgId: _this.orgDet }) +
					'<h3 style="margin-top: 30px;margin-left: 8px;">Account List</h3>' +
					_this.templateAcct({ acctRows: acctRows.join(''), orgId: _this.orgDet }) +
					'<h3 style="margin-top: 30px;margin-left: 8px;">Role List</h3>' +
					_this.templateRole({ roleRows: roleRows.join(''), orgId: _this.orgDet }) +
					'<h3 style="margin-top: 30px;margin-left: 8px;">SubOrg List</h3>' +
					_this.templateSubs({ subRows: subRows.join('')})
			    );

			    $('#widget-shell-' + _this.shell.id + ' .panel-title span').html(_this.title);
            });
		},

        postRender: function() {
			var _this = this;

            $('#widget-' + _this.shell.id).on('click', '.update-node-btn', function(e) {

                _this.populateFrom();

                _this.statusOptions();

				var orgId = $(e.target.parentElement).data("orgid");
				var url = $(e.target.parentElement).data("url");

				// set the modal text
				$('#myModal .modal-content').html(_this.modalStatusTemplate({
				    addOrg: "updateNode"
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#changeStatus-btn-final').click( function() {
					var status = $('#stat').val().split('-')[0];
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

					}).fail(function(err) {
					    console.log(err)
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: err.responseJSON.errors.map((error) => error.detail)
						}) );
					});
				});

			});

         $('#widget-' + _this.shell.id).on('click', '.update-acct-btn', function(e) {

                _this.populateFrom();
                _this.statusOptions();

				var orgId = $(e.target.parentElement).data("orgid");
				var acctId = $(e.target.parentElement).data("acctid");

                console.log('acctid')
				console.log(acctId);


				// set the modal text
				$('#myModal .modal-content').html(_this.modalStatusTemplate({
				    addOrg: "updateAcct"
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#changeStatus-btn-final').click( function() {
					var status = $('#stat').val().split('-')[0];
					var fromAcct = $('#from-account').val();

					$.when(
						utils.load({
							url: _this.url_updateAcct,
							data: {
								"id": orgId,
								"accountId": acctId,
								"action": status,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function(err) {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: err.responseJSON.errors.map((error) => error.detail)
						}) );
					});
				});

			});

         $('#widget-' + _this.shell.id).on('click', '.change-role-btn', function(e) {

				var orgId = $(e.target.parentElement).data("orgid");
				var acctId = $(e.target.parentElement).data("acctid");
                _this.populateFrom();

				// set the modal text
				$('#myModal .modal-content').html(_this.modalAdminTemplate({
				    addOrg: "changeRole/assignAdmin",
				    orgId: orgId,
				    acctId: acctId
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#admin-btn-final').click( function() {
                    var orgId = $('#org-id').val();
					var role = $('#role-id').val();
					var acctId = $('#acct-id').val();
					var fromAcct = $('#from-account').val();

					var type = $('#action:checked').val();

                    var urlAdmin;
                    switch(type) {
                        case "change":
                            urlAdmin = _this.url_changeAcct
                            break;
                        case "assign":
                            urlAdmin = _this.url_assignAdmin
                             break;
                        case "approve":
                            urlAdmin = _this.url_approveAdmin
                            break;
                        default:
                            urlAdmin = ""
                    }

				    console.log(orgId);
				    console.log(role);
				    console.log(fromAcct);
				    console.log(urlAdmin);

					$.when(
						utils.load({
							url: urlAdmin,
							data: {
								"id": orgId,
								"roleId": role,
								"accountId": acctId,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function(err) {
					    console.log(err)
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: err.responseJSON.errors.map((error) => error.detail)
						}) );
					});
				});

			});

         $('#widget-' + _this.shell.id).on('click', '.remove-role-btn', function(e) {

                var orgId = $(e.target.parentElement).data("orgid");
         		var roleId = $(e.target.parentElement).data("roleid");
                _this.populateFrom();

				// set the modal text
				$('#myModal .modal-content').html(_this.modalFromTemplate({
				    addOrg: "removeRole"
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

					}).fail(function(err) {
					    console.log(err)
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: err.responseJSON.errors.map((error) => error.detail)
						}) );
					});
				});

			});

         $('#widget-' + _this.shell.id).on('click', '.add-role', function(e) {
                var orgId = $(e.target.parentElement).data("orgid");
                _this.populateFrom();
                _this.roleAccessOptions();

				// set the modal text
				$('#myModal .modal-content').html(_this.modalRoleTemplate({
				    addOrg: "addRole",
				    orgId: orgId
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#newRole-btn-final').click( function() {

                    var id = $('#org-id').val();
                    var roleId = $('#role-id').val();
                    var access = $('#access').val().split('-')[0];
                    var voter = $('#voter:checked').val() == 'yes';
                    var admin = $('#admin:checked').val() == 'yes';
                    var fromAcct = $('#from-account').val();


					$.when(
						utils.load({
							url: _this.url_newRole,
							data: {
								"id": id,
								"roleId": roleId,
								"access" : access,
								"voter": voter,
								"admin": admin,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');
						console.log('role-done')

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function(err) {
					    console.log(err)
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: err.responseJSON.errors.map((error) => error.detail)
						}) );
					});
				});

			});

         $('#widget-' + _this.shell.id).on('click', '.add-node', function(e) {
                var orgId = $(e.target.parentElement).data("orgid");
                _this.populateFrom();

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

					}).fail(function(err) {
					    console.log(err)
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: err.responseJSON.errors.map((error) => error.detail)
						}) );
					});
				});

			});

         $('#widget-' + _this.shell.id).on('click', '.add-acct', function(e) {
                var orgId = $(e.target.parentElement).data("orgid");
                _this.populateFrom();

				// set the modal text
				$('#myModal .modal-content').html(_this.modalAcctTemplate({
				    addOrg: "addAcct",
				    orgId: orgId,
				    acctId: ""
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
								"accountId": acctId,
								"roleId": roleId,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function(err) {
					    console.log(err)
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: err.responseJSON.errors.map((error) => error.detail)
						}) );
					});
				});

			});

        $('#widget-' + _this.shell.id).on('click', '.recover-acct-btn', function(e) {
                var orgId = $(e.target.parentElement).data("orgid");
                var acctId = $(e.target.parentElement).data("acctid");
                _this.populateFrom();

				// set the modal text
				$('#myModal .modal-content').html(_this.modalRecoverAcctTemplate({
				    addOrg: "recoverAcct"
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#recover-acct-btn-final').click( function() {
                var fromAcct = $('#from-account').val();
                var type = $('#action:checked').val();

                var urlRecover = type == "recover" ? _this.url_recoverAcct : _this.url_approveAcct

					$.when(
						utils.load({
							url: urlRecover,
							data: {
								"id": orgId,
								"accountId": acctId,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function(err) {
					    console.log(err)
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: err.responseJSON.errors.map((error) => error.detail)
						}) );
					});
				});

			});
        $('#widget-' + _this.shell.id).on('click', '.recover-node-btn', function(e) {
                var orgId = $(e.target.parentElement).data("orgid");
                var enodeId = $(e.target.parentElement).data("enodeid");
                _this.populateFrom();

				// set the modal text
				$('#myModal .modal-content').html(_this.modalRecoverNodeTemplate({
				    addOrg: "recoverNode"
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#recover-node-btn-final').click( function() {
                var fromAcct = $('#from-account').val();
                var type = $('#action:checked').val();

                var urlRecover = type == "recover" ? _this.url_recoverNode : _this.url_approveNode

					$.when(
						utils.load({
							url: urlRecover,
							data: {
								"id": orgId,
								"enodeId": acctId,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgDetailUpdate'], true)
						_this.fetch();

					}).fail(function(err) {
					    console.log(err)
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: err.responseJSON.errors.map((error) => error.detail)
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
