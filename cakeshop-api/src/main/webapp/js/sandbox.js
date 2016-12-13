
(function() {
    var Sandbox = window.Sandbox = window.Sandbox || {};

    // ----------------- editor ----------------------

    var editor = Sandbox.editor = Sandbox.initEditor();
    var errMarkerId = null;

    Sandbox.initFileTabs();

    var _src = editor.getValue();
    if (!_.isString(_src) || _src.length === 0) {
        Sandbox.showTxView(); // load default view now since no compilation happening
    }


    // ----------------- compiler ----------------------
    var compileJSON;
    var compilerAcceptsMultipleFiles = false; // by default?

    var previousInput = '';
    var sourceAnnotations = [];

    var getEditorSource = function() {
      return editor.getValue();
    };

    var compile = function() {
      editor.getSession().clearAnnotations();
      sourceAnnotations = [];
      editor.getSession().removeMarker(errMarkerId);
      $('#output .compiler_output').empty(); // clear output window

      var editorSource = getEditorSource();
        Sandbox.Filer.saveActiveFile(editorSource);

      var files = {};
      files[Sandbox.Filer.getActiveFile()] = editorSource;
      var input = gatherImports(files, compile);
      if (!input) {
            return;
        }
      var optimize = document.querySelector('#optimize').checked;

        // input = preprocess(input);
        // console.log(input);

        Sandbox.trigger("compile", input);
        Contract.compile(input, optimize).then(
            function(data) {
                Sandbox.trigger("compiled", data);
                renderContracts(data, editorSource);
            },
            function(errs) {
                if (errs) {
                    errs.forEach(function(err) {
                        renderError(err.detail);
                    });
                } else if (!Client.connected) {
                    renderError("server disconnected");
                }
                $(".sidenav li.compilerView a").click(); // make sure output tab is visible
            }
        );
    };

    var compileTimeout = null;
    var onChange = _.debounce(function() {
      var input = editor.getValue();
      if (input === "") {
            Sandbox.Filer.saveActiveFile("");
        return;
      }
      if (input === previousInput)
        return;
      previousInput = input;
        compile();
    }, 300);

    var cachedRemoteFiles = {};
    function gatherImports(files, asyncCallback, needAsync) {

      if (!compilerAcceptsMultipleFiles)
        return files[Sandbox.Filer.getActiveFile()];

      var importRegex = /import\s[\'\"]([^\'\"]+)[\'\"];/g;
      var reloop = false;
      do {
        reloop = false;
        for (var fileName in files) {
          var match;
          while (match = importRegex.exec(files[fileName])) {
            var m = match[1];
            if (m in files) continue;
            if (getFiles().indexOf(fileKey(m)) !== -1) {
              files[m] = window.localStorage[fileKey(match[1])];
              reloop = true;
            } else if (m in cachedRemoteFiles) {
              files[m] = cachedRemoteFiles[m];
              reloop = true;
            } else if (githubMatch = /^(https?:\/\/)?(www.)?github.com\/([^\/]*\/[^\/]*)\/(.*)/.exec(m)) {
              $.getJSON('https://api.github.com/repos/' + githubMatch[3] + '/contents/' + githubMatch[4], function(result) {
                var content;
                if ('content' in result)
                  content = Base64.decode(result.content);
                else
                  content = "\"" + m + "\" NOT FOUND"; //@TODO handle this better
                cachedRemoteFiles[m] = content;
                files[m] = content;
                gatherImports(files, asyncCallback, true);
              }).fail(function(){
                var content = "\"" + m + "\" NOT FOUND"; //@TODO handle this better
                cachedRemoteFiles[m] = content;
                files[m] = content;
                gatherImports(files, asyncCallback, true);
              });
              return null;
            }
          }
        }
      } while (reloop);
      var input = JSON.stringify({'sources':files});
      if (needAsync)
        asyncCallback(input);
      return input;
    }

    editor.getSession().on('change', onChange);
    document.querySelector('#optimize').addEventListener('change', compile);

    // ----------------- compiler output renderer ----------------------
    var detailsOpen = {};

    function errortype(message) {
      return message.match(/^.*:[0-9]*:[0-9]* Warning: /) ? 'warning' : 'error';
    }

    var renderError = function(message) {
      var type = errortype(message);
      var $pre = $("<pre />").text(message);
      var $error = $('<div class="sol ' + type + '"><div class="close"><i class="fa fa-close"></i></div></div>').prepend($pre);
      $('#output .compiler_output').append( $error );
      var err = message.match(/^([^:]*):([0-9]*):(([0-9]*):)? /);
      if (err) {
        var errFile = err[1];
        var errLine = parseInt(err[2], 10) - 1;
        var errCol = err[4] ? parseInt(err[4], 10) : 0;
        if (errFile === '' || errFile === fileNameFromKey(Sandbox.SOL_CACHE_FILE)) {
          sourceAnnotations[sourceAnnotations.length] = {
            row: errLine,
            column: errCol,
            text: message,
            type: type
          };
          editor.getSession().setAnnotations(sourceAnnotations);
        }
        $error.click(function(ev){
          if (errFile !== '' && errFile !== fileNameFromKey(Sandbox.SOL_CACHE_FILE) && getFiles().indexOf(fileKey(errFile)) !== -1) {
            // Switch to file
            Sandbox.SOL_CACHE_FILE = fileKey(errFile);
            updateFiles();
            //@TODO could show some error icon in files with errors
          }
          editor.focus();
          editor.gotoLine(errLine + 1, errCol - 1, true);
        });
        $error.find('.close').click(function(ev){
          ev.preventDefault();
          $error.remove();
          return false;
        });
      }
    };

    var gethDeploy = function(contractName, interface, bytecode){
        var abi = _.isString(interface) ? JSON.parse(interface) : interface;
      var funABI = getConstructorInterface(interface);

      var code = "";
      $.each(funABI.inputs, function(i, inp) {
        code += "var " + inp.name + " = /* var of type " + inp.type + " here */ ;\n";
      });

      code += "var " + contractName + "Contract = web3.eth.contract(" + interface.replace("\n","") + ");" +
            "\nvar " + contractName + " = " + contractName + "Contract.new(";

      $.each(funABI.inputs, function(i, inp) {
        code += "\n   " + inp.name + ",";
      });

      code += "\n   {"+
      "\n     from: web3.eth.accounts[0], "+
      "\n     data: '"+bytecode+"', "+
      "\n     gas: 3000000"+
      "\n   }, function(e, contract){"+
      "\n    console.log(e, contract);"+
      "\n    if (typeof contract.address != 'undefined') {"+
      "\n         console.log('Contract mined! address: ' + contract.address + ' transactionHash: ' + contract.transactionHash);" +
      "\n    }" +
      "\n })";


      return code;
    };

    var combined = function(contractName, interface, bytecode){
      return JSON.stringify([{name: contractName, interface: interface, bytecode: bytecode}]);
    };

    var renderContracts = function(contracts, source) {
        var $contractOutput = $('<div class="udapp" />');
        contracts.forEach(function(contract) {
            var $contractEl = $('<div class="contract"/>');
            var $title = $('<span class="title"/>').text( contract.get("name") );
            if (contract.get("binary")) {
                $title.append($('<div class="size"/>').text((contract.get("binary").length / 2) + ' bytes'));
            }
            $contractEl.append($title); // .append( this.getCreateInterface( $contractEl, this.contracts[c]) );

        var $detail = $('<div class="info"/>')
        .append(textRow('Bytecode', contract.get("binary")))
        .append(textRow('ABI', contract.get("abi")))
        .append(textRow('Web3 deploy', gethDeploy(contract.get("name").toLowerCase(), contract.get("abi"), contract.get("binary")), 'deploy'))
        // .append(textRow('uDApp', combined(contractName, contract.get("abi"), contract.get("binary")), 'deploy'))
        .append(getDetails(contract, source, contract.get("name")));

            $contractEl.append($detail);
            $contractOutput.append($contractEl);
            $contractOutput.append("<br/>");
        });

      $contractOutput.find('.title').click(function(ev){ $(this).closest('.contract').toggleClass("expand").find('.info').toggle(); });
      $('#output .compiler_output').append( $contractOutput );
      $('.col2 input,textarea').click(function() { this.select(); });

    }; // renderContracts

    var tableRowItems = function(first, second, cls) {
      return $('<div class="keyval-row"/>')
        .addClass(cls)
        .append($('<div class="col1">').append(first))
        .append($('<div class="col2">').append(second));
    };
    var tableRow = function(description, data) {
      return tableRowItems(
        $('<strong/>').text(description),
        $('<input readonly="readonly" class="form-control"/>').val(data));
    };
    var textRow = function(description, data, cls) {
      return tableRowItems(
        $('<strong/>').text(description),
        $('<textarea readonly="readonly" class="form-control gethDeployText"/>').val(data),
        cls);
    };
    var preRow = function(description, text) {
      return tableRowItems(
        $('<strong/>').text(description),
        $('<pre/>').text(text));
    };
    var getDetails = function(contract, source, contractName) {
        // solidity interface
      var details = $('<div class="contractDetails"/>')
        .append(textRow('Solidity Interface', contract.get("solidityInterface")));

        // function hashes
      var funHashes = '';
      for (var fun in contract.get("functionHashes"))
        funHashes += contract.get("functionHashes")[fun] + ' ' + fun + '\n';
      details.append(preRow("Functions", funHashes));

        // gas estimates
      details.append(preRow("Gas Estimates", formatGasEstimates(contract.get("gasEstimates"))));

      return details;
    };
    var formatGasEstimates = function(data) {
        if (_.isNull(data) || _.isUndefined(data)) {
            return "";
        }
      var gasToText = function(g) { return g === null ? 'unknown' : g; };
      var text = '';
      if ('creation' in data)
        text += 'Creation: ' + gasToText(data.creation[0]) + ' + ' + gasToText(data.creation[1]) + '\n';
      text += 'External:\n';
      for (var fun in data.external) {
        text += '  ' + fun + ': ' + gasToText(data.external[fun]) + '\n';
        }
      text += 'Internal:\n';
      for (fun in data.internal) {
        text += '  ' + fun + ': ' + gasToText(data.internal[fun]) + '\n';
        }
      return text;
    };
    var formatAssemblyText = function(asm, prefix, source) {
      if (typeof(asm) == typeof('') || asm === null || asm === undefined)
        return prefix + asm + '\n';
      var text = prefix + '.code\n';
      $.each(asm['.code'], function(i, item) {
        var v = item.value === undefined ? '' : item.value;
        var src = '';
        if (item.begin !== undefined && item.end !== undefined)
          src = source.slice(item.begin, item.end).replace('\n', '\\n', 'g');
        if (src.length > 30)
          src = src.slice(0, 30) + '...';
        if (item.name != 'tag')
          text += '  ';
        text += prefix + item.name + ' ' + v + '\t\t\t' + src +  '\n';
      });
      text += prefix + '.data\n';
      if (asm['.data'])
        $.each(asm['.data'], function(i, item) {
          text += '  ' + prefix + '' + i + ':\n';
          text += formatAssemblyText(item, prefix + '    ', source);
        });

      return text;
    };

    $('.asmOutput button').click(function() {$(this).parent().find('pre').toggle(); });

    var getConstructorInterface = function(abi) {
      var funABI = {'name':'','inputs':[],'type':'constructor','outputs':[]};
      for (var i = 0; i < abi.length; i++)
        if (abi[i].type == 'constructor') {
          funABI.inputs = abi[i].inputs || [];
          break;
        }
      return funABI;
    };

    // for surfacing promise errors
    window.addEventListener("unhandledrejection", function(e) {
        e.preventDefault();
        console.log(e.detail.reason.message);
        console.log(e.detail.reason.stack);
        alert("ERROR: " + e.detail.reason);
    });

    // compile on page load (whatever is in buffer)
    $(function() {
        onChange();
    });

    Sandbox.compile = compile;
    Sandbox.getEditorSource = getEditorSource;

})();
