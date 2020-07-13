import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'permissions-list',
		title: 'Permissions List',
		size: 'large',

		url_list: 'api/permissions/getList',
		url_add: 'api/permissions/addOrg',
		url_approve: 'api/permissions/approveOrg',
		url_update_status: 'api/permissions/updateOrgStatus',
		url_approve_status: 'api/permissions/approveOrgStatus',
		url_subOrg: 'api/permissions/addSubOrg',

		topic: '/topic/block',

		hideLink: true,

		template: _.template(
            + '<div class="form-group pull-right">'
		    + '	<label for="fr-account">From Account</label>'
            + '	<select id="fr-account" class="form-control" style="transition: none;"> </select>' +
		    + '</div>'
		    + '<div>'
		    + '<table style="width: 100%; table-layout: fixed;" class="table table-striped">'
		    + ' <thead style="font-weight: bold;">'
		    + '     <tr>'
			+ '			<td class="org-id">OrgId</td>'
			+ '         <td class="org-status" style="width:30px;">Status</td>'
		    + '         <td class="status-col"></td>'
            + '         <td class="parent-id" style="width:90px;">ParentOrgId</td>'
            + '         <td class="ultimate-parent" style="width:90px;">UltimateParent</td>'
            + '         <td class="approve-col"></td>'
		    + '     </tr>'
		    + ' </thead>'
		    + '<tbody><%= rows %></tbody>'
		    + '</table>'
		    + '</div>'
            + '<div class="form-group pull-right">'
			+ '	<button class="btn btn-primary add-org">Add New Org</button>'
			+ ' <button class="btn btn-primary add-subOrg">Add New SubOrg</button>'
			+ '</div>'
		),

		 templateUninitialized: _.template(
            '<div>'
            + '<h3 style="text-align: center;margin-top: 70px;">Permissioning not enabled</h3>'
            + '</div>'
         ),

		templateRow: _.template('<tr>'
		    + '<td class="value org-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap;"><a href="#"><%= o.fullOrgId %></a></td>'
		    + '<td class="value org-status" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= o.status %></td>'
		    + '<td data-orgid="<%= o.fullOrgId %>" class="status-col">'
            +   '<button class="btn btn-default status-btn">Update/Approve</button>'
            + '</td>'
            + '<td class="value parent-id" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><a href="#"><%= o.parentOrgId %></a></td>'
            + '<td class="value ultimate-parent" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><a href="#"><%= o.ultimateParent %></a></td>'
		    + '<td data-orgid="<%= o.fullOrgId %>" class="approve-col">'
            +   '<button class="btn btn-default approve-btn">Approve Org</button>'
            + '</td>'
		    + '</tr>'
		),

		modalOrgTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group add-org-form">' +
			'		<label for="org-label">Org Name</label>' +
			'		<input type="text" class="form-control" id="org-label" placeholder="<%=orgId%>">' +
			'		<label for="enode-id">Enode id</label>' +
			'		<input type="text" class="form-control" id="enode-id">' +
			'		<label for="account-admin">Account Admin</label>' +
            '		<input type="text" class="form-control" id="account-admin">' +
            '		<label for="from-account">From Account</label>' +
            '		<input type="text" class="form-control" id="from-account" placeholder="<%=fromAccount%>">' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="addOrg-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

		modalSubOrgTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'<div class="modal-body">' +
			'	<div class="form-group add-org-form">' +
			'		<label for="org-label">Org Name</label>' +
			'		<input type="text" class="form-control" id="org-label">' +
			'		<label for="parent-label">Parent Org</label>' +
            '		<input type="text" class="form-control" id="parent-label">' +
			'		<label for="enode-id">Enode id</label>' +
			'		<input type="text" class="form-control" id="enode-id">' +
            '		<label for="from-account">From Account</label>' +
            '		<input type="text" class="form-control" id="from-account" placeholder="<%=fromAccount%>">' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="addSubOrg-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),

		modalStatusTemplate: _.template( '<div class="modal-header">' +
			'	<%=addOrg%>' +
			'</div>' +
			'<div class="modal-body">' +
            '  <div class="radio">' +
			'    <label>' +
			'      <input type="radio" id="searchType" name="searchType" value="update" checked="checked"/>' +
			'      Update' +
			'    </label>' +
			'  </div>' +
			'  <div class="radio">' +
			'    <label>' +
			'      <input type="radio" id="searchType" name="searchType" value="approve"/>' +
			'      Approve' +
			'    </label>' +
			'  </div>' +
			'	<div class="form-group add-org-form">' +
			'		<label for="status">Status</label>' +
			'		<input type="text" class="form-control" id="status">' +
		    '		<label for="from-account">From Account</label>' +
		    '		<input type="text" class="form-control" id="from-account" placeholder="<%=fromAccount%>">' +
			'	</div>' +
			'</div>' +
			'<div class="modal-footer">' +
			'	<button type="button" id="changeStatus-btn-final" class="btn btn-primary">Yes, <%=addOrg%>.</button>' +
			'</div>'),


        modalConfirmation: _.template('<div class="modal-body"><%=message%></div>'),

		subscribe: function() {
			Dashboard.Utils.on(function(e, action) {
				if (action[0] == 'orgUpdate') {
					this.fetch();
				}
			}.bind(this));
		},

        fetch: function () {
			var _this = this;

			$.when(
				utils.load({ url: this.url_list })
			).done(function(info) {
			    console.log(info)
				var rows = [];

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

            Account.list().then(function(accounts) {
				var rows = ['<option>Choose Account</option>'];

				accounts.forEach(function(acct) {
					if (acct.get('unlocked')) {
						//only add unlocked accounts
						rows.push( '<option>' + acct.get('address') + '</option>' );
					}
				});

				console.log('rows')
				console.log(rows)

				this._$('#fr-account')
					.html( rows.join('') );
			}.bind(this));
		},

		postRender: function() {
			var _this = this;

            $('#widget-' + _this.shell.id).on('click', '.add-org', function(e) {
                var from = $('#widget-' + _this.shell.id + ' #fr-account').val();

				// set the modal text
				$('#myModal .modal-content').html(_this.modalOrgTemplate({
				    addOrg: "addOrg",
				    orgId: "",
				    fromAccount: from

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
							url: _this.url_add,
							data: {
								"id": orgName,
								"enodeId": enodeId,
								"accountId": acctAdmin,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgUpdate'], true)
						_this.fetch();

					}).fail(function() {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Sorry, Please try again.'
						}) );
					});
				});

			});

            $('#widget-' + _this.shell.id).on('click', '.approve-btn', function(e) {

                var from = $('#widget-' + _this.shell.id + ' #fr-account').val();

 		        console.log('parentel');
 			    console.log($(e.target.parentElement).data);

 			    var orgName = $(e.target.parentElement).data("orgid")

				// set the modal text
				$('#myModal .modal-content').html(_this.modalOrgTemplate({
				    addOrg: "approveOrg",
				    orgId: orgName,
				    fromAccount: from
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
							url: _this.url_approve,
							data: {
								"id": orgName,
								"enodeId": enodeId,
								"accountId": acctAdmin,
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgUpdate'], true)
						_this.fetch();

					}).fail(function() {
						$('#myModal .modal-content').html(_this.modalConfirmation({
							message: 'Sorry, Please try again.'
						}) );
					});
				});

			});

         $('#widget-' + _this.shell.id).on('click', '.status-btn', function(e) {

				var orgId = $(e.target.parentElement).data("orgid");
				var from = $('#widget-' + _this.shell.id + ' #fr-account').val();

				// set the modal text
				$('#myModal .modal-content').html(_this.modalStatusTemplate({
				    addOrg: "changeStatus",
				    fromAccount: from
				}) );

				//open modal
				$('#myModal').modal('show');


                $('#changeStatus-btn-final').click( function() {
					var status = $('#status').val();
					var type = $('#searchType:checked').val();

					console.log(type)

					var urlStatus = type == "update" ? _this.url_update_status : _this.url_approve_status

					console.log(urlStatus)

					$.when(
						utils.load({
							url: urlStatus,
							data: {
								"id": orgId,
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

            $('#widget-' + _this.shell.id).on('click', '.add-subOrg', function(e) {

            var from = $('#widget-' + _this.shell.id + ' #fr-account').val();

				// set the modal text
				$('#myModal .modal-content').html(_this.modalSubOrgTemplate({
				    addOrg: "addSubOrg",
				    fromAccount: from
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
								"f": {"from": fromAcct}
							}
						})
					).done(function () {
						$('#myModal').modal('hide');

						Dashboard.Utils.emit(['orgUpdate'], true)
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
