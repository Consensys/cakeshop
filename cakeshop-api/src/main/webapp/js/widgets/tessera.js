import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'tessera',
		title: 'Tessera',
		size: 'small',

		hideLink: true,

		url: 'api/tessera/upcheck',


		template: _.template('<div class="row" id="heads-up">'+
                          '<div class="col-lg-12 col-xs-6">' +
                          '<div class="tower-nugget tower-txt-success">'+
                          '<i class="fa fa-play"></i>' +
                          '<span class="heading">Tessera Status</span>'+
                          '<span class="value default" id="default-Tnode-status">'+
                           '</span></div></div>'),


		fetch: function() {
			var _this = this;

      console.log("Fetching tessera node from url "+this.url);
			$.when(
				utils.load({ url: this.url })
			).fail(function(res) {
       		console.log("failed response");
       		console.log(res);
      }).done(function(info) {
				  console.log("Result from the fetch ***");
				  var response = info.data.attributes;

				  console.log(info);
				  console.log(response.result);
				  console.log(_this.template({}));
           $('#widget-' + _this.shell.id).html( _this.template({}) );
            _this._$('#default-Tnode-status').innerHtml = response.result;
            //$('#default-Tnode-status').innerHtml =  response.result;
          _this.postFetch();
			}.bind(this));
		},
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
