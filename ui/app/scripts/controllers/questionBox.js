'use strict';

angular.module('cameraboxApp')
    .controller('QuestionBoxCtrl', ['$scope', 'cameraBox', 'timer', function ($scope, cameraBox, timer) {
        // display configuration
        $scope.display = {};

        // the current question state
        $scope.question = cameraBox;

        // create a timer, each time there is a new state with a delay in it
        $scope.$watch('question.state', function() {
            if ($scope.question.state && $scope.question.state.delay && $scope.question.state.delay > 0) {
                $scope.question.state.timer = timer.createTimer($scope.question.state.delay / 1000);
                $scope.display.showQuestionDelay = true;
            } else {
                $scope.display.showQuestionDelay = false;
            }
            $scope.display.showQuestionNumber = $scope.question.state.step === 'question';
        })
    }]);
