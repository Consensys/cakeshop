import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'permissions-list',
		title: 'Permissions List',
		size: 'large',

		url_list: 'api/permissions/getList',
		url_details: 'api/permissions/getDetails',
		url_addOrg: 'api/permissions/addOrg',
		url_approveOrg: 'api/permissions/approveOrg',
		url_update_status: 'api/permissions/updateOrgStatus',
		url_approve_status: 'api/permissions/approveOrgStatus',
		url_subOrg: 'api/permissions/addSubOrg',

		template: _.template('<div>'
		    + '<table style="width: 100%; table-layout: fixed;" class="table table-striped">'
		    + ' <thead style="font-weight: bold;">'
		    + '     <tr>'
			+ '			<td class="org-id">OrgId</td>'
			+ '         <td class="org-status">Status</td>'
            + '         <td class="parent-id">Parent Orgs</td>'
            + '         <td class="status-col"></td>'
            + '         <td class="approve-col"></td>'
            + '         <td class="subOrg-col"></td>'
		    + '     </tr>'
		    + ' </thead>'
		    + '<tbody><%= rows %></tbody>'
		    + '</table>'
		    + '</div>'
            + '<div class="form-group pull-right">'
			+ '	<button class="btn btn-primary add-org">Add New Org</button>'
			+ '</div>'
		),

		 templateUninitialized: _.template(
            '<div>'
            + '<h3 style="text-align: center;margin-top: 70px;">Permissioning not enabled</h3>'
            + '</div>'
         ),

		templateRow: _.template('<tr>'
		    + '<td class="value org-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap;"><a href="#"><%= o.fullOrgId %></a></td>'
            + '<td class="value org-status" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap;"><%= status %></td>'
            + '<td class="value parent-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><a href="#"><%= parentUlt %></a><%= separator %><a href="#"><%= parentDirect %></a></td>'
		    + '<td data-orgid="<%= o.fullOrgId %>" data-status="<%= o.status %>" class="status-col">'
            +   '<button class="btn btn-default status-btn" <%= disableStatus %>>Change Status</button>'
            + '</td>'
		    + '<td data-orgid="<%= o.fullOrgId %>" class="approve-col">'
            +   '<button class="btn btn-default approve-btn" <%= disableApprove %>>Approve Org</button>'
            + '</td>'
            + '<td data-orgid="<%= o.fullOrgId %>" class="subOrg-col">'
            + ' <button class="btn btn-default add-subOrg">Add New SubOrg</button>'
            + '</td>'
		    + '</tr>'
		),

		modalOrgTemplate: _.template( '<div class="modal-header">' +
			'	<%=header%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group add-org-form">' +
			'		<label for="org-label">Org Name</label>' +
			'		<input type="text" class="form-control" id="org-label" value="<%=orgId%>" placeholder="Enter name for new org">' +
			'		<label for="enode-id">Enode id</label>' +
			'		<input type="text" class="form-control" id="enode-id" placeholder="Enter full enode id">' +
			'		<label for="account-admin">Account Admin for Org</label>' +
            '		<input type="text" class="form-control" id="account-admin" placeholder="Enter account address">' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
			'	<button type="button" id="addOrg-btn-final" class="btn btn-primary">Add Org</button>' +
			'</div>'),

		modalOrgApproveTemplate: _.template( '<div class="modal-header">' +
			'	<%=header%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group add-org-form">' +
			'		<label for="org-label">Org Name</label>' +
			'		<input type="text" class="form-control" id="org-label" value="<%=orgId%>">' +
		    '	    <label for="enode-id">Enode id</label>' +
            '	    <select id="enode-id" class="form-control" style="transition: none;"> </select>' +
		    '	    <label for="account-admin">Account Admin for Org</label>' +
            '	    <select id="account-admin" class="form-control" style="transition: none;"> </select>' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
			'	<button type="button" id="addOrg-btn-final" class="btn btn-primary">Approve Org</button>' +
			'</div>'),

		modalSubOrgTemplate: _.template( '<div class="modal-header">' +
			'	<%=header%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group add-org-form">' +
			'		<label for="org-label">Org Name</label>' +
			'		<input type="text" class="form-control" id="org-label" placeholder="Enter name for new suborg">' +
			'		<label for="parent-label">Parent Org</label>' +
            '		<input type="text" class="form-control" id="parent-label" value="<%=parentOrg%>">' +
			'		<label for="enode-id">Enode id</label>' +
			'		<input type="text" class="form-control" id="enode-id" placeholder="Enter full enode id">' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
			'	<button type="button" id="addSubOrg-btn-final" class="btn btn-primary">Add Sub Org</button>' +
			'</div>'),

		modalStatusTemplate: _.template( '<div class="modal-header">' +
			'	<%=header%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group">' +
			'		<label for="stat">Change Status</label>' +
            '		<input type="text" class="form-control" id="stat" value="<%=action%>">' +
		    '	    <label for="from-account">From Account</label>' +
            '	    <select id="from-account" class="form-control" style="transition: none;"> </select>' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
			'	<button type="button" id="changeStatus-btn-final" class="btn btn-primary">Change Status</button>' +
			'</div>'),


        modalConfirmation: _.template('<div class="modal-body"><%=message%></div>'),

		subscribe: function() {
			Dashboard.Utils.on(function(e, action) {
				if (action[0] == 'orgUpdate') {
					this.fetch();
				}
			}.bind(this));
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

		populateEnode: function(orgId) {
		    $.when(
		        utils.load({ url: this.url_details, data: { id: orgId } })
		    ).done(function(list) {
		        var nodes = list.data.attributes.nodeList.length > 1 ? ['<option> Select enodeId </option>'] : [];
		        var accts = list.data.attributes.acctList.length > 1 ? ['<option> Select Admin Account </option>'] : [];

		        _.each(list.data.attributes.nodeList, function(node) {
                    nodes.push('<option>' + node.url + '</option>');
                })
		        _.each(list.data.attributes.acctList, function(acct) {
                    accts.push('<option>' + acct.acctId + '</option>');
                })

                $('#enode-id').html(nodes.join(''));
                $('#account-admin').html(accts.join(''));
		    }.bind(this));

		},

        fetch: function () {
			var _this = this;

			$.when(
				utils.load({ url: this.url_list })
			).fail(function(res) {
                $('#widget-' + _this.shell.id).html(
                    _this.templateUninitialized());
            }).done(function(info) {
				var rows = [];
			    var statuses = ["Not in List", "Proposed", "Approved", "Pending Suspension", "Suspended", "Recovery initiated"];

				if (info.data.length > 0) {
					_.each(info.data, function(peer) {
					    var status = statuses[peer.attributes.status]
					    var orgId = peer.attributes.fullOrgId
					    var parentUlt = orgId != peer.attributes.ultimateParent ? peer.attributes.ultimateParent : ''
					    var parentDirect = parentUlt != peer.attributes.parentOrgId ? peer.attributes.parentOrgId : ''
					    var separator = parentDirect == '' || parentUlt == '' ? '' : '/'
					    var parent = parentUlt+separator+parentDirect
					    var disableApprove = peer.attributes.status != 1 ? 'disabled' : ''
					    var disableStatus = peer.attributes.status == 1 || peer.attributes.status == 0 || parent != '' ? 'disabled' : ''
						rows.push( _this.templateRow({
						    o: peer.attributes,
						    status: status,
						    parentUlt: parentUlt,
						    parentDirect: parentDirect,
						    disableApprove: disableApprove,
						    disableStatus: disableStatus,
						    separator: separator }) );
					});


                    $('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );
				}

				$('#widget-' + _this.shell.id + ' a').click(function(e) {
					e.preventDefault();

					Dashboard.show({ widgetId: 'permissions-detail', section: 'permissions', data: $(this).text(), refetch: true });
				});

				_this.postFetch();
			}.bind(this));
		},

		postRender: function() {
			var _this = this;

            $('#widget-' + _this.shell.id).on('click', '.add-org', function(e) {
                _this.populateFrom();


				// set the modal text
				$('#myModal .modal-content').html(_this.modalOrgTemplate({
				    header: "Add a new org to the network",
				    orgId: "",

				}) );

				//open modal
				$('#myModal').modal('show');


                $('#addOrg-btn-final').click( function() {
					var orgName = $('#org-label').val();
					var enodeId = $('#enode-id').val();
					var acctAdmin = $('#account-admin').val();
					var fromAcct = $('#from-account').val();


					$.when(
						utils.load({
							url: _this.url_addOrg,
							data: {
								"id": orgName,
								"enodeId": enodeId,
								"accountId": acctAdmin,
								"from": fromAcct
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgUpdate'], true)
						_this.fetch();

					}).fail(function(err) {
					    console.log(err)
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: err.responseJSON.errors.map((error) => error.detail)
						}) );
					});
				});

			});

            $('#widget-' + _this.shell.id).on('click', '.approve-btn', function(e) {

 			    var orgName = $(e.target.parentElement).data("orgid")
                _this.populateFrom();
                _this.populateEnode(orgName);

				// set the modal text
				$('#myModal .modal-content').html(_this.modalOrgApproveTemplate({
				    header: "Approve new org",
				    orgId: orgName,
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#addOrg-btn-final').click( function() {
					var orgName = $('#org-label').val();
					var enodeId = $('#enode-id').val();
					var acctAdmin = $('#account-admin').val();
					var fromAcct = $('#from-account').val();

					$.when(
						utils.load({
							url: _this.url_approveOrg,
							data: {
								"id": orgName,
								"enodeId": enodeId,
								"accountId": acctAdmin,
								"from": fromAcct
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgUpdate'], true)
						_this.fetch();

					}).fail(function(err) {
					    console.log(err)
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: err.responseJSON.errors.map((error) => error.detail)
						}) );
					});
				});

			});

            $('#widget-' + _this.shell.id).on('click', '.status-btn', function(e) {

				var orgId = $(e.target.parentElement).data("orgid");
				var status = $(e.target.parentElement).data("status");
				var change = status == 2 || status == 4
//				var approve = status == 3 || status == 5
				var header = change ? "Change org status" : "Approve org status change"
                _this.populateFrom();

                var action = ""
                if (status == 3 || status == 2) {
                    action = "1-Suspend"
                } else if (status == 5 || status == 4) {
                    action = "2-Activate"
                }

				// set the modal text
				$('#myModal .modal-content').html(_this.modalStatusTemplate({
				    header: header,
				    action: action
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#changeStatus-btn-final').click( function() {
					var status = $('#stat').val().split('-')[0];
					var fromAcct = $('#from-account').val();

					var urlStatus = change ? _this.url_update_status : _this.url_approve_status

					$.when(
						utils.load({
							url: urlStatus,
							data: {
								"id": orgId,
								"action": status,
								"from": fromAcct
							}
						})
					).done(function () {
						$('#myModal').modal('hide');
                        console.log("done")
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

            $('#widget-' + _this.shell.id).on('click', '.add-subOrg', function(e) {

                var orgId = $(e.target.parentElement).data("orgid");

                _this.populateFrom();

				// set the modal text
				$('#myModal .modal-content').html(_this.modalSubOrgTemplate({
				    header: "Add new suborg to " + orgId,
				    parentOrg: orgId,
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#addSubOrg-btn-final').click( function() {
					var orgName = $('#org-label').val();
					var parentOrg = $('#parent-label').val();
					var enodeId = $('#enode-id').val();
					var fromAcct = $('#from-account').val();

					$.when(
						utils.load({
							url: _this.url_subOrg,
							data: {
								"id": orgName,
								"parentId": parentOrg,
								"enodeId": enodeId,
								"from": fromAcct
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgUpdate'], true)
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
