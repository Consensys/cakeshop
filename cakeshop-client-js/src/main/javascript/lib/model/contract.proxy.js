
(function() {

    Contract.Proxy = (function() {
        function Proxy(contract) {
            this._contract = contract;
            if (!contract.abi) {
                return;
            }
            var proxy = this;
            contract.abi.forEach(function(method) {
                if (method.type !== "function") {
                    return;
                }

                /**
                 * Process args based on ABI definitions
                 */
                function processInputArgs(args) {
                    var inputs = method.inputs;
                    var ret = [];

                    for (var i = 0; i < inputs.length; i++) {
                        var input = inputs[i],
                            arg   = args[i];
                        if (input.type.match(/^bytes\d+$/)) {
                            // base64 encode bytes
                            ret.push(Sandbox.encodeBytes(arg));
                        } else {
                            // all other input types, just accumulate
                            ret.push(arg);
                        }
                    }

                    return ret;
                }

                /**
                 * Process results based on ABI definitions
                 */
                function processOutputArgs(results) {
                    var outputs = method.outputs;

                    // console.log("outputs", outputs);
                    // console.log("results", results);

                    var ret = [];
                    for (var i = 0; i < outputs.length; i++) {
                        var output = outputs[i],
                            result = results[i];
                        if (output.type.match(/^bytes\d+$/)) {
                            // base64 decode bytes
                            ret.push(Sandbox.decodeBytes(result));
                        } else if (output.type.match(/^bytes\d+\[\d*\]$/) && _.isArray(result)) {
                            // console.log("decoding result bytes32[]", result);
                            // base64 decode arrays of bytes
                            result = _.map(result, function(v) { return Sandbox.decodeBytes(v); });
                            // console.log("decoded ", result);
                            ret.push(result);
                        } else {
                            // all other input types, just accumulate
                            ret.push(result);
                        }
                    }

                    if (outputs.length === 1) {
                        return ret[0]; // hmmm?
                    }
                    return ret;
                }

                // attach method to proxy
                proxy[method.name] = function(options) {
                    if (_.isNull(options) || _.isUndefined(options)) {
                        options = {from: null, args: []};
                    }

                    // process arguments based on ABI
                    options.args = processInputArgs(options.args);
                    options.method = method.name;

                    return new Promise(function(resolve, reject) {
                        if (method.constant === true) {
                            contract.read(options).then(function(res) {
                                resolve(processOutputArgs(res));
                            }, function(err) {
                                reject(err);
                            });
                        } else {
                            contract.transact(options).then(function(txId) {
                                resolve(txId);
                            }, function(err) {
                                reject(err);
                            });
                        }
                    });
                };
            });
        }

        return Proxy;
    })();

})();
