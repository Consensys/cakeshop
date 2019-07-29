var path = require('path');
var webpack = require('webpack');


module.exports = {
	entry: {
        index: './js/index.js',
        manage: "./js/manage.js",
        sandbox: [
            "./js/sandbox/init.js",
            "./js/sandbox/lib.js",
            "./js/sandbox/tx.js",
            "./js/sandbox/filetabs.js",
            "./js/sandbox/editor.js",
            "./js/sandbox/sidebar.js",
            "./js/sandbox/gist.js",
            "./js/sandbox/tour.js",
            "./js/sandbox.js"
        ]
    },
	output: {
		path: path.join(__dirname, 'js'),
		filename: '[name]-gen.js',
		publicPath: '/js/'
	},
	devServer: {
		historyApiFallback: true,
		hot: true,
		inline: true,
		stats: 'errors-only',
		port: 7999
	},
	plugins: [
		new webpack.ProvidePlugin({
			$: 'jquery',
			jQuery: 'jquery',
			'window.jQuery': 'jquery',
			d3: 'd3',
			Backbone: 'backbone',
			_: 'underscore',
			SockJS: 'sockjs-client',
			moment: 'moment',
		}),
	],
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                include: [
                    path.resolve(__dirname, 'js'),
                    path.resolve(__dirname, 'node_modules/jif-dashboard'),
                ],
                exclude: /node_modules/,
                use: ['babel-loader']
            }
        ]
    }
};
