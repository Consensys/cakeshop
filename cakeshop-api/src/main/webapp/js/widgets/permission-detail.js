import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'permissions-detail',
		title: 'Permissions Detail',
		size: 'large',

		url_details: 'api/permissions/getDetails',
		url_addNode: 'api/permissions/addNode',
		url_updateNode: 'api/permissions/updateNode',
		url_recoverNode: 'api/permissions/recoverNode',
		url_approveNode: 'api/permissions/approveNode',
		url_addAcct: 'api/permissions/addAccount',
		url_changeAcct: 'api/permissions/changeAccount',
		url_updateAcct: 'api/permissions/updateAccount',
		url_assignAdmin: 'api/permissions/assignAdmin',
		url_approveAdmin: 'api/permissions/approveAdmin',
		url_recoverAcct: 'api/permissions/recoverAcct',
		url_approveAcct: 'api/permissions/approveAcct',
		url_addRole: 'api/permissions/addNewRole',
		url_removeRole: 'api/permissions/removeRole',

		setData: function(data) {
			this.data = data;
			this.orgDet = data;
			this.title = 'Permissions For: ' + this.orgDet;
		},

		populateFrom: function() {
            Account.list().then(function(accounts) {
		        var rows = [];

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
		        var rows = ['<option>Choose role access</option>', '<option>0-ReadOnly</option>', '<option>1-Transact</option>', '<option>2-ContractDeploy</option>', '<option>3-FullAccess</option>'];
                $('#access').html( rows.join('') );
            })
        },

		statusOptions: function() {
            Account.list().then(function(accounts) {
		        var rows = ['<option>Choose new status</option>', '<option>1-Suspend</option>', '<option>2-Activate</option>', '<option>3-Blacklist</option>'];
                $('#stat').html( rows.join('') );
            })
        },

		populateRoles: function(orgId) {
		    $.when(
		        utils.load({ url: this.url_details, data: { id: orgId } })
		    ).done(function(list) {
		        var roles = list.data.attributes.roleList.length > 1 ? ['<option> Select role id </option>'] : [];

		        _.each(list.data.attributes.roleList, function(role) {
                    roles.push('<option>' + role.roleId + '</option>');
                })

                $('#role-id').html(roles.join(''));
		    }.bind(this));

		},

		templateNode: _.template('<div>'
            + '<table style="width: 100%; table-layout: fixed;" class="table table-striped">'
		 	+ '	<thead style="font-weight: bold;">'
			+ '		<tr>'
			+ '           <td class="url">Url</td>'
			+ '           <td class="status">Status</td>'
			+ '           <td class="status-col"></td>'
			+ '           <td class="recover-col"></td>'
			+ '		</tr>'
			+ '	</thead>'
		 	+ '	<tbody> <%= nodeRows %> </tbody>'
		 	+ '</table>'
		    + '</div>'
		),

		templateAcct: _.template('<div>'
            + '<table style="width: 100%; table-layout: fixed;" class="table table-striped">'
		 	+ '	<thead style="font-weight: bold;">'
			+ '		<tr>'
			+ '			<td class="acct-id">Account Id</td>'
			+ '			<td class="role-id">RoleId</td>'
			+ '			<td class="status">Status</td>'
			+ '			<td class="org-admin">OrgAdmin</td>'
			+ '         <td class="role-col"></td>'
			+ '         <td class="status-col"></td>'
			+ '         <td class="recover-acct-col"></td>'
			+ '		</tr>'
			+ '	</thead>'
		 	+ '	<tbody> <%= acctRows %> </tbody>'
		 	+ '</table>'
		    + '</div>'
		),

		templateRole: _.template('<div>'
            + '<table style="width: 100%; table-layout: fixed;" class="table table-striped">'
		 	+ '	<thead style="font-weight: bold;">'
			+ '		<tr>'
			+ '			<td class="role-id">RoleId</td>'
			+ '			<td class="active">Active</td>'
			+ '			<td class="access">Access</td>'
			+ '			<td class="voter">Voter</td>'
			+ '			<td class="admin">Admin</td>'
			+ '         <td class="remove-role-col"></td>'
			+ '		</tr>'
			+ '	</thead>'
		 	+ '	<tbody> <%= roleRows %> </tbody>'
		 	+ '</table>'
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

		templateAddNewButtons: _.template('<div data-orgid="<%= orgId %>"class="form-group pull-right">'
		    + '	<button class="btn btn-primary add-node">Add New Node</button>'
            + '	<button class="btn btn-primary add-acct <%= disableAddAcct %>">Add New Account</button>'
			+ '	<button class="btn btn-primary add-role">Add New Role</button>'
			+ '</div>'
		),

		templateRowNode: _.template('<tr>'
            + '<td class="value org-url" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= n.url %></td>'
            + '<td class="value status" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= status %></td>'
            + '<td data-orgid="<%= n.orgId %>" data-url="<%= n.url %>" class="status-col">'
            +   '<button class="btn btn-default update-node-btn" <%= disableStatus %>>Change Status</button>'
            + '</td>'
		    + '<td data-orgid="<%= n.orgId %>" data-enodeid="<%= n.url %>" data-status="<%= n.status %>" class="recover-col">'
            +   '<button class="btn btn-default recover-node-btn" <%= disableRecover %>>Recover</button>'
            + '</td>'
		    + '</tr>'
		),

		templateRowAcct: _.template('<tr>'
		    + '<td class="value acct-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= a.acctId %></td>'
		    + '<td class="value role-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= a.roleId %></td>'
		    + '<td class="value status" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= status %></td>'
            + '<td class="value org-admin" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= orgAdmin %></td>'
		    + '<td data-orgid="<%= a.orgId %>" data-acctid="<%= a.acctId %>" class="role-col">'
            +   '<button class="btn btn-default change-role-btn">Change Role</button>'
            + '</td>'
		    + '<td data-orgid="<%= a.orgId %>" data-acctid="<%= a.acctId %>"class="status-col">'
            +   '<button class="btn btn-default update-acct-btn" <%= disableStatus %>>Change Status</button>'
            + '</td>'
		    + '<td data-orgid="<%= a.orgId %>" data-acctid="<%= a.acctId %>" data-status="<%= a.status %>" class="recover-acct-col">'
            +   '<button class="btn btn-default recover-acct-btn <%= disableRecover %>">Recover Acct</button>'
            + '</td>'
		    + '</tr>'
		),

		templateRowRole: _.template('<tr>'
		    + '<td class="value role-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= r.roleId %></td>'
		    + '<td class="value active" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= active %></td>'
            + '<td class="value access" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= access %></td>'
		    + '<td class="value voter" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= voter %></td>'
		    + '<td class="value admin" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= admin %></a></td>'
		    + '<td data-orgid="<%= r.orgId %>" data-roleid="<%= r.roleId %>" class="remove-role-col">'
            +   '<button class="btn btn-default remove-role-btn">Remove Role</button>'
            + '</td>'
		    + '</tr>'
		),

		templateRowSubs: _.template('<tr>'
		    + '<td class="value subOrg" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><a href="#"><%= s %></a></td>'
		    + '</tr>'
		),

		modalStatusTemplate: _.template( '<div class="modal-header">' +
			'	<%=header%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group status-form">' +
		    '	    <label for="stat">Change Status</label>' +
            '	    <select id="stat" class="form-control" style="transition: none;"> </select>' +
            '		</br> '+
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
			'	<button type="button" id="changeStatus-btn-final" class="btn btn-primary">Change Status</button>' +
			'</div>'),

		modalFromTemplate: _.template( '<div class="modal-header">' +
			'	<%=header%>' +
			'</div>' +
			'<div class="modal-body">' +
			'   <div id="notification">' +
			'   </div>' +
			'	<div class="form-group from-form">' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
			'	<button type="button" id="from-btn-final" class="btn btn-primary">Remove Role</button>' +
			'</div>'),

		modalRoleTemplate: _.template( '<div class="modal-header">' +
			'	<%=header%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group role-form">' +
            '		<label for="org-id">Org Id</label>' +
            '		<input type="text" class="form-control" id="org-id" value="<%=orgId%>">' +
            '		</br> '+
            '		<label for="role-id">Role Id</label>' +
            '		<input type="text" class="form-control" id="role-id" placeholder="Enter new role name">' +
            '		</br> '+
		    '	    <label for="access">Access Level</label>' +
            '	    <select id="access" class="form-control" style="transition: none;"> </select>' +
            '		</br> '+
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
            '		</br> '+
            '       <input type="checkbox" id="voter" name="voter" value="yes"> <label for="voter">Voter</label>' +
            '		</br> '+
            '       <input type="checkbox" id="admin" name="admin" value="yes"> <label for="admin">Admin</label>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
			'	<button type="button" id="newRole-btn-final" class="btn btn-primary">Add Role</button>' +
			'</div>'),

		modalNodeTemplate: _.template( '<div class="modal-header">' +
			'	<%=header%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group node-form">' +
            '		<label for="org-id">Org Id</label>' +
            '		<input type="text" class="form-control" id="org-id" value="<%=orgId%>">' +
            '		</br> '+
            '		<label for="enode-id">Enode Id</label>' +
            '		<input type="text" class="form-control" id="enode-id" placeholder="Enter full enode id">' +
            '		</br> '+
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
			'	<button type="button" id="newNode-btn-final" class="btn btn-primary">Add Node</button>' +
			'</div>'),

		modalAcctTemplate: _.template( '<div class="modal-header">' +
			'	<%=header%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group acct-form">' +
            '		<label for="org-id">Org Id</label>' +
            '		<input type="text" class="form-control" id="org-id" value="<%=orgId%>">' +
            '		</br> '+
            '		<label for="acct-id">Acct Id</label>' +
            '		<input type="text" class="form-control" id="acct-id" placeholder="Enter new account address">' +
            '		</br> '+
		    '	    <label for="role-id">Role Id</label>' +
            '	    <select id="role-id" class="form-control" style="transition: none;"> </select>' +
            '		</br> '+
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
			'	<button type="button" id="newAcct-btn-final" class="btn btn-primary">Add Account</button>' +
			'</div>'),

		modalChangeRoleTemplate: _.template( '<div class="modal-header">' +
			'	<%=header%>' +
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
			'		</br> '+
            '		<label for="org-id">Org Id</label>' +
            '		<input type="text" class="form-control" id="org-id" value="<%=orgId%>">' +
            '		</br> '+
            '		<label for="acct-id">Acct Id</label>' +
            '		<input type="text" class="form-control" id="acct-id" value="<%=acctId%>">' +
            '		</br> '+
		    '	    <label for="role-id">Role Id</label>' +
            '	    <select id="role-id" class="form-control" style="transition: none;"> </select>' +
            '		</br> '+
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
			'	<button type="button" id="change-role-btn-final" class="btn btn-primary">Update Role</button>' +
			'</div>'),

		modalRecoverAcctTemplate: _.template( '<div class="modal-header">' +
			'	<%=header%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group recover-form">' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
			'	<button type="button" id="recover-acct-btn-final" class="btn btn-primary">Recover Account</button>' +
			'</div>'),

		modalRecoverNodeTemplate: _.template( '<div class="modal-header">' +
			'	<%=header%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group recover-form">' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
			'	<button type="button" id="recover-node-btn-final" class="btn btn-primary">Recover Node</button>' +
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

				var nodeStatuses = ["Not in List", "Pending", "Approved", "Deactivated", "Blacklisted", "Recovery Initiated"];
                var acctStatuses = ["Not in List", "Pending", "Active", "", "Suspended", "Blacklisted", "Revoked", "Recovery Initiated"];
                var roleAccesses = ["Read Only", "Transact", "ContractDeploy", "FullAccess"];

				_.each(res.data.attributes.nodeList, function(node) {
				    var status = nodeStatuses[node.status]
				    var disableRecover = node.status == 4 || node.status == 5 ? '' : 'disabled'
				    var disableStatus = node.status == 2 || node.status == 3 ? '' : 'disabled'
			        nodeRows.push(_this.templateRowNode({
			            n: node,
			            status: status,
			            disableRecover: disableRecover,
			            disableStatus: disableStatus
			        }));
			    })
			    _.each(res.data.attributes.acctList, function(acct) {
			        var status = acctStatuses[acct.status]
			        var disableRecover = acct.status == 5 || acct.status == 7? '' : 'disabled'
			        var disableStatus = acct.status == 2 || acct.status == 4 || acct.status == 6 ? '' : 'disabled'
			        // checkmark (\u2713) and x (\u2717)
			        var orgAdmin = acct.orgAdmin ? '\u2713' : '\u2717'
					acctRows.push(_this.templateRowAcct({
					    a: acct,
					    status: status,
					    disableRecover: disableRecover,
					    disableStatus: disableStatus,
					    orgAdmin: orgAdmin
					}));
			    })
			    _.each(res.data.attributes.roleList, function(role) {
			        var access = roleAccesses[role.access]
			        // checkmark (\u2713) and x (\u2717)
			        var admin = role.admin ? '\u2713' : '\u2717'
			        var active = role.active ? "\u2713" : '\u2717'
			        var voter = role.voter ? '\u2713' : '\u2717'
					roleRows.push(_this.templateRowRole({
						r: role, 
						access: access, 
						admin: admin, 
						active: active, 
						voter: voter
					}));
			    })
			    _.each(res.data.attributes.subOrgList, function(subOrg) {
					subRows.push(_this.templateRowSubs({s: subOrg}));
			    })

			    var disableAddAcct = res.data.attributes.roleList == null || res.data.attributes.roleList.length < 1 ? 'disabled' : ''

				$('#widget-' + _this.shell.id).html(_this.templateAddNewButtons({orgId: _this.orgDet, disableAddAcct: disableAddAcct}) +
				    '<h3 style="margin-top: 30px;margin-left: 8px;">Node List</h3>' +
				    _this.templateNode({ nodeRows: nodeRows.join(''), orgId: _this.orgDet }) +
					'<h3 style="margin-top: 30px;margin-left: 8px;">Account List</h3>' +
					_this.templateAcct({ acctRows: acctRows.join(''), orgId: _this.orgDet }) +
					'<h3 style="margin-top: 30px;margin-left: 8px;">Role List</h3>' +
					_this.templateRole({ roleRows: roleRows.join(''), orgId: _this.orgDet }) +
					'<h3 style="margin-top: 30px;margin-left: 8px;">SubOrg List</h3>' +
					_this.templateSubs({ subRows: subRows.join('')})
			    );

			    $('#widget-shell-' + _this.shell.id + ' .panel-title span').html(_this.title);

				$('#widget-' + _this.shell.id + ' a').click(function(e) {
					e.preventDefault();

					Dashboard.show({ widgetId: 'permissions-detail', section: 'permissions', data: $(this).text(), refetch: true });
				});

				_this.postFetch();
            });
		},

        postRender: function() {
			var _this = this;

         $('#widget-' + _this.shell.id).on('click', '.add-node', function(e) {
                var orgId = $(e.target.parentElement).data("orgid");
                _this.populateFrom();

				// set the modal text
				$('#myModal .modal-content').html(_this.modalNodeTemplate({
				    header: "Add new node to " + orgId,
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
								"from": fromAcct
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

            $('#widget-' + _this.shell.id).on('click', '.update-node-btn', function(e) {

                _this.populateFrom();

                _this.statusOptions();

				var orgId = $(e.target.parentElement).data("orgid");
				var url = $(e.target.parentElement).data("url");

				// set the modal text
				$('#myModal .modal-content').html(_this.modalStatusTemplate({
				    header: "Change Status of Node"
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
								"from": fromAcct
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
                var status = $(e.target.parentElement).data("status");
                var approve = status == 5
                var header = approve ? "Approve recovery of this blacklisted node" : "Recover this node"
                _this.populateFrom();

				// set the modal text
				$('#myModal .modal-content').html(_this.modalRecoverNodeTemplate({
				    header: header
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#recover-node-btn-final').click( function() {
                var fromAcct = $('#from-account').val();

                var urlRecover = !approve ? _this.url_recoverNode : _this.url_approveNode

					$.when(
						utils.load({
							url: urlRecover,
							data: {
								"id": orgId,
								"enodeId": enodeId,
								"from": fromAcct
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
                _this.populateRoles(orgId);

				// set the modal text
				$('#myModal .modal-content').html(_this.modalAcctTemplate({
				    header: "Add new account to " + orgId,
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
								"accountId": acctId,
								"roleId": roleId,
								"from": fromAcct
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


				// set the modal text
				$('#myModal .modal-content').html(_this.modalStatusTemplate({
				    header: "Change Status of Account"
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
								"from": fromAcct
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
                _this.populateRoles(orgId);

				// set the modal text
				$('#myModal .modal-content').html(_this.modalChangeRoleTemplate({
				    header: "Change Role of Account or Assign/Approve Account as an Admin",
				    orgId: orgId,
				    acctId: acctId
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#change-role-btn-final').click( function() {
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

					$.when(
						utils.load({
							url: urlAdmin,
							data: {
								"id": orgId,
								"roleId": role,
								"accountId": acctId,
								"from": fromAcct
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
                var status = $(e.target.parentElement).data("status");
                var approve = status == 7
                var header = approve ? "Approve recovery of this blacklisted account" : "Recover this account"
                _this.populateFrom();

				// set the modal text
				$('#myModal .modal-content').html(_this.modalRecoverAcctTemplate({
				    header: header,

				}) );

				//open modal
				$('#myModal').modal('show');


                $('#recover-acct-btn-final').click( function(e) {
                var fromAcct = $('#from-account').val();

                var urlRecover = !approve ? _this.url_recoverAcct : _this.url_approveAcct
					$.when(

						utils.load({
							url: urlRecover,
							data: {
								"id": orgId,
								"accountId": acctId,
								"fromObject": {"from": fromAcct}
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
				    header: "Add a new role to " + orgId,
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
							url: _this.url_addRole,
							data: {
								"id": id,
								"roleId": roleId,
								"access" : access,
								"voter": voter,
								"admin": admin,
								"from": fromAcct
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
				    header: "Remove Role from " + orgId
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
								"from": fromAcct
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
