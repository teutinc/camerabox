'use strict';

angular.module('photoboothAdminApp')
    .controller('ActivitiesCtrl', ['$scope', '$location', 'activities', function ($scope, $location, activities) {
        var refreshActivities = function () {
            activities.list().then(function (list) {
                $scope.activities = list;
            });
        };

        $scope.activities = [];
        refreshActivities();

        $scope.start = function (activity) {
            activities.start(activity).then(
                function success(response) {
                    refreshActivities();
                },
                function error(response) {
                    alert('unable to start: ' + activity.name + ' => ' + response.status);
                }
            );
        };

        $scope.stop = function (activity) {
            activities.stop(activity).then(
                function success(response) {
                    refreshActivities();
                },
                function error(response) {
                    alert('unable to stop: ' + activity.name + ' => ' + response.status);
                }
            );
        };

        $scope.create = function () {
            $location.url('/create');
        };

        $scope.edit = function (activity) {
            $location.url('/edit/' + activity.id);
        };

        $scope.delete = function (activity) {
            // fixme, add confirmation dialog...
            activities.delete(activity).then(
                function success() {
                    refreshActivities();
                },
                function error(response) {
                    alert('unable to delete: ' + activity.name + ' => ' + response.status);
                }
            );
        };
    }]);
