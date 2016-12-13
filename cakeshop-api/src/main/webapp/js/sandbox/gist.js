
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    // ------------------ gist load ----------------

    Sandbox.loadFromGist = function(queryParams) {

        function getGistId(str) {
          var idr = /[0-9A-Fa-f]{8,}/;
          var match = idr.exec(str);
            if (match) {
                return match[0];
            }
          return null;
        }

      var key = queryParams.gist;
        if (!key) {
            return false;
        }

    var gistId = getGistId(key);
    var loadingFromGist = !!gistId;
      $.ajax({
        url: 'https://api.github.com/gists/' + gistId,
        jsonp: 'callback',
        dataType: 'jsonp',
            success: function(response) {
                if (response.data) {
                    for (var key in response.data.files) {
                        var content = response.data.files[key].content;
                        if (Sandbox.Filer.get(key) === content) {
                            Sandbox.activateTab(key);
                            return;
                        }
                        var fname = Sandbox.Filer.getUniqueKey(key);
                        Sandbox.addFileTab(fname, content, true);
                    }
                }
            }
      });

        return loadingFromGist;
    };

})();
