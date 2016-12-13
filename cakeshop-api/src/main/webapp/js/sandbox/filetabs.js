
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    var SOL_CACHE_FILE_PREFIX = 'sol-cache-file-';
    Sandbox.SOL_CACHE_FILE = null;

    var Filer = Sandbox.Filer = {
        untitledCount: 0,

        init: function() {
            var u = localStorage.untitled_count;
            if (u !== undefined && u !== null) {
                this.untitledCount = parseInt(u);
            }
        },

        newUntitledKey: function() {
            localStorage.untitled_count = ++this.untitledCount;
            return "Untitled " + this.untitledCount;
        },

        getUniqueKey: function(prefix) {
            if (!this.get(prefix)) {
                return prefix;
            }
            var i = 0;
            while (true) {
                i++;
                if (!this.get(prefix + " " + i)) {
                    return prefix + " " + i;
                }
            }
        },

        // Get a list of file names (without prefix)
        list: function() {
            var files = [];
            for (var f in localStorage) {
                if (f.startsWith(SOL_CACHE_FILE_PREFIX) === true) {
                    files.push(f.replace(SOL_CACHE_FILE_PREFIX, ""));
                }
            }
            files.sort(function(a, b) {
                return a.localeCompare(b);
            });
            return files;
        },

        // Get the source for a given file key
        get: function(key) {
            if (_.isNumber(key)) {
                key = this.list()[key];
            }
            if (localStorage[key]) {
                return localStorage[key];
            }
            return localStorage[SOL_CACHE_FILE_PREFIX + key];
        },

        // Add a new file to storage
        add: function(key, source) {
            localStorage[SOL_CACHE_FILE_PREFIX + key] = source;
        },

        remove: function(key) {
            localStorage.removeItem(SOL_CACHE_FILE_PREFIX + key);
        },

        getActiveFile: function() {
            return localStorage.active_file;
        },

        setActiveFile: function(file) {
            localStorage.active_file = file;
        },

        saveActiveFile: function(source) {
            this.add(this.getActiveFile(), source);
        },
    };
    Filer.init();

    var editor;

    function initFileTabs() {
        editor = Sandbox.editor;

        var queryParams = Sandbox.queryParams();
        var loadingFromGist = Sandbox.loadFromGist(queryParams);

        var loadingFromUrl = false;
        if (queryParams.contract && queryParams.contract.startsWith("0x")) {
            loadingFromUrl = true;
            Contract.get(queryParams.contract).then(function(contract) {
                var fname = contract.get("name") + " " + contract.id.substring(0, 10);
                if (Filer.get(fname)) {
                    return activateTab(fname);
                }
                Sandbox.addFileTab(fname, contract.get("code"), true);
            });
        }

        if (!(loadingFromGist || loadingFromUrl) && Filer.list().length === 0) {
            // Load default file
            Sandbox.loadContract("Ballot.txt").then(function(source) {
                Filer.add(Filer.newUntitledKey(), source);
                drawFileTabs();
                activateTab(Filer.list()[0]); // activate first tab
            });
        }

        // ----------------- file selector-------------
        var $filesEl = $('ul.filetabs');
        $filesEl.on('click','.new_file', function() {
            var key = Filer.newUntitledKey();
            Filer.add(key, "");
            addFileTab(key, true);
        });

        $filesEl.on('click', '.file:not(.active)', function(e) {
            e.preventDefault();
            var filename = $(this).find('.name').text();
            activateTab(filename);
            return false;
        });

        $filesEl.on('click', '.file.active', function(ev) {
        	var $fileTabEl = $(this);
        	var originalName = $fileTabEl.find('.name').text();
        	ev.preventDefault();
        	if ($(this).find('input').length > 0) return false;
        	var $fileNameInputEl = $('<input value="'+originalName+'"/>');
        	$fileTabEl.html($fileNameInputEl);
        	$fileNameInputEl.focus();
        	$fileNameInputEl.select();
        	$fileNameInputEl.on('blur', handleRename);
        	$fileNameInputEl.keyup(handleRename);

        	function handleRename(ev) {
        		ev.preventDefault();
        		if (ev.which && ev.which !== 13) return false;
        		var newName = ev.target.value;
        		$fileNameInputEl.off('blur');
        		$fileNameInputEl.off('keyup');

    			var content = Filer.get(originalName);
        		if (newName !== originalName && confirm("Are you sure you want to rename: " + originalName + " to " + newName + '?')) {
                    // rename tab/file
        			Filer.add(newName, content);
                    Filer.remove(originalName);
                    Filer.setActiveFile(newName);
        		}

                drawFileTabs();
                activateTab(Filer.getActiveFile());
                Filer.saveActiveFile(content); // save again, workaround for zero-out bug

        		return false;
        	}

        	return false;
        });

        $filesEl.on('click', '.file .remove', function(e) {
        	e.preventDefault();

        	var name = $(this).parent().find('.name').text();
        	var index = Filer.list().indexOf(name);

        	if (confirm("Are you sure you want to remove: " + name + " from local storage?")) {
                Filer.remove(name);
                getFileTab(name).remove();
                activateTab(Filer.list()[ Math.max(0, index - 1) ]); // activate previous file in list
        	}
        	return false;
        });

        if (Filer.list().length > 0) {
            drawFileTabs();
            activateTab(Filer.list()[0]); // activate first tab
        }
    }

    function getFileTab(key) {
    	return $('.filetabs li').filter(function(){ return $(this).find('.name').text() === key; });
    }

    function activateTab(filename) {
        Filer.setActiveFile(filename);
        var t = getFileTab(filename);
        $(".filetabs li").removeClass("active");
        t.addClass("active");
		editor.setValue(Filer.get(filename), -1);
		editor.focus();
    }

    function addFileTab(filename, activate) {
        var t = $('<li role="presentation" class="file"><a role="tab" data-toggle="tab"><span class="name">' + filename + '</span> <span class="remove"><i class="fa fa-close red"></i></span></a></li>');
		$(".filetabs").append(t);
        if (activate === true) {
            activateTab(filename);
        }
    }

    function drawFileTabs() {
    	var files = Filer.list();

    	$(".filetabs li.file").remove();
    	files.forEach(function(filename) {
            addFileTab(filename);
        });
    }

    // export methods
    Sandbox.initFileTabs = initFileTabs;
    Sandbox.activateTab = activateTab;

    Sandbox.addFileTab = function(filename, source, activate) {
        Filer.add(filename, source);
        addFileTab(filename, activate);
        Filer.add(filename, source);
    };

})();
