
(function() {

    var Account = window.Account = Backbone.Model.extend({

        urlRoot: "api/wallet",
        url: function(path) {
            return this.urlRoot + (path ? "/" + path : "");
        },

        initialize: function() {
            this.id = this.get("address");
        },

        humanBalance: function() {
            var b = parseInt(this.get("balance"), 10) / 1000000000000000000;
            return (b > 1000000000) ? '∞' : b.toFixed(2);
        }

    });

    Account.list = function() {
        return new Promise(function(resolve, reject) {
            Client.post(Account.prototype.url('list')).
                done(function(res, status, xhr) {
                    if (res.data && _.isArray(res.data)) {
                        var accounts = [];
                        res.data.forEach(function(d) {
                            var c = new Account(d.attributes);
                            accounts.push(c);
                        });
                        resolve(accounts);
                    }
                });
        });
    };

})();
