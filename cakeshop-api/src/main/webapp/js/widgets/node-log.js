import utils from '../utils';
import CodeMirror from 'codemirror/lib/codemirror';

module.exports = function() {
	var extended = {
		name: 'node-log',
		title: 'Node Log',
		size: 'medium',
		stream: false,

		hideLink: true,

		// template: _.template('<textarea id="log-editor" rows="4" cols="50"> </textarea>'),
		template: _.template('<div style="text-align: center;padding-top: 70px;"><button class="btn btn-lg btn-primary">Start Log Streaming</button></div>'),

		subscribe: function() {
			if (this.stream) {
				this.logSub = utils.subscribe('/topic/log/geth', this.onData);

				$(window).on('beforeunload', function() {
					this.logSub.unsubscribe();
				}.bind(this));
			}
		},

		onData: function(data) {
			console.log(widget.$log.lineCount());

			// var p = widget.$log ? '\n' + widget.$log.getDoc().getValue() : '';
			// widget.$log.getDoc().setValue(data.data.attributes.result + p );

			widget.$log.replaceRange('\n' + data.data.attributes.result, CodeMirror.Pos(0));
		},

		renderCodeArea: function() {
			this.stream = true;

			this._$().html('<textarea id="log-editor" rows="4" cols="50"> </textarea>');

			this.$log = CodeMirror.fromTextArea(this._$('#log-editor')[0], {
				lineWrapping: true,
				lineNumbers: true,
				readOnly: 'nocursor'
			});

			this.subscribe();
			// this._$('.CodeMirror').height(200);
		},

		render: function() {
			Dashboard.render.widget(this.name, this.shell.tpl);

			this.fetch();

			this._$().css({
				'height': '240px',
				'margin-bottom': '10px',
				'overflow-x': 'hidden',
				'width': '100%'
			}).html(this.template());

			this._$('button').on('click', function() {
				this.renderCodeArea();
			}.bind(this));

			this.postRender();
			$(document).trigger('WidgetInternalEvent', ['widget|rendered|' + this.name]);
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
