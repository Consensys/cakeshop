
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};
    _.extend(Sandbox, Backbone.Events);

    function trimBase64Nulls(s) {
        for (var i = 0; i < s.length; i++) {
            if (s.charCodeAt(i) === 0) {
                return s.substring(0, i);
            }
        }
        return s; // no nulls?
    }

    Sandbox.decodeBytes = function(val) {
    	var useB64 = document.querySelector('#base64').checked;
        if (!useB64) {
            return val;
        }
        return trimBase64Nulls(Base64.decode(val).trim());
    };

    Sandbox.encodeBytes = function(val) {
    	var useB64 = document.querySelector('#base64').checked;
        if (!useB64) {
            return val;
        }
        return Base64.encode(val);
    };

    Sandbox.queryParams = function() {
        var params = {};
        var pairs = window.location.search.substr(1).split("=");
        while (pairs.length > 0) {
            params[pairs.shift()] = pairs.shift();
        }
        return params;
    };

    jQuery.fn.selectText = function() {
    	var doc = document,
    	 element = this[0],
         range;

    	if (doc.body.createTextRange) {
    		range = document.body.createTextRange();
    		range.moveToElementText(element);
    		range.select();
    	} else if (window.getSelection) {
    		var selection = window.getSelection();
    		range = document.createRange();
    		range.selectNodeContents(element);
    		selection.removeAllRanges();
    		selection.addRange(range);
    	}
    };

})();
