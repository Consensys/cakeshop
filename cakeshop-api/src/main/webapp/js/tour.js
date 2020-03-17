import 'jif-dashboard/dashboard-core'
import 'jif-dashboard/dashboard-util'
import 'jif-dashboard/dashboard-template'

(function () {
  var tour = new Tour({
    framework: 'bootstrap3',
    debug: false,
    showProgressBar: false,
    showProgressText: false,
    backdrop: true,
    container: "body",
    backdropContainer: "body",
    backdropOptions: {
        highlightOpacity: 0.0, // hightlight animation calulations are off, not needed anyway
        highlightColor: '#FFF',
        backdropSibling: false,
    },
    onEnd: function() {
      //for autostart
    	window.localStorage.setItem('tour_end', true);
    },


    steps: [].concat([
      {
        element: "div.tower-logo-container",
        title: "Welcome to the Cakeshop!",
        content: "Let's start with a brief tour",
        container: ".tower-navigation",
        backdrop: false,
        backdropContainer: ".tower-navigation",
        onShow: function() { },
      },
      {
        element: ".tower-sidebar ul",
        content: "This is the main navigation menu, where you can access the other parts of the tool",
        backdropContainer: ".tower-sidebar",
          backdrop: false,
        onShow: function() {
        },
        onHide: function() {
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
            backdrop: false,
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
      // MANAGED NODE
      {
        element: "#managed",
        title: "Managed Node",
        content: "The managed node tab gives the ability to have cakeshop run its own local Quorum node",
        backdropContainer: ".tower-sidebar",
          backdrop: false,
        onShow: showMenuStep("#managed"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.node-control",
        content: "From here you can start, stop, restart and even reset the node back to a fresh state",
        placement: "top",
        onShow: showMenuStep("#managed"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.node-settings",
        content: "Here you can alter various settings of the underlying node",
        placement: "top",
        onShow: showMenuStep("#managed"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.node-log",
        content: "Here you can stream the logs of the managed node",
        placement: "top",
        onShow: showMenuStep("#managed"),
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
          backdrop: false,
        onShow: showMenuStep("#contracts"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.contract-list",
        content: "Here we have the contract registry, which tracks the contracts deployed on the chain",
        placement: "top",
        onShow: showMenuStep("#contracts"),
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
          backdrop: false,
        onShow: showMenuStep("#explorer"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.block-list",
        content: "List of Blocks, in reverse chronological order. This list will update in realtime",
        placement: "top",
        onShow: showMenuStep("#explorer"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.block-detail",
        content: "This shows the detailed headers stored with each block and contains links to any transactions that were committed in the block as well",
        placement: "top",
        onShow: showMenuStep("#explorer"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.block-view",
        content: "Search for a block or transaction",
        placement: "top",
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
          backdrop: false,
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
          backdrop: false,
        onShow: showMenuStep("#peers"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.peers-list",
        content: "A list of peers connected to the node, if any",
        placement: "top",
        onShow: showMenuStep("#peers"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.peers-neighborhood",
        content: "In order to make setting up a cluster as easy as possible, we scan the local subnet for available nodes and display them here",
        placement: "top",
        onShow: showMenuStep("#peers"),
        onHide: hideMenuStep,
      },
      {
        element: ".widget-shell.peers-add",
        content: "Manually connect to a peer by entering its Node URL here (See Node Info on the Console tab)",
        placement: "top",
        onShow: showMenuStep("#peers"),
        onHide: hideMenuStep,
      },
    ])
    .concat([
      // ------------------------------------------------------------------------
      // Version info
      {
        element: ".tower-sidebar .version-info:first-of-type",
        title: "Thanks for using Cakeshop!",
        content: "Here you can see the current version of Cakeshop along with the build hash that matches the git commit the produced this build. Please reach out to us with any feedback or support requests.",
        backdrop: false,
        backdropContainer: ".tower-sidebar",
        onShow: () => {
        },
        onHide: () => {
            // for some reason clicking the disabled next button ends the tour but doesn't set the flag. Set it
            window.localStorage.setItem('tour_end', true);
        },
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
        resolve();
      });
    };
  }

  function hideMenuStep() {
  }

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
