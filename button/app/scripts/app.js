'use strict';

angular.module('cameraboxButton', ['ngWebSocket'])
    .factory('button', ['$websocket', '$location', '$timeout', function($websocket, $location, $timeout) {
        var dataStream;
        var alive = 'alive';

        var connect = function() {
            dataStream = $websocket('ws://' + $location.host() + ':8080/ws');
        };

        var keepAlive = function () {
            if (!alive) {
                connect();
            } else {
                alive = undefined;
            }
            dataStream.send('ping')
        };

        var sendKeepAlive = function() {
            // send keep alive, every 3s
            $timeout(function () {
                keepAlive();
                sendKeepAlive();
            }, 5000);
        };

        connect();
        sendKeepAlive();

        return {
            click: function() {
                dataStream.send('click');
            },
            doubleClick: function() {
                dataStream.send('double-click');
            }
        };
    }])
    .directive('cameraboxButton', ['button', function(button) {
        return {
            restrict: 'E',
            scope: true,
            template: '<button class="main-button">Click me</button>',
            compile: function () {
                return {
                    pre: function (scope, element) {
                        element.on('click', function(event) {
                            button.click();
                        });
                    }
                }
            }
        }
    }]);
