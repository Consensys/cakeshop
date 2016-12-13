
/**
* CONTRACT LIBRARY
*/

(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    Sandbox.loadContract = function(name) {
        var url = "js/sandbox/contracts/" + name;
        return new Promise(function(resolve, reject) {
            $.get(url).done(function(data) {
                resolve(data);
            });
        });
    };

    $("#libView a.contract").click(function(e) {
        var name = $(e.target).data("name");

        Sandbox.loadContract(name + ".txt").then(function(source) {
            if (!source) {
                console.log("no source?!");
                return;
            }

            var fname = name;
            var i = 0;
            while (Sandbox.Filer.get(fname) !== undefined) {
                i++;
                fname = name + " " + i;
            }
            Sandbox.addFileTab(fname, source, true);
        });
    });

})();
