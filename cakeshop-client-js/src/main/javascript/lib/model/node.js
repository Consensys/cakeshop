
(function() {

    var Node = window.Node = Backbone.Model.extend({

        urlRoot: "api/node",
        url: function(path) {
            return this.urlRoot + (path ? "/" + path : "");
        },

        initialize: function() {
            this.id = this.get("id");
        }

    });

    // Subscribe to Node status changes
    Node.subscribe = function(handler) {
        Client.subscribe("/topic/node/status", function(res) {
            handler(new Node(res.data.attributes));
        });
    };

    Node.get = function() {
        return new Promise(function(resolve, reject) {
            Client.post(Node.prototype.url('get')).
                done(function(res, status, xhr) {
                    resolve(new Node(res.data.attributes));
                });
        });
    };

    Node.update = function(settings) {
        return new Promise(function(resolve, reject) {
            Client.post(Node.prototype.url('update'), settings).
                done(function(res, status, xhr) {
                    resolve(new Node(res.data.attributes));
                });
        });
    };

})();
