
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

    // subscribe to node status
    var is_mining = false;
    Node.subscribe(function(node) {
        if (node.get("status") !== "running") {
            updateStatus("n/a", false);
            return;
        }
        updateStatus("#" + node.get("latestBlock"), node.get("config").committingTransactions);
    });

    Client.on("stomp:disconnect", function() {
        updateStatus("n/a", false);
    });

    function updateStatus(block, status) {
        is_mining = status;
        if (block) {
            $("nav.sidenav li.block_number a").text(block);
        }
        if (status === true) {
            $("nav.sidenav li.mining_status i").attr({class: "fa fa-refresh fa-spin"});
            $("nav.sidenav li.mining_status a").css({color: "#4FC922"});
        } else {
            $("nav.sidenav li.mining_status i").attr({class: "fa fa-pause"});
            $("nav.sidenav li.mining_status a").css({color: "#777"});
        }
    }

    $("nav.sidenav li.mining_status a").click(function(e) {
        Node.update({committingTransactions: !is_mining}).then(function(node) {
            updateStatus("#" + node.get("latestBlock"), node.get("config").committingTransactions);
        });
    });

    $("nav.sidenav li.mining_status a").hover(
        function(e) { // mouseenter
            if (is_mining) {
                // show pause button
                $("nav.sidenav li.mining_status i").attr({class: "fa fa-pause"});
                $("nav.sidenav li.mining_status a").css({color: "#FF0000"});

            } else {
                $("nav.sidenav li.mining_status i").attr({class: "fa fa-play"});
                $("nav.sidenav li.mining_status a").css({color: "#4FC922"});
            }
        },
        function(e) { // mouseleave
            updateStatus(null, is_mining);
        }
    );

})();
