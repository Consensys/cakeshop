/**
 *	Widget template.
 *
 *	Loading flow:
 *		1. Register in a section for displaying
 *		2. Widget is pulled down
 *		3. addWidget called on screenManager
 *		4. screenManager calls widget.init
 *			5. widget.setData is called by widget.init
 *			6. widget.initialized is toggled
 *			7. widget.ready is called by widget.init
 *				8. widget.render is called by widget.init
 *					9. widget.fetch is called by widget.render
 *					10. widget.postRender is called by widget.render
 *			11. widget.subscribe is called by widget.init
 */
(function() {
	var extended = {
		name: 'widget-id',
		title: 'Widget Name That Appears on it',
		size: 'medium', // 'small', 'medium', 'large', 'third'

		// url: 'TBD', // fill if needed

		template: _.template('<ul class="widget-node-control">'+ // internal template
				'<li>List item 1</li>'+
				'<li>List item 2</li>'+
				'<li>List item 2</li>'+
				'<li>List item 2</li>'+
			'</ul>'),

		postRender: function() { // executed when placing on screen
			$('#widget-' + this.shell.id).html(this.template({}));
			$('#widget-' + this.shell.id + ' button').click(this._handler);
		},

		_handler: function(ev) {

		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
})();
