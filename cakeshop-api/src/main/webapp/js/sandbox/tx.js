import React from "react";
import ReactDOM from "react-dom";
import NodeChooser from "../components/NodeChooser";
import {Constructor} from "../components/Constructor";
import {TransactTable} from "../components/Transact";
import utils from "../utils"

(function() {
  var Sandbox = window.Sandbox = window.Sandbox || {};
  var activeContract, compiler_output;

  function showTxView() {
      ReactDOM.render(<NodeChooser/>,
          document.getElementById('rpc-select-container')
      );
      loadAccounts();
  }

  var events_enabled = true;

  Sandbox.on("compile", function() {
    if (!events_enabled) {
      return;
    }

    $(".select_contract .compiled_contracts select").empty();
    $(".select_contract .constructor").empty();
    $(".compiled_contracts .refresh").show();
  });

  Sandbox.on("compiled", function(contracts) {
    if (!events_enabled) {
      return;
    }

    showCompiledContracts(contracts);
  });

  function loadAccounts() {
    // load and display panel
    Account.list().then(function(accounts) {
      Sandbox.accounts = accounts;
      var s = '<div class="panel-body"><table class="table">';

      accounts.forEach(function(a) {
        s += '<tr>';
        s += '<td class="address" data-address="' + a.get("address") + '">' + a.get("address") + '</td>';
        s += '<td class="text-right">' + a.humanBalance() + '</td>';
        s += '</tr>';
      });

      s += '</table></div>';

      $(".panel.accounts .panel-body").remove();
      $(".panel.accounts").append(s);

      $(".panel.accounts td.address").click(function(e) {
        var isEditable = !!$(this).prop('contentEditable');
        $(this).prop('contentEditable', isEditable);
        $(this).focus();
        $(this).selectText();
      });

      Sandbox.trigger("col3-reflow");
    });
  }

  function loadContracts() {
    // show deployed contracts (via registry)
    var sel = $(".select_contract .contracts select")
      .empty()
      .append("<option value=''></option>");

    // $("div.contracts .refresh").show();
    Contract.list(function(contracts) {
      contracts.forEach(function(c) {
        var ts = moment.unix(c.get("createdDate")).format("YYYY-MM-DD hh:mm A");
        var name = c.get("name") + " (" + trunc(c.id) + ", " + ts + ")";
        sel.append("<option value='" + c.id + "'>" + name + "</option>");
      });
      // $("div.contracts .refresh").hide();
      // sel.val(sel.find("option:last").val()).change(); // useful for debugging contract stuff
    });
  }

  function showCompiledContracts(_output) {
    // Show compiled contracts in dropdown

    // if (_output === -1) {
    //   return; //
    // }

    compiler_output = _output;
    $(".compiled_contracts .refresh").hide();

    var sel = $(".select_contract .compiled_contracts select")
      .empty()
      .append("<option value=''></option>");

    if (compiler_output && _.isArray(compiler_output)) {
      compiler_output.forEach(function(c) {
        sel.append("<option value='" + c.get("name") + "'>" + c.get("name") + "</option>");
      });
    }
  }

    function onTransactionSubmitted(txId, method, methodSig, methodArgs) {
        if (method.constant === true) {
            Sandbox.addTx(
                "[read] " + methodSig + " => " + JSON.stringify(txId));
        } else {
            Sandbox.addTx("[txn] " + methodSig + " => created tx "
                + Sandbox.wrapTx(txId));
            Transaction.waitForTx(txId).then(function (tx) {
                Sandbox.addTx(
                    "[txn] " + Sandbox.wrapTx(txId) + " was committed in block "
                    + Sandbox.wrapBlock(tx.get("blockNumber")));
                var logs = tx.get("logs");
                if (logs && logs.length > 0) {
                    logs.forEach(function (log) {
                        var name = (log.name ? log.name : "<anon>"),
                            data = JSON.stringify(log.data);
                        data = data.substring(1, data.length - 1);
                        addTx("[event] " + name + "(" + data + ")");
                    });
                }
                Sandbox.showCurrentState(activeContract._current_state);
            });
        }
    }

  function showTransactForm() {

    var abi = activeContract.abi;
    if (!abi) {
      return;
    }

    var s = '<div class="panel-body"><table id="react-transact" class="table"></table></div>';

    $(".transact .panel-body").remove();
    const accounts = Sandbox.accounts.map(
        (account) => ({address: account.get("address")}));
    $(".transact").append(s);
      ReactDOM.render(<TransactTable accounts={accounts}
                                     activeContract={activeContract}
                                     onTransactionSubmitted={onTransactionSubmitted}/>,
          document.getElementById('react-transact')
      );

    // highlight associated sourcecode on method input click
    $(".transact .method").click(function(e) {
      var el = e.target.tagName === "TR" ? $(e.target) : $(e.target).parents("tr"),
       methodName = el.attr("data-method"),
       method = activeContract.getMethod(methodName);

      highlightMethod(method);
    });
  }

   /**
   * Highlight the given method in the source code editor
   *
   * @param [Object] method (ABI object)
   */
  function highlightMethod(method) {
    if (!method) {
      return;
    }

    var lines = Sandbox.getEditorSource().split("\n");

    for (var i = 0; i < lines.length; i++) {
      var highlight = false;
      if (lines[i].match(new RegExp("function\\s+" + method.name + "\\s*\\("))) {
        highlight = true;
      } else if (method.constant === true &&
          lines[i].match(new RegExp("^\\s*[a-z\\d\\[\\]]+\\s+public\\b.*?" + method.name + "\\s*;"))) {

        highlight = true;
      }
      if (highlight) {
        Sandbox.editor.selection.moveCursorToPosition({row: i, column: 0});
        Sandbox.editor.selection.selectLine();
        Sandbox.editor.scrollToLine(i, true, false);
      }
    }
  }

  function showCurrentState(previousState) {
    if (!activeContract) {
      return;
    }

    activeContract.readState().then(function(results) {
      displayStateTable(results);

      if (!previousState) {
        return;
      }

      // diff states and show changes in paper tape
      var resultHash = {},
        previousHash = {};

      results.forEach(function(r) { resultHash[r.method.name] = r.result; });
      previousState.forEach(function(r) { previousHash[r.method.name] = r.result; });

      _.each(resultHash, function(val, key) {

        var pVal = previousHash[key];
        if (val !== pVal) {
          if (_.isUndefined(pVal) || pVal === null) {
            addTx("[state] " + key + " = " + JSON.stringify(val));
          } else {
            addTx("[state] " + key + ": " + JSON.stringify(pVal) + " => " + JSON.stringify(val));
          }
        }

      });
    });
  }

  function displayStateTable(results) {
    var s = '<div class="panel-body"><table class="table">';

    results.sort(function(a, b) {
      return a.method.name.localeCompare(b.method.name);
    });

    results.forEach(function(r) {
      s += '<tr>';
      s += '<td>' + r.method.name + '</td>';
      s += '<td class="text-right">';
      if (r.result && _.isArray(r.result)) {
        s += '<ol start="0">';
        r.result.forEach(function(v) {
          s += "<li>" + JSON.stringify(v) + "</li>";
        });
      } else if (r.result && _.isObject(r.result)) {
        s += '<table class="table table-bordered table-condensed">';
        _.keys(r.result).forEach(function(key) {
          s += '<tr>';
          s += '<td>' + key + '</td>';
          s += '<td class="text-right">' + r.result[key] + '</td>';
          s += '</tr>';
        });
        s += '</table>';
      } else if (r.result && _.isString(r.result)) {
        if (r.result.length > 20) {
          s += '<div class="form-group"><textarea class="form-control" rows="3">' + r.result + '</textarea></div>';
        } else {
          s += r.result;
        }
      } else {
        s += r.result;
      }
      s += '</td></tr>';
    });
    s += '</table></div>';
    try {
      $(".panel.state .panel-body").remove();
      $(".panel.state").append(s);
      Sandbox.trigger("col3-reflow");
    } catch (e) {
      console.log(e);
    }
  }

  function trunc(addr) {
    var len = addr.startsWith("0x") ? 10 : 8;
    return addr.substring(0, len);
  }

  function wrapBlock(blockId) {
    return '<a class="block" target="_explorer" href="index.html#section=explorer&widgetId=block-detail&data=' + encodeURIComponent(blockId) + '">#' + blockId + '</a>';
  }

  function wrapTx(txId) {
    return '<a class="tx" target="_explorer" href="index.html#section=explorer&widgetId=txn-detail&data=' + encodeURIComponent(txId) + '" title="' + txId + '">' + trunc(txId) + '</a>';
  }

  function wrapAddr(addr) {
    return '<span class="addr" title="' + addr + '">' + trunc(addr) + '</span>';
  }

  function addTx(message, date) {
    date = date ? moment(date) : moment();
    var timestamp = '<span class="time pull-right">' + date.format("hh:mm:ss A") + '</span>';
    var div = '<div class="tx">' + timestamp + message + '</div>';

    $(".papertape .panel-body").append(div);

    // make sure msg is visible
    var lastTx = $(".papertape .panel-body div.tx:last");
    if (lastTx && lastTx[0]) {
      lastTx[0].scrollIntoView();
    }
  }

  function setActiveContract(c) {
    activeContract = c;
    showSourceCode(c);
    showTransactForm();
    showCurrentState();
    addTx("using '" + c.get("name") + "' at " + wrapAddr(c.id));
  }

  function showSourceCode(c) {
    var tabName = c.get("name") + " " + trunc(c.id);

    if (Sandbox.Filer.get(tabName)) {
      return Sandbox.activateTab(tabName);
    }

    Sandbox.addFileTab(tabName, c.get("code"), true);
  }

  // Enter contract address
  $(".select_contract .address input").change(function(e) {
    var addr = $(e.target).val();
    Contract.get(addr).then(setActiveContract);
  });

  // Select already deployed contract
  $(".select_contract .contracts select").change(function(e) {
    var addr = $(e.target).val();
    if (!addr || addr.length === 0) {
      return;
    }
    $(".select_contract .address input").val(addr).change();
  });

  // Select contract to deploy
  $(".select_contract .compiled_contracts select").change(function(e) {
    var sel = $(e.target).val();
      const container = document.getElementById('constructor-react');
      ReactDOM.unmountComponentAtNode(container);

    var contract = _.find(compiler_output, function(c) { return c.get("name") === sel; });

    if (!sel || !contract) {
      return;
    }

    var conMethod = _.find(contract.abi, function(m) { return m.type === "constructor"; });

      ReactDOM.render(<Constructor onDeploy={deploy} method={conMethod}/>,
          container
      );
  });

    function deploy(params, privateFor) {
        console.log("deploying, params:", params, "privateFor:", privateFor)
        // find contract to deploy
        var sel = $(".select_contract .compiled_contracts select").val();
        if (!sel) {
            return false;
        }

        var editorSource = Contract.preprocess(Sandbox.getEditorSource());
        var optimize = document.querySelector('#optimize').checked;
        var evmVersion = document.querySelector('#evmVersionSelector').value;
        var filename = Sandbox.Filer.getActiveFilename();

        Contract.compile(editorSource, optimize, filename, evmVersion).then(
            function (compiler_output) {
                var contract = _.find(compiler_output, function (c) {
                    return c.get("name") === sel;
                });

                var _params = _.map(params, function (v, k) {
                    return v;
                });

                var _args = "";

                if (_params.length > 0) {
                    _args = " (" + _params.join(", ") + ")";
                }

                addTx(
                    "[deploy] Contract '" + contract.get("name") + "'" + _args);

                Contract.deploy(contract.get("code"), optimize, _params,
                    contract.get("binary"),
                    "",
                    privateFor,
                    filename,
                    evmVersion
                ).then(function (addr) {

                    addTx("Contract '" + contract.get("name") + "' deployed at "
                        + wrapAddr(addr));
                    $(".select_contract .address input").val(addr);

                    addTx("Waiting for contract to be registered");
                    var registered = false;

                    function waitForRegistration() {
                        // TODO use contract event topic for registry ??
                        Contract.get(addr).then(function (c) {
                            if (c === null || c.get("name") === null) {
                                setTimeout(waitForRegistration, 1000); // poll every 1s til done
                                return;
                            }

                            registered = true;
                            setActiveContract(c);
                            loadContracts(); // refresh contract list
                        });
                    }

                    setTimeout(waitForRegistration, 200);
                }, function (errors) {
                    addTx("Deploy failed: " + errors[0].detail);
                });

            });

        return false;
    }

  /**
   * Toggle the expand/collapse icon.
   *
   * @return [Boolean] true if panel is expanded
   */
  function toggleCollapseIcon(i) {
    if (i.hasClass("fa-minus-square-o")) {
      // collapse
      i.removeClass("fa-minus-square-o").addClass("fa-plus-square-o");
      return false;
    } else {
      // expand
      i.removeClass("fa-plus-square-o").addClass("fa-minus-square-o");
      return true;
    }
  }

  function shrinkify(panel_class) {
    $(panel_class + " .shrink").click(function(e) {
      toggleCollapseIcon($(e.target));
      $(panel_class + " .panel-body").toggle();
    });
  }

  $(".trash").click(function(e) {
    $(".papertape .panel-body").empty();
  });

  shrinkify(".select_contract");
  shrinkify(".state");
  shrinkify(".papertape");
  shrinkify(".transact");
  shrinkify(".accounts");

  Sandbox.showTxView = showTxView;
    Sandbox.addTx = addTx;
    Sandbox.wrapTx = wrapTx;
    Sandbox.wrapBlock = wrapBlock;
    Sandbox.showCurrentState = showCurrentState;
  Sandbox.accounts = [];

  $(function() {
    showTxView(); // default view
  });

})();
