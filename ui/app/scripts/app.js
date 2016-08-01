'use strict';

angular.module('cameraboxApp', [
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute',
    'ngWebSocket'
])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'views/bootstrap.html',
                controller: 'BootstrapCtrl'
            })
            .when('/noop', {
                templateUrl: 'views/noActivity.html',
                controller: 'NoActivityCtrl'
            })
            .when('/questionBox', {
                templateUrl: 'views/questionBox.html',
                controller: 'QuestionBoxCtrl'
            })
            .otherwise({
                redirectTo: '/'
            });
    })
    .factory('cameraBox', ['$websocket', '$location', '$timeout', function($websocket, $location, $timeout) {
        var cameraBox = {};

        var dataStream;
        var alive = 'alive';

        var connect = function() {
            dataStream = $websocket('ws://' + $location.host() + ':8080/ws');
            dataStream.onMessage(function(message) {
                if (message.data === 'pong') {
                    alive = 'alive';
                }
                cameraBox.state = JSON.parse(message.data);
                if (cameraBox.state.activity) {
                    $location.url('/' + cameraBox.state.activity);
                } else {
                    console.warn('received message, does not contains any activity!');
                    console.dir(localState);
                }
            });
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
            }, 3000);
        };

        connect();
        sendKeepAlive();

        return cameraBox;
    }])
    .factory('timer', ['$timeout', function($timeout) {

        var Timer = function(seconds) {
            this.time = seconds;

            var self = this;
            var decr = function() {
                self.time--;
                if (self.time > 0) {
                    $timeout(decr, 1000);
                }
            };
            $timeout(decr, 1000);
        };

        return {
            createTimer: function(seconds) {
                return new Timer(seconds);
            },
            emptyTimer: function() {
                return {
                    time: 0
                };
            }
        };
    }]);

