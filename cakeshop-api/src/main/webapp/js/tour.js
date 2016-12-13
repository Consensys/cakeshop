import 'jif-dashboard/dashboard-core';
import 'jif-dashboard/dashboard-util';
import 'jif-dashboard/dashboard-template';

(function () {
  var tour = new Tour({
    debug: false,
    //storage: false,
    backdrop: true,
    container: "body",
    backdropContainer: "body",
    onEnd: function() {
      //for autostart
    	window.localStorage.setItem('tourEnded', true);
    },

    steps: [].concat([
      {
        element: "div.tower-logo-container",
        title: "Welcome to the Cakeshop!",
        content: "Let's start with a brief tour",
        container: ".tower-navigation",
        backdropContainer: ".tower-navigation",
        onShow: function() { },
      },
      {
        element: ".tower-sidebar ul",
        content: "This is the main navigation menu, where you can access the other parts of the tool",
        onShow: function() {
          $(".tower-navigation").css({"z-index": 1100});
        },
        onHide: function() {
          $(".tower-navigation").css({"z-index": 10000});
        },
      },
    ])
    .concat([
      //------------------------------------------------------------------------
      // CONSOLE
      {
        element: "#console",
        title: "Console",
        content: "You're currently looking at the console, which gives you an overview of the blockchain node running on the local system",
        backdropContainer: ".tower-sidebar",
        onShow: showMenuStep("#console"),
        onHide: hideMenuStep,
      },
      {
        element: "#heads-up",
        content: "Here are some simple metrics which are always available, such as the node's status, number of connected peers, current block number, and pending transaction count",
        placement: "bottom",
        onShow: showMenuStep("#console"),
        onHide: hideMenuStep,
      },
      {
        element: "#grounds",
        content: "This is the main application area and is composed of a number of widgets. They can be reordered and resized as needed",
        placement: "top",
        onShow: showMenuStep("#console"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.node-info",
        content: "This widget shows a snapshot of the node, including configuration details and some statistics",
        placement: "bottom",
        onShow: showMenuStep("#console"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.node-control",
        content: "From here you can start, stop, restart and even reset the node back to a fresh state",
        placement: "bottom",
        onShow: showMenuStep("#console"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.node-settings",
        content: "Here you can alter various settings of the underlying node",
        placement: "bottom",
        onShow: showMenuStep("#console"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.metrix-txn-sec",
        content: "Transactions committed per second",
        placement: "top",
        onShow: showMenuStep("#console"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.metrix-txn-min",
        content: "Transactions committed per minute",
        placement: "top",
        onShow: showMenuStep("#console"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.metrix-blocks-min",
        content: "Blocks generated per minute",
        placement: "top",
        onShow: showMenuStep("#console"),
        onHide: hideMenuStep,
      },
    ])
    .concat([
      //------------------------------------------------------------------------
      // CONTRACTS
      {
        element: "#contracts",
        title: "Contracts",
        content: "The contracts tab gives you a high-level contract-oriented view of the data on the chain",
        backdropContainer: ".tower-sidebar",
        onShow: showMenuStep("#contracts"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.contract-list",
        content: "Here we have a list of contracts deployed on the chain (via the Cakeshop APIs)",
        placement: "bottom",
        onShow: showMenuStep("#contracts"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.contract-detail",
        content: "Metadata for the selected contract, such as the ABI and the original source code",
        placement: "bottom",
        onShow: loadWidget("#contracts", "contract-detail", ".widget-shell.contract-list tbody tr:first button.deets"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.contract-paper-tape",
        content: "The paper tape shows the transaction history for the selected contract",
        placement: "bottom",
        onShow: loadWidget("#contracts", "contract-paper-tape", ".widget-shell.contract-list tbody tr:first button.tape"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.contract-current-state",
        content: "Here we can see the current state of the contract, as read from any exposed public variables (methods marked 'constant' in solidity)",
        placement: "bottom",
        onShow: loadWidget("#contracts", "contract-current-state", ".widget-shell.contract-list tbody tr:first button.state"),
        onHide: hideMenuStep,
      },
    ])
    .concat([
      //------------------------------------------------------------------------
      // CHAIN EXPLORER
      {
        element: "#explorer",
        title: "Chain Explorer",
        content: "This is the chain explorer, a low-level view of the data stored on the blockchain shown in terms of raw blocks and transactions",
        backdropContainer: ".tower-sidebar",
        onShow: showMenuStep("#explorer"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.block-list",
        content: "List of Blocks, in reverse chronological order. This list will update in realtime",
        placement: "bottom",
        onShow: showMenuStep("#explorer"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.block-detail",
        content: "This shows the detailed headers stored with each block and contains links to any transactions that were committed in the block as well",
        placement: "bottom",
        onShow: showMenuStep("#explorer"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.block-view",
        content: "Search for a block or transaction",
        placement: "bottom",
        onShow: showMenuStep("#explorer"),
        onHide: hideMenuStep,
      },
    ])
    .concat([
      //------------------------------------------------------------------------
      // WALLET
      {
        element: "#wallet",
        title: "Wallet",
        content: "The wallet view shows a list of available accounts on the node and their associated balances. You can also create new accounts for testing purposes.",
        backdropContainer: ".tower-sidebar",
        onShow: showMenuStep("#wallet"),
        onHide: hideMenuStep,
      },
    ])
    .concat([
      //------------------------------------------------------------------------
      // PEERS
      {
        element: "#peers",
        title: "Peers",
        content: "The peers section makes it easy to create and manage local blockchain clusters for testing purposes",
        backdropContainer: ".tower-sidebar",
        onShow: showMenuStep("#peers"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.peers-list",
        content: "A list of peers connected to the node, if any",
        placement: "bottom",
        onShow: showMenuStep("#peers"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.peers-neighborhood",
        content: "In order to make setting up a cluster as easy as possible, we scan the local subnet for available nodes and display them here",
        placement: "bottom",
        onShow: showMenuStep("#peers"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.peers-add",
        content: "Manually connect to a peer by entering its Node URL here (See Node Info on the Console tab)",
        placement: "bottom",
        onShow: showMenuStep("#peers"),
        onHide: hideMenuStep,
      },
    ])
    .concat([
      //------------------------------------------------------------------------
      // API DOCS
      {
        element: "#api",
        title: "API Documentation",
        content: "This dashboard and all related tools are built using a set of RESTful APIs. These APIs provide a friendly interface for interacting with the blockchain and abstract away some of the complexity",
        backdropContainer: ".tower-sidebar",
        onShow: showMenuStep("#api"),
        onHide: hideMenuStep,
      },
    ])

  });

  function loadWidget(tab, widget, click_sel) {
    return function() {
      return new Promise(function(resolve, reject) {
        showMenuStep(tab)();
        if ($(".widget-shell." + widget).length !== 0) {
          return resolve();
        }
        // load it
        $(document).on("WidgetInternalEvent", function(e, action) {
          if (action === "widget|rendered|" + widget) {
            resolve();
            $(document).off(e);
          }
        });
        $(click_sel).click();
      });
    };
  }

  function showMenuStep(id) {
    return function() {
      return new Promise(function(resolve, reject) {
        if (!$(id).hasClass("active")) {
          $(id).click();
        }
        $(".tower-navigation").css({"z-index": 1100});
        $(".tower-sidebar").css({"z-index": 1100});
        resolve();
      });
    };
  }

  function hideMenuStep() {
    $(".tower-navigation").css({"z-index": 10000});
    $(".tower-sidebar").css({"z-index": 9999});
  }

  // Initialize the tour
  tour.init();


  var loaded = false;
 // $(document).on("WidgetInternalEvent", function(e, action) {
  $(document).on("StartTour", function(e, action) {
   //if (action === "node-status|announce" && loaded === false) {
	   Tower.tour = tour;
	   window.localStorage.setItem("tour_current_step", 0); // always reset to 0
	   tour.start();
	   loaded = true;

    //}
  });

})();
