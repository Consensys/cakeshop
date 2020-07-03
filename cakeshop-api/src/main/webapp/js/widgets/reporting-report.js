import React from 'react';
import ReactDOM from 'react-dom';
import { Report } from "../components/Report";
import utils from "../utils";

function isStateEqual(state1, state2) {
    var markedState2 = state2;
    var noChange = true;
    for (let i = 0; i < state1.historicStorage.length; i++) {
        if (state1.historicStorage[i].value !== state2.historicStorage[i].value) {
            markedState2.historicStorage[i].changed = true;
            noChange = false
        }
    }
    if (noChange) {
        return false
    }
    return markedState2
}

module.exports = function() {
    var extended = {
        name: 'reporting-report',
        title: 'Report',
        size: 'large',

        setData: function(data) {
            this.contractAddress = data.id;
        },

        fetch: function() {
            var _this = this;
            $('#widget-' + _this.shell.id).html('<div id="report-container" style="width: 100%;"/>');
            ReactDOM.render(<Report isLoading={true} />, document.getElementById('report-container'));
            // fetch last persisted block number
            $.when(
                utils.load({ url: window.reportingEndpoint, data: {"jsonrpc":"2.0","method":"reporting.GetLastPersistedBlockNumber","params":[],"id":99} })
            ).fail(function(res) {
                $('#widget-' + _this.shell.id).html( '<h3 style="text-align: center;margin-top: 70px;">Report Generation Failed</h3>' );
            }).done(function(res) {
                if (res.error) {
                    console.log(res.error);
                    $('#widget-' + _this.shell.id).html( '<h3 style="text-align: center;margin-top: 70px;">Report Generation Failed</h3>' );
                    return
                }
                // generate report for whole history
                $.when(
                    utils.load({ url: window.reportingEndpoint, data: {"jsonrpc":"2.0","method":"reporting.GetStorageHistory","params":[{address: _this.contractAddress, startBlockNumber: 1, endBlockNumber: res.result}],"id":99} })
                ).fail(function(res) {
                    $('#widget-' + _this.shell.id).html( '<h3 style="text-align: center;margin-top: 70px;">Report Generation Failed</h3>' );
                }).done(function(res) {
                    if (res.error) {
                        console.log(res.error);
                        $('#widget-' + _this.shell.id).html( '<h3 style="text-align: center;margin-top: 70px;">Report Generation Failed</h3>' );
                        return
                    }
                    // parse res.result.historicState
                    // console.log(JSON.stringify(res.result.historicState));
                    var historicState = res.result.historicState;
                    var parsedStorage = [];
                    if (historicState.length !== 0) {
                        parsedStorage.push(historicState[0]);
                        var currentState = historicState[0];
                        for (var i = 1; i < historicState.length; i++) {
                            var nextState = isStateEqual(currentState, historicState[i]);
                            if (nextState) {
                                parsedStorage.unshift(nextState);
                                currentState = nextState
                            }
                        }
                    }
                    // console.log(parsedStorage);
                    ReactDOM.render(<Report parsedStorage={parsedStorage} isLoading={false} />, document.getElementById('report-container'));
                })
            })
        }
    };

    var widget = _.extend({}, widgetRoot, extended);

    // register presence with screen manager
    Dashboard.addWidget(widget);
};
