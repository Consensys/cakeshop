
(function() {

  var Sandbox = window.Sandbox = window.Sandbox || {};

  Sandbox.tour = new Tour({
    debug: false,
    backdrop: true,
    onEnd: function() {
      $('.sidenav li').css({'z-index': '100'});
    },
    steps: [
      {
        element: '#editor',
        title: 'Welcome to the Sandbox',
        content: 'The Sandbox is a place to write, deploy, and test smart contract code. The editor will compile solidity in realtime.',
      },
      {
        element: '#block_num',
        content: 'The current block number',
        placement: 'left',
      },
      {
        element: '#mining_status',
        content: 'Current mining status. Click to toggle mining on and off.',
        placement: 'left',
        onNext: function() {
          $('#tx-icon a').click();
        },
      },
      {
        element: '#txView',
        content: 'This is the transactions panel where you\'ll spend most of your time',
        placement: 'left',
        onShow: function() {
          $('#tx-icon').css({'z-index': '10000'});
        }
      },
      {
        element: '#select-contract',
        title: 'Step 1: Choose Contract',
        content: 'Choose from previously deployed contracts, deploy a new contract, or directly enter the address of a contract.',
        placement: 'left',
        onShow: function() {
          $('#tx-icon').css({'z-index': '10000'});
          $('#txView').css({'z-index': '10000'});
        }
      },
      {
        element: '#state',
        content: 'This displays the state of the contract.',
        placement: 'left',
        onShow: function() {
          $('#tx-icon').css({'z-index': '10000'});
          $('#txView').css({'z-index': '10000'});
        }
      },
      {
        element: '#transact',
        content: 'These are transactions you can run based on the deployed contract.',
        placement: 'left',
        onShow: function() {
          $('#tx-icon').css({'z-index': '10000'});
          $('#txView').css({'z-index': '10000'});
        }
      },
      {
        element: '#accounts',
        content: 'This displays the accounts',
        placement: 'left',
        onShow: function() {
          $('#tx-icon').css({'z-index': '10000'});
          $('#txView').css({'z-index': '10000'});
        }
      },
      {
        element: '#papertape',
        content: 'The paper tape shows the history of all the transactions run.',
        placement: 'left',
        onNext: function() {
          $('.libView a').click();
          $('#tx-icon').css({'z-index': '100'});
        },
        onShow: function() {
          $('#tx-icon').css({'z-index': '10000'});
          $('#txView').css({'z-index': '10000'});
        }
      },
      {
        element: '#libView',
        content: 'This is the contracts library. You will find example contracts here.',
        placement: 'left',
        onNext: function() {
          $('.compilerView a').click();
          $('.sidenav .libView').css({'z-index': '100'});
        },
        onPrev: function() {
          $('#tx-icon a').click();
          $('.sidenav .libView').css({'z-index': '100'});
        },
        onShow: function() {
          $('.sidenav .libView').css({'z-index': '10000'});
        }
      },
      {
        element: '#compilerView',
        content: 'This is where the output of the compiler is displayed, which is useful for debugging purposes.',
        placement: 'left',
        onNext: function() {
          $('.settingsView a').click();
          $('.sidenav .compilerView').css({'z-index': '100'});
        },
        onPrev: function() {
          $('.libView a').click();
          $('.sidenav .compilerView').css({'z-index': '100'});
        },
        onShow: function() {
          $('.sidenav .compilerView').css({'z-index': '10000'});
        }
      },
      {
        element: '#settingsView',
        content: 'You may change settings here.',
        placement: 'left',
        onPrev: function() {
          $('.compilerView a').click();
          $('.sidenav .settingsView').css({'z-index': '100'});
        },
        onShow: function() {
          $('.sidenav .settingsView').css({'z-index': '10000'});
        }
      }
    ]
  }).init();

  if (!window.localStorage.getItem('tour_end')) {
    Sandbox.tour.start(true);
  }

  $('#help').click(function() {
    window.localStorage.removeItem('tour_current_step');
    window.localStorage.removeItem('tour_end');
    Sandbox.tour.start(true);
  });

})();
