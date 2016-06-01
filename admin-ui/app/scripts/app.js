'use strict';

angular.module('photoboothAdminApp', [
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute',
    'ui.sortable',
    'ui.bootstrap',
    'angularFileUpload'
])

    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'views/activities.html',
                controller: 'ActivitiesCtrl'
            })
            .when('/create', {
                templateUrl: 'views/edit.html',
                controller: 'EditCtrl'
            })
            .when('/edit/:id', {
                templateUrl: 'views/edit.html',
                controller: 'EditCtrl'
            })
            .otherwise({
                redirectTo: '/'
            });
    })
    .constant('DefaultPort', '8080')
    .factory('activities', ['$http', '$location', 'DefaultPort', function ($http, $location, DefaultPort) {
        var url = 'http://' + $location.host() + ':' + DefaultPort + '/api/activities';
        return {
            list: function () {
                return $http.get(url).then(function (response) {
                        return response.data.map(function (e) {
                            // flatten the running activity object, to look like an activity having a running field
                            var obj = e.activity;
                            obj.running = e.running;
                            return obj;
                        });
                    });
            },
            start: function (activity) {
                return $http.post(url + '/start/' + activity.id).then(function (response) {
                    return response;
                });
            },
            stop: function (activity) {
                return $http.post(url + '/stop/' + activity.id).then(function (response) {
                    return response;
                });
            },
            get: function(id) {
                return $http.get(url + '/' + id).then(function (response) {
                    return response.data;
                });
            },
            add: function(activity, contents) {
                return $http.post(url, {activity: activity, contents:contents}).then(function (response) {
                    return response.data;
                });
            },
            update: function(activity, contents) {
                return $http.put(url + '/' + activity.id, {activity: activity, contents:contents}).then(function (response) {
                    return response.data;
                });
            },
            delete: function(activity) {
                return $http.delete(url + '/' + activity.id).then(function (response) {
                    return response.data;
                });
            }
        };
    }]);
