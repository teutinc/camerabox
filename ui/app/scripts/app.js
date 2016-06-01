'use strict';

angular.module('photoboothApp', [
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
    .factory('photoBooth', ['$websocket', '$location', '$timeout', function($websocket, $location, $timeout) {
        var photoBooth = {};

        // init with a small delay, in order to let some time to display the bootstrap screen
        $timeout(function () {
            var dataStream = $websocket('ws://' + $location.host() + ':8080/ws');
            dataStream.onMessage(function(message) {
                console.log('message received:');
                console.dir(message);
                photoBooth.state = JSON.parse(message.data);
                if (photoBooth.state.activity) {
                    $location.url('/' + photoBooth.state.activity);
                } else {
                    console.warn('received message, does not contains any activity!');
                    console.dir(localState);
                }
            });
        }, 2000);

        return photoBooth;
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

