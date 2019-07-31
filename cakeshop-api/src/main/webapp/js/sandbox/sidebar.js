
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    Sandbox.getActiveSidebarTab = function() {
        return getTabName($("ul.sidenav li.active"));
    };

    function getTabName(tab) {
        var classes = tab.attr("class").split(/ /);
        if (classes.length > 1) {
            classes = _.reject(classes, function(c) { return c === "active"; });
        }
        return classes[0];
    }

    $("ul.sidenav li a").click(function(e) {
        var tab = getTabName($(e.target).parents("li"));

        if (tab === "help") {
          return;
        }

        $("ul.sidenav li").removeClass("active");
        $("ul.sidenav li."+tab).addClass("active");
        $(".sidebar .tab").hide();
        $(".sidebar #"+tab).show();

        if (tab === "txView") {
            Sandbox.showTxView();
        }

    });

    Client.on("stomp:disconnect", function() {
        updateBlockNumber("n/a");
    });

    function onNodeStatusUpdate(node) {
        console.log("node status", node)
        if (node.get("status") !== "running") {
            updateBlockNumber("n/a");
            return;
        }
        updateBlockNumber("#" + node.get("latestBlock"));
    }

    function updateBlockNumber(block) {
        if (block) {
            $("nav.sidenav li.block_number a").text(block);
        }
    }

    // subscribe to node status
    Node.subscribe(onNodeStatusUpdate);
    // get initial status
    Node.get().then(onNodeStatusUpdate);
})();
