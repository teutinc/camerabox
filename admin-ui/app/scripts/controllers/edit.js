'use strict';

angular.module('cameraboxAdminApp')
    .controller('EditCtrl', ['$scope', '$location', '$routeParams', 'activities', function ($scope, $location, $routeParams, activities) {

        /*
            Note: this list has to be synch with the activity types managed by the server.
         */
        var availableTypes = [
            new ActivityType(
                'QuestionBoxActivity',
                function (activity) {
                    $scope.defineBye = activity.bye != undefined;
                },
                function (activity) {
                    if (!activity.questions || !activity.questions.length) {
                        alert('at least one question should be defined!')
                        return false;
                    }
                    return true;
                }
            )
        ];

        $scope.availableTypesNames = availableTypes.map(function (type) {
            return type.name;
        });

        $scope.creation = true;
        $scope.activity = {};
        $scope.contents = [];
        if ($routeParams.id) {
            activities.get($routeParams.id).then(function (activity) {
                $scope.activity = activity;
                $scope.creation = false;

                // depending on the activity type, we might want to do some things
                availableTypes
                    .filter(function (type) {
                        return type.name === activity.type;
                    })
                    .forEach(function (type) {
                        if (type.loadCallback) {
                            type.loadCallback(activity);
                        }
                    });
            });
        }

        $scope.save = function () {
            // apply validation callback associated to the activity type
            var validationSucceed = availableTypes
                .filter(function (type) {
                    return type.name === $scope.activity.type;
                })
                .filter(function (type) {
                    return !type.validateCallback || !type.validateCallback($scope.activity);
                }).length === 0;

            if (validationSucceed) {
                var func = $scope.creation ? activities.add : activities.update;
                func(angular.copy($scope.activity), angular.copy($scope.contents)).then(
                    function() {
                        $location.url('/')
                    },
                    function(error) {
                        console.dir(error);
                        alert('unable to save current activity: ' + error.data);
                    }
                );
            }
        };

        $scope.cancelEdit = function () {
            $location.url('/');
        };
    }]);

var ActivityType = function(name, loadCallback, validateCallback) {
    this.name = name;
    this.loadCallback = loadCallback;
    this.validateCallback = validateCallback;
};
